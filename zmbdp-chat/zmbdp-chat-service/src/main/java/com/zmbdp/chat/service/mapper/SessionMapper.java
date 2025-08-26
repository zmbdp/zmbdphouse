package com.zmbdp.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.chat.service.domain.entity.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
