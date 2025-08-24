package com.zmbdp.portal.service.homepage.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.zmbdp.admin.api.config.domain.dto.DictionaryDataDTO;
import com.zmbdp.admin.api.config.feign.DictionaryServiceApi;
import com.zmbdp.common.cache.utils.CacheUtil;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.core.utils.StringUtil;
import com.zmbdp.common.redis.service.RedisService;
import com.zmbdp.portal.service.homepage.domain.dto.DictDataDTO;
import com.zmbdp.portal.service.homepage.service.IDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 调用 admin 字典服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class DictionaryServiceImpl implements IDictionaryService {

    private static final String DICT_TYPE_PREFIX = "applet:dict:type:";
    private static final Long DICT_TYPE_TIMEOUT = 5L;
    private static final String DICT_DATA_PREFIX = "applet:dict:data:";
    private static final Long DICT_DATA_TIMEOUT = 5L;

    /**
     * 字典服务 Api
     */
    @Autowired
    private DictionaryServiceApi dictionaryServiceApi;

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
     * 根据字典类型查询字典数据列表
     *
     * @param types 字典类型
     * @return key: type  value: dataList
     */
    @Override
    public Map<String, List<DictDataDTO>> batchFindDictionaryDataByTypes(List<String> types) {
        Map<String, List<DictDataDTO>> resultMap = new HashMap<>();

        // 从缓存获取
        // type1: [data1, data2...]
        // type2: [data1, data2...]
        List<String> notCacheTypes = new ArrayList<>();
        for (String type : types) {
            List<DictDataDTO> dataDTOList = getCacheList(type);
            if (CollectionUtils.isEmpty(dataDTOList)) {
                notCacheTypes.add(type);
            } else {
                resultMap.put(type, dataDTOList);
            }
        }

        // 全部存在：返回
        if (CollectionUtils.isEmpty(notCacheTypes)) {
            return resultMap;
        }

        // 不存在的就远程调用访问
        Map<String, List<DictionaryDataDTO>> dictionaryDataDTOMap = dictionaryServiceApi.selectDictDataByTypes(notCacheTypes);
        if (CollectionUtils.isEmpty(dictionaryDataDTOMap)) {
            log.error("字典类型不存在！ notCacheTypes:{}", JsonUtil.classToJson(notCacheTypes));
            return resultMap;
        }
        dictionaryDataDTOMap.forEach((type, list) -> {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            cacheList(type, BeanCopyUtil.copyListProperties(list, DictDataDTO::new));
            resultMap.put(type, BeanCopyUtil.copyListProperties(list, DictDataDTO::new));
        });
        return resultMap;
    }

    /**
     * 获取字典类型缓存数据
     *
     * @param type 字典类型
     * @return 字典数据
     */
    private List<DictDataDTO> getCacheList(String type) {
        if (StringUtil.isBlank(type)) {
            return List.of();
        }

        return CacheUtil.getL2Cache(redisService, DICT_TYPE_PREFIX + type, new TypeReference<List<DictDataDTO>>() {
        }, caffeineCache);
    }

    /**
     * 缓存字典类型数据
     *
     * @param type               字典类型
     * @param copyListProperties 字典数据
     */
    private void cacheList(String type, List<DictDataDTO> copyListProperties) {
        if (StringUtil.isBlank(type)) {
            return;
        }

        CacheUtil.setL2Cache(
                redisService, DICT_TYPE_PREFIX + type, copyListProperties,
                caffeineCache, DICT_TYPE_TIMEOUT, TimeUnit.MINUTES
        );
    }


    /**
     * 根据字典数据 key 列表获取字典数据
     *
     * @param dataKeys 字典数据 keys
     * @return key: dataKey  value: data
     */
    @Override
    public Map<String, DictDataDTO> batchFindDictionaryData(List<String> dataKeys) {
        Map<String, DictDataDTO> resultMap = new HashMap<>();

        // 查缓存: dataKey:DictDataDTO
        List<String> noCacheDataKeys = new ArrayList<>();
        for (String dataKey : dataKeys) {
            DictDataDTO dictDataDTO = getDataCache(dataKey);
            if (null == dictDataDTO) {
                noCacheDataKeys.add(dataKey);
            } else {
                resultMap.put(dataKey, dictDataDTO);
            }
        }

        // 全部存在：返回
        if (CollectionUtils.isEmpty(noCacheDataKeys)) {
            return resultMap;
        }

        // 不存在：feign
        List<DictionaryDataDTO> dataDTOList = dictionaryServiceApi.getDicDataByKeys(noCacheDataKeys);
        if (CollectionUtils.isEmpty(dataDTOList)) {
            log.error("feign 字典数据不存在！noCacheDataKeys：{}", JsonUtil.classToJson(noCacheDataKeys));
            return resultMap;
        }

        // 缓存结果
        for (DictionaryDataDTO dictionaryDataDTO :  dataDTOList) {
            DictDataDTO dictDataDTO = new DictDataDTO();
            BeanCopyUtil.copyProperties(dictionaryDataDTO, dictDataDTO);
            cacheData(dictionaryDataDTO.getDataKey(), dictDataDTO);
            resultMap.put(dictionaryDataDTO.getDataKey(), dictDataDTO);
        }
        return resultMap;
    }

    /**
     * 根据字典类型查询缓存中的字典数据列表
     * @param dataKey 字典数据键
     * @return 字典数据
     */
    private DictDataDTO getDataCache(String dataKey) {
        if (StringUtil.isEmpty(dataKey)) {
            return null;
        }
        return redisService.getCacheObject(DICT_DATA_PREFIX + dataKey, DictDataDTO.class);
    }

    /**
     * 缓存字典数据
     * @param dataKey 字典数据业务主键
     * @param dictDataDTO 字典数据对象
     */
    private void cacheData(String dataKey, DictDataDTO dictDataDTO) {
        if (StringUtil.isEmpty(dataKey)) {
            return;
        }
        redisService.setCacheObject(DICT_DATA_PREFIX + dataKey, dictDataDTO, DICT_DATA_TIMEOUT, TimeUnit.MINUTES);
    }
}
