package com.zmbdp.portal.service.house.controller;

import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.portal.service.house.domain.vo.HouseDataVO;
import com.zmbdp.portal.service.house.service.IHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 房源服务控制器
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/housepage")
public class HousePageController {

    @Autowired
    private IHouseService houseService;

    /**
     * C端 根据房源 id 查询房源详情
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    @GetMapping("/get/nologin")
    public Result<HouseDataVO> houseDetail(@RequestParam("houseId") Long houseId) {
        return Result.success(houseService.houseDetail(houseId));
    }
}