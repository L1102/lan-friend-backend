package com.lan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lan.model.domain.UserTeam;
import com.lan.service.UserTeamService;
import com.lan.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
 * @author lan
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {

}
