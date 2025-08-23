package com.zmbdp.portal.service.homepage.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.zmbdp.admin.api.map.domain.vo.RegionVO;
import com.zmbdp.admin.api.map.feign.MapServiceApi;
import com.zmbdp.common.cache.utils.CacheUtil;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.StringUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.redis.service.RedisService;
import com.zmbdp.portal.service.homepage.domain.dto.CityDescDTO;
import com.zmbdp.portal.service.homepage.service.IRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 调用 admin 地图服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class RegionServiceImpl implements IRegionService {

    /**
     * 这个城市下有什么区域缓存的 key
     */
    private static final String REGION_CHILDREN_PREFIX = "applet:region:children:";

    /**
     * 缓存过期时间
     */
    private static final Long REGION_CHILDREN_TIMEOUT = 24 * 60L;

    /**
     * 远程调用地图服务 Api
     */
    @Autowired
    private MapServiceApi mapServiceApi;

    /**
     * redis 缓存服务
     */
    @Autowired
    private RedisService redisService;

    /**
     * 一级缓存服务对象
     */
    @Autowired
    private Cache<String, Object> caffeineCache;

    /**
     * 获取当前城市城市 id 对应子集城市列表
     *
     * @param parentId 父级 id
     * @return 城市列表
     */
    @Override
    public List<CityDescDTO> regionChildren(Long parentId) {
        if (null == parentId) {
            log.error("区域 id 为空，无法查询子区域列表");
            return List.of();
        }

        // 查缓存
        List<CityDescDTO> regionList = getCacheRegionList(parentId);
        // 查到了直接返回
        if (!CollectionUtils.isEmpty(regionList)) {
            // 存在：返回
            return regionList;
        }
        // 缓存不存在，查地图 Api
        Result<List<RegionVO>> regionVOListResult = mapServiceApi.regionChildren(parentId);
        if (
                null == regionVOListResult ||
                regionVOListResult.getCode() != ResultCode.SUCCESS.getCode() ||
                null == regionVOListResult.getData()
        ) {
            log.error("获取父区域下的子区域列表失败！parentId: {}", parentId);
            return List.of();
        }
        // 数据库存在设置缓存
        regionList = BeanCopyUtil.copyListProperties(regionVOListResult.getData(), CityDescDTO::new);

        // 设置缓存
        cacheRegionList(parentId, regionList);

        return regionList;
    }

    /**
     * 获取缓存的子区域列表
     *
     * @param parentId 父级 id
     * @return 区域列表
     */
    private List<CityDescDTO> getCacheRegionList(Long parentId) {
        if (null == parentId || parentId <= 0 || StringUtil.isEmpty(String.valueOf(parentId))) {
            return null;
        }
        // 设置缓存
        return CacheUtil.getL2Cache(redisService, REGION_CHILDREN_PREFIX + parentId, new TypeReference<List<CityDescDTO>>() {
        }, caffeineCache);
    }

    /**
     * 缓存城市列表
     *
     * @param parentId   父级 id
     * @param regionList 区域列表
     */
    private void cacheRegionList(Long parentId, List<CityDescDTO> regionList) {
        if (null == parentId || parentId <= 0 || StringUtil.isEmpty(String.valueOf(parentId))) {
            return;
        }
        // 设置缓存
        // 西安id: [区1id,区2id]
        // 成都id: [区1id,区2id]
        CacheUtil.setL2Cache(
                redisService, REGION_CHILDREN_PREFIX + parentId, regionList,
                caffeineCache, REGION_CHILDREN_TIMEOUT, TimeUnit.MINUTES
        );
    }
}
