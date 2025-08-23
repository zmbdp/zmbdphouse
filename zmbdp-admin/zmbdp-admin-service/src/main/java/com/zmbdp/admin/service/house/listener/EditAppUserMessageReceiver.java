package com.zmbdp.admin.service.house.listener;

import com.zmbdp.admin.api.appuser.domain.dto.AppUserDTO;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.admin.service.user.config.RabbitConfig;
import com.zmbdp.common.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(), // 不指定生成匿名队列，这么做不需要担心冲突，并且没人连接队列会自动删除
        exchange = @Exchange(value = RabbitConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT))
})
public class EditAppUserMessageReceiver {
    @Autowired
    private IHouseService houseService;

    @RabbitHandler
    public void process(AppUserDTO appUserDTO) {
        if (null == appUserDTO || null == appUserDTO.getUserId()) {
            log.error("MQ接收到的用户修改消息为空或用户id为空！");
            return;
        }

        log.info("MQ成功接收到消息，message:{}", JsonUtil.classToJson(appUserDTO));

        try {
            // 1. 获取用户下房源 id 列表
            List<Long> houseIds = houseService.listByUserId(appUserDTO.getUserId());

            // 2. 更新用户下全量房源列表的缓存
            for (Long houseId : houseIds) {
                houseService.cacheHouse(houseId);
            }
        } catch (Exception e) {
            log.error("处理用户更新时，更新房源缓存异常，appUserDTO:{}", JsonUtil.classToJson(appUserDTO), e);
        }

    }

}