package com.zmbdp.admin.service.timedtask.house;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.admin.service.house.domain.dto.HouseStatusEditReqDTO;
import com.zmbdp.admin.service.house.domain.entity.HouseStatus;
import com.zmbdp.admin.service.house.domain.enums.HouseStatusEnum;
import com.zmbdp.admin.service.house.mapper.HouseStatusMapper;
import com.zmbdp.admin.service.house.service.IHouseService;
import com.zmbdp.common.core.utils.TimestampUtil;
import com.zmbdp.common.redis.service.RedissonLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 刷新房源状态定时任务
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
@RefreshScope
public class ResetHouseStatusTimedTask {

    /**
     * 刷新房源状态锁的 key
     */
    private static final String LOCK_KEY = "rest:house:task:lock";

    /**
     * 房源状态映射表
     */
    @Autowired
    private HouseStatusMapper houseStatusMapper;

    /**
     * Redisson 分布式锁
     */
    @Autowired
    private RedissonLockService redissonLockService;

    /**
     * 房源服务
     */
    @Autowired
    private IHouseService houseService;

    /**
     * 定时任务：刷新房源状态
     */
    @Scheduled(cron = "*/10 * * * * ?")
//    @Scheduled(cron = "0 0 0 * * ?")
    public void resetHouseStatus() {
        log.info("开始执行定时任务：刷新房源状态");

        // 加 Redisson 分布式锁
        RLock rLock = redissonLockService.acquire(LOCK_KEY, 3, TimeUnit.SECONDS);
        if (null == rLock) {
            log.info("刷新房源状态定时任务被其他实例执行！");
            return;
        }

        try {
            // 查询全量已出租房源
            List<HouseStatus> rentingHouses = houseStatusMapper.selectList(new LambdaQueryWrapper<HouseStatus>()
                    .eq(HouseStatus::getStatus, HouseStatusEnum.RENTING.name())
            );

            // 过滤需要刷新状态的房源列表（出租到期时间）
            List<HouseStatus> needConvertList = rentingHouses.stream()
                    .filter(houseStatus -> null != houseStatus.getRentEndTime() && 0 > TimestampUtil.calculateDifferenceMillis(
                            TimestampUtil.getCurrentMillis(), houseStatus.getRentEndTime()
                    )).toList();

            // 修改状态
            for (HouseStatus houseStatus : needConvertList) {
                HouseStatusEditReqDTO houseStatusEditReqDTO = new HouseStatusEditReqDTO();
                houseStatusEditReqDTO.setHouseId(houseStatus.getHouseId());
                houseStatusEditReqDTO.setStatus(HouseStatusEnum.UP.name());
                // 然后再刷新缓存
                houseService.editStatus(houseStatusEditReqDTO);
            }
        } finally {
            redissonLockService.releaseLock(rLock);
        }
    }
}
