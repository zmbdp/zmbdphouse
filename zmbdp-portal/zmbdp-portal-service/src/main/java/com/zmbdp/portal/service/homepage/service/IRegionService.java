package com.zmbdp.portal.service.homepage.service;

import com.zmbdp.portal.service.homepage.domain.dto.CityDescDTO;

import java.util.List;

/**
 * 调用 admin 地图服务接口
 *
 * @author 稚名不带撇
 */
public interface IRegionService {

    /**
     * 获取当前城市城市 id 对应子集城市列表
     *
     * @param parentId 父级 id
     * @return 城市列表
     */
    List<CityDescDTO> regionChildren(Long parentId);
}
