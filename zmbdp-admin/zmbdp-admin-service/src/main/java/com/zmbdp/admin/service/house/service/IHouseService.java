package com.zmbdp.admin.service.house.service;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.*;
import com.zmbdp.common.core.domain.dto.BasePageDTO;

import java.util.List;

/**
 * 房源服务业务层接口
 *
 * @author 稚名不带撇
 */
public interface IHouseService {

    /**
     * 添加或编辑房源
     *
     * @param houseAddOrEditReqDTO 房源信息
     * @return 房源 ID
     */
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);

    /**
     * 更新房源缓存
     *
     * @param houseId 房源 ID
     */
    void cacheHouse(Long houseId);

    /**
     * 查询房源详情（带缓存）
     *
     * @param houseId 房源 id
     * @return 房源详情 DTO
     */
    HouseDTO detail(Long houseId);

    /**
     * 查询房源摘要列表（支持翻页、支持筛选）
     *
     * @param houseListReqDTO 查询参数
     * @return 房源摘要列表
     */
    BasePageDTO<HouseDescDTO> list(HouseListReqDTO houseListReqDTO);

    /**
     * 更新房源状态
     *
     * @param houseStatusEditReqDTO 房源状态修改参数
     */
    void editStatus(HouseStatusEditReqDTO houseStatusEditReqDTO);

    /**
     * 根据房东 id 查询其下房源 id 列表
     *
     * @param userId 房东 id
     * @return 房源 id 列表
     */
    List<Long> listByUserId(Long userId);

    /**
     * 刷新房源缓存
     */
    void refreshHouseIds();

    /**
     * 根据城市 id 和排序规则查询房源列表，支持筛选、排序、翻页
     *
     * @param searchHouseListReqDTO 查询参数
     * @return 房源列表
     */
    BasePageDTO<HouseDTO> searchList(SearchHouseListReqDTO searchHouseListReqDTO);
}
