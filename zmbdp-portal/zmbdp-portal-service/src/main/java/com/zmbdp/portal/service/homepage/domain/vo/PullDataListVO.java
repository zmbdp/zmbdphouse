package com.zmbdp.portal.service.homepage.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 拉取区域数据列表 VO
 *
 * @author 稚名不带撇
 */
@Data
public class PullDataListVO implements Serializable {

    /**
     * 区域列表
     */
    private List<CityDescVO> regionList;

    /**
     * 字典类型下的字典数据
     */
    private Map<String, List<DictVO>> dictMap;
}
