package com.zmbdp.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.admin.service.house.domain.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
