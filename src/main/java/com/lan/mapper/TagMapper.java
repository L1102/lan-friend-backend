package com.lan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lan.model.domain.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lan
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}