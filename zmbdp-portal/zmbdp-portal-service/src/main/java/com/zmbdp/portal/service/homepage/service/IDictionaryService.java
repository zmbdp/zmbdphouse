package com.zmbdp.portal.service.homepage.service;

import com.zmbdp.portal.service.homepage.domain.dto.DictDataDTO;

import java.util.List;
import java.util.Map;

/**
 * 门户调用字典服务
 *
 * @author 稚名不带撇
 */
public interface IDictionaryService {

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param types 字典类型
     * @return key: type  value: dataList
     */
    Map<String, List<DictDataDTO>> batchFindDictionaryDataByTypes(List<String> types);

    /**
     * 根据字典数据 keys 获取字典数据
     *
     * @param dataKeys 字典数据 keys
     * @return key: dataKey  value: data
     */
    Map<String, DictDataDTO> batchFindDictionaryData(List<String> dataKeys);
}
