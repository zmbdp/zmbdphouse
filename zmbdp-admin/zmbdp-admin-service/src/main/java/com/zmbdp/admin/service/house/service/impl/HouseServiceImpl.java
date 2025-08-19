package com.zmbdp.admin.service.house.service.impl;

import com.zmbdp.admin.service.house.domain.dto.HouseAddOrEditReqDTO;
import com.zmbdp.admin.service.house.service.IHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 房源服务业务层
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private IHouseService houseService;


    /**
     * 添加或编辑房源
     *
     * @param houseAddOrEditReqDTO 房源信息
     * @return 房源 ID
     */
    @Override
    public Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        // 根据是否存在房源 id 判断是添加还是编辑
        // TODO: 得做逻辑
        return 0L;
    }
}
