package com.zmbdp.admin.api.house.feign;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.api.house.domain.vo.HouseDetailVO;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.vo.BasePageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 房源服务远程调用 Api
 *
 * @author 稚名不带撇
 */
@FeignClient(contextId = "houseServiceApi", name = "zmbdp-admin-service", path = "/house")
public interface HouseServiceApi {

    /**
     * 查询房源列表，支持筛选、排序、翻页
     *
     * @param searchHouseListReqDTO 查询参数
     * @return 房源列表
     */
    @PostMapping("/list/search")
    Result<BasePageVO<HouseDetailVO>> searchList(@RequestBody SearchHouseListReqDTO searchHouseListReqDTO);


    /**
     * 查询房源详情（带缓存）
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    @GetMapping("/detail")
    Result<HouseDetailVO> detail(@RequestParam("houseId") Long houseId);
}
