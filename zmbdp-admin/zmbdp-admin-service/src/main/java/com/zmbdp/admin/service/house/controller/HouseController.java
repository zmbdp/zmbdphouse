package com.zmbdp.admin.service.house.controller;

import com.zmbdp.admin.service.house.domain.dto.HouseAddOrEditReqDTO;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.common.domain.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 房源管理服务
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/house")
public class HouseController {

    /**
     * 房源服务
     */
    @Autowired
    private IHouseService houseService;

    /**
     * 添加或编辑房源
     *
     * @param houseAddOrEditReqDTO 房源信息
     * @return 添加或编辑结果
     */
    @PostMapping("/add_edit")
    public Result<Long> addOrEdit(@Validated @RequestBody HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        return Result.success(houseService.addOrEdit(houseAddOrEditReqDTO));
    }
}
