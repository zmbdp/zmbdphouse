package com.zmbdp.portal.service.homepage.controller;

import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.vo.BasePageVO;
import com.zmbdp.portal.service.homepage.domain.dto.HouseListReqDTO;
import com.zmbdp.portal.service.homepage.domain.dto.PullDataListReqDTO;
import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.HouseDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.PullDataListVO;
import com.zmbdp.portal.service.homepage.service.IHomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/homepage")
public class HomePageController {

    @Autowired
    private IHomePageService homePageService;

    /**
     * 根据经纬度获取城市信息
     *
     * @param lng 经度
     * @param lat 纬度
     */
    @GetMapping("/city_desc/get/nologin")
    public Result<CityDescVO> getCityDesc(Double lat, Double lng) {
        return Result.success(homePageService.getCityDesc(lat, lng));
    }

    /**
     * 获取下拉筛选数据列表
     *
     * @param pullDataListReqDTO 请求参数
     * @return 筛选数据列表
     */
    @PostMapping("/pull_list/get/nologin")
    public Result<PullDataListVO> getPullData(@Validated @RequestBody PullDataListReqDTO pullDataListReqDTO) {
        return Result.success(homePageService.getPullData(pullDataListReqDTO));
    }

    /**
     * 查询房源列表
     *
     * @param reqDTO 筛选信息
     * @return 房源列表
     */
    @PostMapping("/house_list/search/nologin")
    public Result<BasePageVO<HouseDescVO>> houseList(@Validated @RequestBody HouseListReqDTO reqDTO) {
        return Result.success(homePageService.houseList(reqDTO));
    }
}
