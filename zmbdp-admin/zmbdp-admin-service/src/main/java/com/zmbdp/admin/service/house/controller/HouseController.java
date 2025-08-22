package com.zmbdp.admin.service.house.controller;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.api.house.domain.vo.HouseDetailVO;
import com.zmbdp.admin.api.house.feign.HouseServiceApi;
import com.zmbdp.admin.service.house.domain.dto.*;
import com.zmbdp.admin.service.house.domain.vo.HouseVO;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.common.core.domain.dto.BasePageDTO;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.vo.BasePageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 房源管理服务
 *
 * @author 稚名不带撇
 */
@Slf4j
@RestController
@RequestMapping("/house")
public class HouseController implements HouseServiceApi {

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

    /**
     * 查询房源详情（带缓存）
     *
     * @param houseId 房源 id
     * @return 房源详情 DTO
     */
    @Override
    public Result<HouseDetailVO> detail(Long houseId) {
        HouseDTO houseDTO = houseService.detail(houseId);
        if (null == houseDTO) {
            log.warn("要查询的房源不存在，houseId: {}", houseId);
            return Result.fail("房源详情不存在！");
        }
        return Result.success(houseDTO.convertToVO());
    }

    /**
     * 查询房源摘要列表（支持翻页、支持筛选）
     *
     * @param houseListReqDTO 查询参数
     * @return 房源摘要列表
     */
    @PostMapping("/list")
    public Result<BasePageVO<HouseVO>> list(@Validated @RequestBody HouseListReqDTO houseListReqDTO) {
        BasePageDTO<HouseDescDTO> houseDescList = houseService.list(houseListReqDTO);
        BasePageVO<HouseVO> result = new BasePageVO<>();
        BeanCopyUtil.copyProperties(houseDescList, result);
        return Result.success(result);
    }

    /**
     * 更新房源状态
     *
     * @param houseStatusEditReqDTO 房源状态修改参数
     * @return 修改结果
     */
    @PostMapping("/status/edit")
    public Result<?> editStatus(@Validated @RequestBody HouseStatusEditReqDTO houseStatusEditReqDTO) {
        houseService.editStatus(houseStatusEditReqDTO);
        return Result.success();
    }


//    /**
//     * 刷新房源缓存
//     */
//    @GetMapping("/refresh")
//    public Result<Void> refreshHouseIds() {
//        houseService.refreshHouseIds();
//        return Result.success();
//    }

    /**
     * 查询房源列表，支持筛选、排序、翻页
     *
     * @param searchHouseListReqDTO 查询参数
     * @return 房源列表
     */
    @Override
    public Result<BasePageVO<HouseDetailVO>> searchList(@Validated SearchHouseListReqDTO searchHouseListReqDTO) {
//        BasePageVO<HouseDetailVO> result = new BasePageVO<>();
//        BasePageDTO<HouseDTO> searchDTO =  houseService.searchList(searchHouseListReqDTO);
//        result.setTotals(searchDTO.getTotals());
//        result.setTotalPages(searchDTO.getTotalPages());
//        result.setList(BeanCopyUtil.copyListProperties(searchDTO.getList(), HouseDetailVO::new));
//        return Result.success(result);
        return Result.success();
    }
}
