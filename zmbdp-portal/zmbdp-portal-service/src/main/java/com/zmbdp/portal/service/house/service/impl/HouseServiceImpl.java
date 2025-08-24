package com.zmbdp.portal.service.house.service.impl;

import com.zmbdp.admin.api.house.domain.vo.HouseDetailVO;
import com.zmbdp.admin.api.house.feign.HouseServiceApi;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.portal.service.house.domain.vo.HouseDataVO;
import com.zmbdp.portal.service.house.service.IHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 房源服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private HouseServiceApi houseServiceApi;

    /**
     * C端 根据房源 id 查询房源详情
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    @Override
    public HouseDataVO houseDetail(Long houseId) {
        // 判空
        if (null == houseId || houseId < 0) {
            log.warn("要查询的房源id为空或无效！");
            throw new ServiceException(ResultCode.INVALID_PARA);
        }
        // 调用远程接口
        Result<HouseDetailVO> houseDetailResult = houseServiceApi.detail(houseId);
        if (
                null == houseDetailResult ||
                houseDetailResult.getCode() != ResultCode.SUCCESS.getCode() ||
                null == houseDetailResult.getData()
        ) {
            log.error("查询房源详情失败！");
            return null;
        }
        // 查询到了开始拷贝
        HouseDataVO houseDataVO = new HouseDataVO();
        BeanCopyUtil.copyProperties(houseDetailResult.getData(), houseDataVO);
        return houseDataVO;
    }
}
