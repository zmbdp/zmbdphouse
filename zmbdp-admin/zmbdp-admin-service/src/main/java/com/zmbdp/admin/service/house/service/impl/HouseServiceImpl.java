package com.zmbdp.admin.service.house.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.admin.api.config.domain.dto.DictionaryDataDTO;
import com.zmbdp.admin.api.house.domain.dto.DeviceDTO;
import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.api.house.domain.dto.TagDTO;
import com.zmbdp.admin.service.config.mapper.SysDictionaryDataMapper;
import com.zmbdp.admin.service.config.service.ISysDictionaryService;
import com.zmbdp.admin.service.house.domain.dto.*;
import com.zmbdp.admin.service.house.domain.entity.*;
import com.zmbdp.admin.service.house.domain.enums.HouseStatusEnum;
import com.zmbdp.admin.service.house.mapper.*;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.admin.service.map.domain.entity.SysRegion;
import com.zmbdp.admin.service.map.mapper.RegionMapper;
import com.zmbdp.admin.service.user.domain.entity.AppUser;
import com.zmbdp.admin.service.user.mapper.AppUserMapper;
import com.zmbdp.common.bloomfilter.service.BloomFilterService;
import com.zmbdp.common.core.domain.dto.BasePageDTO;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.core.utils.StringUtil;
import com.zmbdp.common.core.utils.TimestampUtil;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.common.redis.service.RedisService;
import com.zmbdp.common.redis.service.RedissonLockService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 房源服务业务层
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class HouseServiceImpl implements IHouseService {

    /**
     * 用户前缀
     */
    private static final String APP_USER_PREFIX = "app_user:";

    /**
     * 城市房源映射锁前缀
     */
    private static final String CITY_HOUSE_PREFIX = "house:list:";

    /**
     * 城市完整信息 key 前缀
     */
    private static final String HOUSE_PREFIX = "house:";

    /**
     * 锁 key (初始化的时候用)
     */
    private static final String HOUSE_LOCK_KEY = "house:lock";

    /**
     * 房源表 mapper
     */
    @Autowired
    private HouseMapper houseMapper;

    /**
     * 用户表 mapper
     */
    @Autowired
    private AppUserMapper appUserMapper;

    /**
     * 地区表 mapper
     */
    @Autowired
    private RegionMapper regionMapper;

    /**
     * 标签表 mapper
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * 城市房源映射表 mapper
     */
    @Autowired
    private CityHouseMapper cityHouseMapper;

    /**
     * 房源状态表 mapper
     */
    @Autowired
    private HouseStatusMapper houseStatusMapper;

    /**
     * 标签房源映射表 mapper
     */
    @Autowired
    private TagHouseMapper tagHouseMapper;

    /**
     * 数据字典表 mapper
     */
    @Autowired
    private SysDictionaryDataMapper sysDictionaryDataMapper;

    /**
     * Redis 服务
     */
    @Autowired
    private RedisService redisService;

    /**
     * 数据字典服务
     */
    @Autowired
    private ISysDictionaryService sysDictionaryService;

    /**
     * 布隆过滤器服务
     */
    @Autowired
    private BloomFilterService bloomFilterService;

    /**
     * Redisson 分布式锁服务
     */
    @Autowired
    private RedissonLockService redissonLockService;

    /**
     * 初始化房源数据
     */
    @PostConstruct
    public void initHouse() {
        // 缓存预热房源信息

        // 加锁（布隆过滤器得单独处理，因为是一级缓存，放到锁里面执行一次的话就不对劲）
        // 先拿出所有数据
        List<Long> houseIds = houseMapper.selectHouseIds();
        if (houseIds.isEmpty()) {
            log.info("没有房源数据");
            return;
        }
        for (Long houseId : houseIds) {
            bloomFilterService.put(HOUSE_PREFIX + String.valueOf(houseId));
        }
        RLock lock = redissonLockService.acquire(HOUSE_LOCK_KEY, 3, TimeUnit.SECONDS);
        if (null == lock) {
            log.info("房源数据预热已获取锁失败，跳过执行");
            return;
        }
        try {
            // 房源信息
            refreshHouseIds();
        } catch (Exception e) {
            log.error("房源数据预热异常", e);
        } finally {
            // 释放锁
            redissonLockService.releaseLock(lock);
        }
    }


    /**
     * 添加或编辑房源
     *
     * @param houseAddOrEditReqDTO 房源信息
     * @return 房源 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 表示抛出异常就回滚，保证事务完整性
    public Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        // 校验参数
        checkAddOrEditReq(houseAddOrEditReqDTO);

        // 拷贝字段
        House house = new House();
        BeanCopyUtil.copyProperties(houseAddOrEditReqDTO, house);
        // 拷贝不能直接拷贝的属性
        house.setArea(BigDecimal.valueOf(houseAddOrEditReqDTO.getArea()));
        house.setPrice(BigDecimal.valueOf(houseAddOrEditReqDTO.getPrice()));
        // 表： soft,washer,broadband
        // 请求：["soft", "washer", "broadband"]
        house.setDevices(
                houseAddOrEditReqDTO.getDevices().stream()
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(","))
        );
        house.setImages(JsonUtil.classToJson(houseAddOrEditReqDTO.getImages()));
        house.setLongitude(BigDecimal.valueOf(houseAddOrEditReqDTO.getLongitude()));
        house.setLatitude(BigDecimal.valueOf(houseAddOrEditReqDTO.getLatitude()));

        // 编辑 需要判断是否更新 城市房源映射、标签房源映射
        if (houseAddOrEditReqDTO.getHouseId() != null) {
            house.setId(houseAddOrEditReqDTO.getHouseId());
            // 判断是否需要修改城市房源映射
            House existHouse = houseMapper.selectById(houseAddOrEditReqDTO.getHouseId());
            if (existHouse == null) {
                log.warn("房源信息不存在; oldHouse: {}]", houseAddOrEditReqDTO.getHouseId());
                throw new ServiceException("传递的房源id有误！", ResultCode.INVALID_PARA.getCode());
            }
            // 只有不相同了才修改
            if (cityHouseNeedChange(existHouse, houseAddOrEditReqDTO.getCityId())) {
                // 改变才更新 (更新 MySQL，更新 Redis)
                editCityHouses(houseAddOrEditReqDTO.getHouseId(), existHouse.getCityId(),
                        houseAddOrEditReqDTO.getCityId(), houseAddOrEditReqDTO.getCityName());
            }

            // 判断是否需要修改房源标签映射
            // 先从房源标签映射表中拿出这个房源的所有标签信息
            List<TagHouse> tagHouses = tagHouseMapper.selectList(new LambdaQueryWrapper<TagHouse>()
                    .eq(TagHouse::getHouseId, houseAddOrEditReqDTO.getHouseId()));
            // 再根据标签列表和标签
            if (tagHouseNeedChange(tagHouses, houseAddOrEditReqDTO.getTagCodes())) {
                // 改变才更新 (更新 Mysql)
                editTagHouses(houseAddOrEditReqDTO.getHouseId(), tagHouses, houseAddOrEditReqDTO.getTagCodes());
            }
        }

        // 插入到数据库
        houseMapper.insertOrUpdate(house);

        // 新增的话, 不仅要更新 城市房源映射、标签房源映射，还要更新 Redis
        if (houseAddOrEditReqDTO.getHouseId() == null) {
            HouseStatus houseStatus = new HouseStatus();
            houseStatus.setHouseId(house.getId());
            houseStatus.setStatus(HouseStatusEnum.UP.name());
            houseStatusMapper.insert(houseStatus);
            // 设置房源地区 mysql 和 缓存
            addCityHouse(house.getId(), house.getCityId(), house.getCityName());

            // 设置房源标签 MySQL
            addTagHouses(house.getId(), houseAddOrEditReqDTO.getTagCodes());
        }

        // 设置房源完整信息缓存 (房源 id 添加到布隆过滤器)
        cacheHouse(house.getId());
        return house.getId();
    }

    /**
     * 校验新增或编辑房源请求参数
     *
     * @param houseAddOrEditReqDTO 新增或编辑房源请求参数
     */
    private void checkAddOrEditReq(HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        // 校验房东信息
        // 查询房东是否存在
        if (houseAddOrEditReqDTO.getUserId() == null || houseAddOrEditReqDTO.getUserId() < 0 || !bloomFilterService.mightContain(APP_USER_PREFIX + String.valueOf(houseAddOrEditReqDTO.getUserId()))) {
            throw new ServiceException("房东 id 不存在！", ResultCode.INVALID_PARA.getCode());
        }
        AppUser appUser = appUserMapper.selectById(houseAddOrEditReqDTO.getUserId());
        if (null == appUser) {
            throw new ServiceException("房东 id 不存在！", ResultCode.INVALID_PARA.getCode());
        }
        // 校验地址信息 (城市 id, 区域 id)，看看这两个 id 是否都能查到，都能查到就正常
        List<Long> regionIds = Arrays.asList(houseAddOrEditReqDTO.getCityId(), houseAddOrEditReqDTO.getRegionId());
        List<SysRegion> regions = regionMapper.selectBatchIds(regionIds);
        if (regionIds.size() != regions.size()) {
            throw new ServiceException("传递的城市信息有误！", ResultCode.INVALID_PARA.getCode());
        }

        // 校验标签码
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Tag::getTagCode, houseAddOrEditReqDTO.getTagCodes());
        List<Tag> tags = tagMapper.selectList(queryWrapper);
        houseAddOrEditReqDTO.setTagCodes(
                houseAddOrEditReqDTO.getTagCodes().stream()
                        .distinct() // 去重
                        .collect(Collectors.toList())
        );
        if (houseAddOrEditReqDTO.getTagCodes().size() != tags.size()) {
            throw new ServiceException("传递的标签列表有误！", ResultCode.INVALID_PARA.getCode());
        }

    }

    /**
     * 判断是否需要更新城市房源映射关系
     *
     * @param oldHouse  旧房源
     * @param newCityId 新城市 id
     * @return true - 需要更新; false - 不需要更新
     */
    private boolean cityHouseNeedChange(House oldHouse, Long newCityId) {
        return !Objects.equals(oldHouse.getCityId(), newCityId);
    }

    /**
     * 编辑城市房源映射（Mysql、Redis）
     *
     * @param houseId     房源 id
     * @param oldCityId   旧城市 id
     * @param newCityId   新城市 id
     * @param newCityName 新城市名称
     */
    private void editCityHouses(Long houseId, Long oldCityId,
                                Long newCityId, String newCityName) {

        // 删除老的映射记录
        cityHouseMapper.delete(new LambdaQueryWrapper<CityHouse>()
                .eq(CityHouse::getCityId, oldCityId)
                .eq(CityHouse::getHouseId, houseId));

        // 新增新的映射记录
        CityHouse cityHouse = new CityHouse();
        cityHouse.setHouseId(houseId);
        cityHouse.setCityId(newCityId);
        cityHouse.setCityName(newCityName);
        cityHouseMapper.insert(cityHouse);

        // 更新缓存
        cacheCityHouses(2, houseId, oldCityId, newCityId);
    }

    /**
     * 缓存城市房源映射关系（redis）
     *
     * @param op        操作类型：1 - 新增；2 - 修改；
     * @param houseId   房源 id
     * @param oldCityId 旧城市 id
     * @param newCityId 新城市 id
     */
    private void cacheCityHouses(int op, Long houseId, Long oldCityId, Long newCityId) {
        try {
            if (1 == op) {
                // 新增场景：新增城市下的房源 id
                redisService.setCacheList(CITY_HOUSE_PREFIX + newCityId, Collections.singletonList(houseId));
            } else if (2 == op) {
                // 修改场景：
                // 删除老城市下的房源
                redisService.removeLeftForList(CITY_HOUSE_PREFIX + oldCityId, houseId);
                // 新增新城市下的房源
                redisService.setCacheList(CITY_HOUSE_PREFIX + newCityId, Collections.singletonList(houseId));
            } else {
                log.error("无效的操作：缓存城市房源关联信息");
            }
        } catch (Exception e) {
            log.error("缓存城市下的房源列表发生异常，op:{}, houseId:{}, oldCityId:{}, newCityId:{}", op, houseId, oldCityId, newCityId, e);
            // 抛出异常，保证事务
            // 因为 C端获取房源列表是以城市 ID 列表为主的，必须保证 redis 和 mysql 的数据的一致性
            throw e;
        }
    }

    /**
     * 判断房源标签映射关系是否需要更新
     *
     * @param oldTagHouses 旧标签映射关系
     * @param newTagCodes  新标签码
     * @return true - 需要更新; false - 不需要更新
     */
    private boolean tagHouseNeedChange(List<TagHouse> oldTagHouses, List<String> newTagCodes) {
        // 从旧标签映射关系中获取所有的标签码
        List<String> oldTagCods = oldTagHouses.stream()
                .map(TagHouse::getTagCode)
                .sorted()  // 排序
                .collect(Collectors.toList());

        // 然后再把新的标签码排序
        newTagCodes = newTagCodes.stream().sorted().collect(Collectors.toList());

        // 不相等的时候才需要更新
        return !Objects.equals(oldTagCods, newTagCodes);
    }

    /**
     * 编辑房源标签映射关系
     *
     * @param houseId      房源 id
     * @param oldTagHouses 旧标签映射关系
     * @param newTagCodes  新标签码
     */
    private void editTagHouses(Long houseId, List<TagHouse> oldTagHouses, List<String> newTagCodes) {
        // houseId: 1
        // old：1 2 3 4 5
        // new: 3 4 5 6 7

        // 需要过滤出要删除的 tagCodes 1 2
        Set<String> oldTagCodes = oldTagHouses.stream()
                .map(TagHouse::getTagCode)
                .collect(Collectors.toSet());
        List<String> deleteTagCodes = oldTagCodes.stream()
                // 判断：旧标签码是否在新标签码中，如果在新标签码中则返回 false，相当与旧标签码不在的时候才需要删除
                .filter(oldTagCode -> !newTagCodes.contains(oldTagCode))
                .collect(Collectors.toList());

        // 删除需要删除的 tagCodes 1 2
        if (!CollectionUtils.isEmpty(deleteTagCodes)) {
            tagHouseMapper.delete(new LambdaQueryWrapper<TagHouse>()
                    .eq(TagHouse::getHouseId, houseId)
                    .in(TagHouse::getTagCode, deleteTagCodes)
            );
        }

        // 过滤出要新增的 tagCodes  6 7
        List<TagHouse> newTagHouses = newTagCodes.stream()
                .filter(newTagCode -> !oldTagCodes.contains(newTagCode)) // 6 7 (String)
                .map(newTagCode -> {
                    TagHouse tagHouse = new TagHouse();
                    tagHouse.setTagCode(newTagCode);
                    tagHouse.setHouseId(houseId);
                    return tagHouse;
                }).collect(Collectors.toList());

        // 新增要新增的 tagCodes 6 7
        if (!CollectionUtils.isEmpty(newTagHouses)) {
            tagHouseMapper.insert(newTagHouses);
        }
    }

    /**
     * 新增城市房源映射关系（MySQL，Redis）
     *
     * @param houseId  房源 id
     * @param cityId   城市 id
     * @param cityName 城市名称
     */
    private void addCityHouse(Long houseId, Long cityId, String cityName) {
        CityHouse cityHouse = new CityHouse();
        cityHouse.setCityId(cityId);
        cityHouse.setCityName(cityName);
        cityHouse.setHouseId(houseId);
        cityHouseMapper.insert(cityHouse);

        // 新增城市房源列表缓存
        cacheCityHouses(1, houseId, null, cityId);
    }

    /**
     * 新增标签映射关系（MySQL）
     *
     * @param houseId  房源 id
     * @param tagCodes 标签码
     */
    private void addTagHouses(Long houseId, List<String> tagCodes) {
        List<TagHouse> tagHouses = tagCodes.stream()
                .map(tagCode -> {
                    TagHouse tagHouse = new TagHouse();
                    tagHouse.setTagCode(tagCode);
                    tagHouse.setHouseId(houseId);
                    return tagHouse;
                }).collect(Collectors.toList());
        tagHouseMapper.insert(tagHouses);
    }

    /**
     * 更新房源缓存
     *
     * @param houseId 房源 id
     */
    @Override
    public void cacheHouse(Long houseId) {
        // 判空
        if (null == houseId) {
            log.warn("要缓存的房源id为空！");
            return;
        }

        // 通过 id 查询完整的信息
        HouseDTO houseDTO = getHouseDTObyId(houseId);
        if (null == houseDTO) {
            log.warn("缓存房源信息时，查询房源错误！");
            return;
        }
        // 缓存
        cacheHouse(houseDTO);
        // 布隆过滤器
        bloomFilterService.put(HOUSE_PREFIX + houseDTO.getHouseId());
    }

    /**
     * 查询房源详情（带缓存）
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    @Override
    public HouseDTO detail(Long houseId) {
        // 判空
        if (null == houseId || houseId < 0) {
            log.warn("要查询的房源id为空或无效！");
            return null;
        }
        // 先查缓存 + 布隆过滤器
        HouseDTO houseDTO = getCacheHouse(houseId);

        // 缓存存在直接返回
        if (null != houseDTO) {
            return houseDTO;
        }

        // 缓存不存在，查询 Mysql
        houseDTO = getHouseDTObyId(houseId);

        // mysql 不存在，缓存空对象（解决缓存穿透）
        if (null == houseDTO) {
            // 设置缓存空对象，设置到 redis 和 布隆过滤器 里面，解决缓存穿透问题
            cacheNullHouse(houseId, 60L);
            log.error("查询房源信息错误，houseId:{}", houseId);
            return null;
        }

        // mysql 存在，缓存房源详情
        cacheHouse(houseDTO);
        // 布隆过滤器
        bloomFilterService.put(HOUSE_PREFIX + houseDTO.getHouseId());
        // 返回
        return houseDTO;
    }

    /**
     * 根据房源 id 获取完整的房源信息
     *
     * @param houseId 房源 id
     * @return 房源 DTO
     */
    private HouseDTO getHouseDTObyId(Long houseId) {
        if (null == houseId) {
            log.warn("要查询的房源 id 为空");
            return null;
        }

        // TODO: 可以加个锁，保证每次查询的就只有一个就行了

        // 查房源、状态、tagHouse关联关系、房东信息
        House house = houseMapper.selectById(houseId);
        if (null == house) {
            log.error("查询房源失败，houseId: {}", houseId);
            return null;
        }

        // 查房东信息
        AppUser appUser = appUserMapper.selectById(house.getUserId());
        if (null == appUser) {
            log.error("查询的房源房东信息不存在，houseId: {}, userId: {}", houseId, house.getUserId());
            return null;
        }

        // 把该房源状态从数据库查出来
        HouseStatus houseStatus = houseStatusMapper.selectOne(new LambdaQueryWrapper<HouseStatus>()
                .eq(HouseStatus::getHouseId, houseId)
        );
        if (null == houseStatus) {
            log.error("查询的房源状态信息不存在，houseId: {}", houseId);
            return null;
        }

        // 把该房源的标签从数据库查出来
        List<TagHouse> tagHouses = tagHouseMapper.selectList(new LambdaQueryWrapper<TagHouse>()
                .eq(TagHouse::getHouseId, houseId)
        );
        if (null == tagHouses) {
            log.error("查询的房源标签信息不存在，houseId: {}", houseId);
            return null;
        }
        // 组装完整的房源信息 DTO
        return convertToHouseDTO(house, houseStatus, appUser, tagHouses);
    }

    /**
     * 缓存房源完整数据 houseDTO
     *
     * @param houseDTO 房源完整数据 houseDTO
     */
    private void cacheHouse(HouseDTO houseDTO) {
        if (null == houseDTO) {
            log.warn("要缓存的房源详细信息为空！");
            return;
        }

        // 缓存
        try {
            redisService.setCacheObject(HOUSE_PREFIX + houseDTO.getHouseId(), houseDTO);
        } catch (Exception e) {
            log.error("缓存房源完整信息时发生异常，houseDTO: {}", JsonUtil.classToJson(houseDTO), e);
        }
    }

    /**
     * 组装房源完整信息
     *
     * @param house       房源信息
     * @param houseStatus 房源状态
     * @param appUser     房东信息
     * @param tagHouses   标签信息
     * @return 房源完整信息
     */
    private HouseDTO convertToHouseDTO(
            House house, HouseStatus houseStatus,
            AppUser appUser, List<TagHouse> tagHouses
    ) {
        // 校验数据合法性
        if (null == house || null == houseStatus || null == appUser) {
            log.warn("房源信息不完整！");
            return null;
        }

        HouseDTO houseDTO = new HouseDTO();
        BeanCopyUtil.copyProperties(house, houseDTO);
        BeanCopyUtil.copyProperties(houseStatus, houseDTO);
        BeanCopyUtil.copyProperties(appUser, houseDTO);

        houseDTO.setArea(house.getArea().doubleValue());
        houseDTO.setPrice(house.getPrice().doubleValue());
        houseDTO.setLongitude(house.getLongitude().doubleValue());
        houseDTO.setLatitude(house.getLatitude().doubleValue());
        houseDTO.setImages(JsonUtil.jsonToList(house.getImages(), String.class));

        // 表： soft,washer,broadband
        // DeviceDTO:  String deviceCode，String deviceName;
        List<String> dataKeys = Arrays.stream(house.getDevices().split(","))
                .distinct()
                .collect(Collectors.toList());
        List<DictionaryDataDTO> deviceDataDTOS = sysDictionaryService.getDicDataByKeys(dataKeys);
        List<DeviceDTO> deviceDTOS = deviceDataDTOS.stream()
                .map(dataDTO -> {
                    DeviceDTO deviceDTO = new DeviceDTO();
                    deviceDTO.setDeviceCode(dataDTO.getDataKey());
                    deviceDTO.setDeviceName(dataDTO.getValue());
                    return deviceDTO;
                }).collect(Collectors.toList());
        houseDTO.setDevices(deviceDTOS);

        // TagDTO:String tagCode; String tagName;
        // 表 Tag

        // 获取到 tagCodes，接着查询 Tag
        List<String> tagCodes = tagHouses.stream()
                .map(TagHouse::getTagCode)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(tagCodes)) {
            List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getTagCode, tagCodes)
            );
            houseDTO.setTags(BeanCopyUtil.copyListProperties(tags, TagDTO::new));
        }
        return houseDTO;
    }

    /**
     * 从缓存查询房源详情
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    private HouseDTO getCacheHouse(Long houseId) {
        if (null == houseId || !bloomFilterService.mightContain(HOUSE_PREFIX + houseId)) {
            return null;
        }
        try {
            return redisService.getCacheObject(HOUSE_PREFIX + houseId, HouseDTO.class);
        } catch (Exception e) {
            log.error("从缓存中获取房源详情异常，key: {}", HOUSE_PREFIX + houseId, e);
        }
        return null;
    }

    /**
     * 缓存房源空对象 (带过期时间)
     *
     * @param houseId 房源 id
     * @param timeout 过期时间（秒）
     */
    private void cacheNullHouse(Long houseId, Long timeout) {

        if (null == houseId) {
            log.warn("要缓存的房源id为空！");
            return;
        }

        // 缓存
        try {
            redisService.setCacheObject(HOUSE_PREFIX + houseId, new HouseDTO(), timeout, TimeUnit.SECONDS);
            bloomFilterService.put(HOUSE_PREFIX + houseId);
        } catch (Exception e) {
            log.error("缓存空房源完整信息时发生异常，houseId: {}", houseId, e);
            // 对于房源完整信息，是否存在于 redis，不需要强一致性。
            // 因为 C端查询时，如果 redis 不存在，可以通过查 MySQL 获取到数据，让后再放入 Redis。
        }
    }

    /**
     * 查询房源摘要列表（支持翻页、支持筛选）
     *
     * @param houseListReqDTO 查询参数
     * @return 房源摘要列表
     */
    @Override
    public BasePageDTO<HouseDescDTO> list(HouseListReqDTO houseListReqDTO) {

        BasePageDTO<HouseDescDTO> result = new BasePageDTO<>();

        // 查询总数：联表查询
        // 涉及 house_status、house 两张表

        // 先查询一下符合条件的总数
        Long totals = houseMapper.selectCountWithStatus(houseListReqDTO);

        // 为空就直接返回空就行
        if (0 == totals) {
            result.setTotals(0);
            result.setTotalPages(0);
            result.setList(List.of());
            log.info("查询的房源列表为空！HouseListReqDTO:{}", JsonUtil.classToJson(houseListReqDTO));
            return result;
        }

        // 分页查询
        List<HouseDescDTO> houses = houseMapper.selectPageWithStatus(houseListReqDTO);
        result.setTotals(totals.intValue());
        result.setTotalPages(BasePageDTO.calculateTotalPages(totals, houseListReqDTO.getPageSize()));
        // 判断分页查询出来的结果是否为空，可能是超页 (查询第三页，可是第三页没有数据)
        if (CollectionUtils.isEmpty(houses)) {
            log.info("超出查询房源列表范围！HouseListReqDTO:{}", JsonUtil.classToJson(houseListReqDTO));
            result.setList(List.of());
            return result;
        }
        result.setList(houses);
        return result;
    }

    /**
     * 更新房源状态
     *
     * @param houseStatusEditReqDTO 房源状态修改参数
     */
    @Override
    public void editStatus(HouseStatusEditReqDTO houseStatusEditReqDTO) {
        // 检测房源是否存在
        if (!bloomFilterService.mightContain(HOUSE_PREFIX + houseStatusEditReqDTO.getHouseId())) {
            log.warn("要修改状态的房源不存在！houseId: {}", houseStatusEditReqDTO.getHouseId());
            throw new ServiceException("房源不存在，无法修改状态！");
        }
        House house = houseMapper.selectById(houseStatusEditReqDTO.getHouseId());
        if (null == house) {
            log.warn("要修改状态的房源不存在！houseId: {}", houseStatusEditReqDTO.getHouseId());
            throw new ServiceException("房源不存在，无法修改状态！");
        }
        // 根据房源 id，查询房源状态映射表，然后修改
        HouseStatus houseStatus = houseStatusMapper.selectOne(new LambdaQueryWrapper<HouseStatus>()
                .eq(HouseStatus::getHouseId, house.getId())
        );
        if (null == houseStatus || StringUtil.isEmpty(houseStatus.getStatus())) {
            throw new ServiceException("房源状态不存在，无法修改状态！");
        }

        // 校验状态传参 (status是枚举)
        HouseStatusEnum statusEnum = HouseStatusEnum.getByName(houseStatusEditReqDTO.getStatus());
        if (null == statusEnum) {
            throw new ServiceException("要修改的房源状态有误，无法修改状态！");
        }
        // 更新数据库 (house_status)
        houseStatus.setStatus(houseStatusEditReqDTO.getStatus());
        // 如果是出租中，则设置出租开始时间、出租结束时间
        if (HouseStatusEnum.RENTING.name().equalsIgnoreCase(houseStatusEditReqDTO.getStatus())) {

            // 校验是否传了出租时长码
            if (StringUtils.isEmpty(houseStatusEditReqDTO.getRentTimeCode())) {
                throw new ServiceException("出租时长不能为空，无法修改状态！");
            }

            houseStatus.setRentTimeCode(houseStatusEditReqDTO.getRentTimeCode());
            houseStatus.setRentStartTime(TimestampUtil.getCurrentMillis());
            switch (houseStatusEditReqDTO.getRentTimeCode()) {
                case "one_year" -> houseStatus.setRentEndTime(TimestampUtil.getYearLaterMillis(1L));
                case "half_year" -> houseStatus.setRentEndTime(TimestampUtil.getMonthsLaterMillis(6L));
                // 测试用的时间 30秒
                case "thirty_seconds" -> houseStatus.setRentEndTime(TimestampUtil.getSecondsLaterMillis(30L));
                default -> throw new ServiceException("出租时长错误，无法修改状态！");
            }
        }
        houseStatusMapper.updateById(houseStatus);

        // 更新缓存
        cacheHouse(house.getId());
    }

    /**
     * 根据房东 id 查询其下房源 id 列表
     *
     * @param userId 房东 id
     * @return 房源 id 列表
     */
    @Override
    public List<Long> listByUserId(Long userId) {
        if (null == userId || userId < 0 || !bloomFilterService.mightContain(APP_USER_PREFIX + String.valueOf(userId))) {
            return List.of();
        }

        List<House> houses = houseMapper.selectList(
                new LambdaQueryWrapper<House>()
                        .eq(House::getUserId, userId));
        return houses.stream().map(House::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 刷新缓存的房源
     */
    @Override
    public void refreshHouseIds() {
        // 查询全量城市列表（2级城市）
        List<SysRegion> sysRegions = regionMapper.selectList(new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getLevel, "2"));

        for (SysRegion sysRegion : sysRegions) {
            // 删除当前城市下所有的房源列表映射（Redis）
            Long cityId = sysRegion.getId();
            redisService.removeForAllList(CITY_HOUSE_PREFIX + cityId);

            // 查询当前城市下所有的房源列表（MySQL）
            List<CityHouse> cityHouseList = cityHouseMapper.selectList(new LambdaQueryWrapper<CityHouse>()
                    .eq(CityHouse::getCityId, cityId)
            );

            // 设置当前城市下所有的房源列表映射（Redis）
            if (!CollectionUtils.isEmpty(cityHouseList)) {
                redisService.setCacheList(CITY_HOUSE_PREFIX + cityId,
                        cityHouseList.stream()
                                .map(CityHouse::getHouseId)
                                .distinct() // 去重
                                .toList()
                );
            }

            // 更新房源列表详细信息（Redis）
            for (CityHouse cityHouse : cityHouseList) {
                cacheHouse(cityHouse.getHouseId());
            }
        }
    }

    /**
     * 根据城市 id 和排序规则查询房源列表，支持筛选、排序、翻页
     *
     * @param searchHouseListReqDTO 查询参数
     * @return 房源列表
     */
    @Override
    public BasePageDTO<HouseDTO> searchList(SearchHouseListReqDTO searchHouseListReqDTO) {
        return null;
    }
}
