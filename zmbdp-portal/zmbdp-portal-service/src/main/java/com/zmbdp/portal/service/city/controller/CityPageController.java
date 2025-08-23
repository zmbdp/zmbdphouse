package com.zmbdp.portal.service.city.controller;

import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.portal.service.city.domain.vo.CityPageVO;
import com.zmbdp.portal.service.city.service.ICityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 城市页面控制层
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/citypage")
public class CityPageController {

    @Autowired
    private ICityService cityService;

    /**
     * 查询热门城市与全城市列表
     *
     * @return 热门城市与全城市列表
     */
    @GetMapping("/get/nologin")
    public Result<CityPageVO> cityPage() {
        return Result.success(cityService.getCityPage());
    }
}