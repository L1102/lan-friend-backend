package com.lan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lan.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lan
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}