package com.zmbdp.portal.service.city.service;

import com.zmbdp.portal.service.city.domain.vo.CityPageVO;

/**
 * 城市服务 service 层接口
 *
 * @author 稚名不带撇
 */
public interface ICityService {

    /**
     * 查询热门城市与全城市列表
     *
     * @return 热门城市与全城市列表
     */
    CityPageVO getCityPage();
}
