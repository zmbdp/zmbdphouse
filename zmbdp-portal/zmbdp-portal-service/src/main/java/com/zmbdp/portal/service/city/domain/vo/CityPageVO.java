package com.zmbdp.portal.service.city.domain.vo;

import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 城市页面数据 VO
 *
 * @author 稚名不带撇
 */
@Data
public class CityPageVO implements Serializable {

    /**
     * 热门城市列表
     */
    private List<CityDescVO> hotCityList;

    /**
     * a-z 城市列表
     */
    private Map<String, List<CityDescVO>> allCityMap;
}