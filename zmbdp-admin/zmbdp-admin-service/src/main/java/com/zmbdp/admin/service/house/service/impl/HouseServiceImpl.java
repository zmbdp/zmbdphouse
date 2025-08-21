package com.zmbdp.admin.service.house.service.impl;

import com.zmbdp.admin.api.house.domain.dto.TagDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.admin.api.config.domain.dto.DictionaryDataDTO;
import com.zmbdp.admin.api.house.domain.dto.DeviceDTO;
import com.zmbdp.admin.service.config.domain.entity.SysDictionaryData;
import com.zmbdp.admin.service.config.mapper.SysDictionaryDataMapper;
import com.zmbdp.admin.service.config.service.ISysDictionaryService;
import com.zmbdp.admin.service.house.domain.dto.HouseAddOrEditReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import com.zmbdp.admin.service.house.domain.entity.*;
import com.zmbdp.admin.service.house.domain.enums.HouseStatusEnum;
import com.zmbdp.admin.service.house.mapper.*;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.admin.service.map.domain.entity.SysRegion;
import com.zmbdp.admin.service.map.mapper.RegionMapper;
import com.zmbdp.admin.service.user.domain.entity.AppUser;
import com.zmbdp.admin.service.user.mapper.AppUserMapper;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
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
     * 城市房源映射锁前缀
     */
    private static final String CITY_HOUSE_PREFIX = "house:list:";
    /**
     * 城市完整信息 key 前缀
     */
    private static final String HOUSE_PREFIX = "house:";

    /**
     * 锁 key
     */
    private static final String LOCK_KEY = "scheduledTask:lock";

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
            // 判断是否需要修改城市房源映射
            House existHouse = houseMapper.selectById(houseAddOrEditReqDTO.getHouseId());
            // 只有不相同了才修改
            if (cityHouseNeedChange(existHouse, houseAddOrEditReqDTO.getCityId())) {
                // 改变才更新(更新 MySQL，更新 Redis)
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

        // 设置房源完整信息缓存
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
        if (houseAddOrEditReqDTO.getUserId() == null) {
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
        // 校验设备码，房源基本配置信息（字典里面的内容）
        // 设备码：根据设备列表拿出查询出设备码
        // 获取用户传入的设备代码列表
        List<String> devices = houseAddOrEditReqDTO.getDevices();
        // 从数据库中查询这些设备代码是否有效
        List<SysDictionaryData> devicesInDb = sysDictionaryDataMapper.selectList(
                new LambdaQueryWrapper<SysDictionaryData>()
                        .eq(SysDictionaryData::getTypeKey, "device_type")
                        .in(SysDictionaryData::getDataKey, devices)
        );

        // 验证设备代码是否都有效
        if (devices.size() != devicesInDb.size()) {
            throw new ServiceException("传递的设备列表有误！", ResultCode.INVALID_PARA.getCode());
        }

        // 校验房源基本配置信息

    }

    /**
     * 判断是否需要更新城市房源映射关系
     *
     * @param oldHouse  旧房源
     * @param newCityId 新城市 id
     * @return true - 需要更新; false - 不需要更新
     */
    private boolean cityHouseNeedChange(House oldHouse, Long newCityId) {
        return !oldHouse.getCityId().equals(newCityId);
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

        // 2 1 3
        // 1 3 2
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
    private void editTagHouses(Long houseId,
                               List<TagHouse> oldTagHouses,
                               List<String> newTagCodes) {
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
                    .in(TagHouse::getTagCode, deleteTagCodes));
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
     * 根据房源 id 获取缓存中房源的完整信息
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
    }

    /**
     * 根据房源 id 获取完整的房源信息
     *
     * @param houseId 房源 id
     * @return 房源 DTO
     */
    private HouseDTO getHouseDTObyId(Long houseId) {
        if (null == houseId) {
            log.warn("要查询的房源id为空");
            return null;
        }

        // 查房源、状态、tagHouse关联关系、房东信息
        House house = houseMapper.selectById(houseId);
        if (null == house) {
            log.error("查询房源失败，houseId:{}", houseId);
            return null;
        }

        // 查房东信息
        AppUser appUser = appUserMapper.selectById(house.getUserId());
        if (null == appUser) {
            log.error("查询的房源房东信息不存在，houseId:{}, userId:{}", houseId, house.getUserId());
            return null;
        }

        // 把该房源状态从数据库查出来
        HouseStatus houseStatus = houseStatusMapper.selectOne(
                new LambdaQueryWrapper<HouseStatus>()
                        .eq(HouseStatus::getHouseId, houseId));
        if (null == houseStatus) {
            log.error("查询的房源状态信息不存在，houseId:{}", houseId);
            return null;
        }

        // 把该房源的标签从数据库查出来
        List<TagHouse> tagHouses = tagHouseMapper.selectList(new LambdaQueryWrapper<TagHouse>()
                .eq(TagHouse::getHouseId, houseId)
        );
        if (null == tagHouses) {
            log.error("查询的房源标签信息不存在，houseId:{}", houseId);
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
            redisService.setCacheObject(HOUSE_PREFIX + houseDTO.getHouseId(),
                    JsonUtil.classToJson(houseDTO));
        } catch (Exception e) {
            log.error("缓存房源完整信息时发生异常，houseDTO:{}", JsonUtil.classToJson(houseDTO), e);
            // 对于房源完整信息，是否存在于redis，不需要强一致性。
            // 因为C端查询时，如果redis不存在，可以通过查MySQL获取到数据，让后再放入Redis。
            // throw e;
        }
    }

    /**
     * 组装房源完整信息
     *
     * @param house 房源信息
     * @param houseStatus 房源状态
     * @param appUser 房东信息
     * @param tagHouses 标签信息
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
}
