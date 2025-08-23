package com.zmbdp.portal.service.homepage.service;

import com.zmbdp.common.domain.domain.vo.BasePageVO;
import com.zmbdp.portal.service.homepage.domain.dto.HouseListReqDTO;
import com.zmbdp.portal.service.homepage.domain.dto.PullDataListReqDTO;
import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.HouseDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.PullDataListVO;

/**
 * 首页服务
 *
 * @author 稚名不带撇
 */
public interface IHomePageService {

    /**
     * 根据经纬度获取城市信息
     *
     * @param lat 用户当前纬度
     * @param lng 用户当前经度
     * @return 城市描述
     */
    CityDescVO getCityDesc(Double lat, Double lng);

    /**
     * 获取下拉筛选数据列表
     *
     * @param pullDataListReqDTO 请求参数
     * @return 筛选数据列表
     */
    PullDataListVO getPullData(PullDataListReqDTO pullDataListReqDTO);

    /**
     * 查询房源列表
     *
     * @param reqDTO 筛选信息
     * @return 房源列表
     */
    BasePageVO<HouseDescVO> houseList(HouseListReqDTO reqDTO);
}
