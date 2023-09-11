package com.lan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lan.model.domain.User;
import com.lan.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author lan
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<User> getRandomUser();
    // UserVO toUserVO(User user);
}