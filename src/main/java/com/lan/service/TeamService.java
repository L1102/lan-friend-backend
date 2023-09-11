package com.lan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lan.model.domain.User;
import com.lan.model.dto.TeamQuery;
import com.lan.model.request.TeamJoinRequest;
import com.lan.model.request.TeamQuitRequest;
import com.lan.model.request.TeamUpdateRequest;
import com.lan.model.vo.TeamUserVO;

import java.util.List;

/**
 * @Author lan
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    Page<TeamUserVO> listTeams(long currentPage,TeamQuery teamQuery, boolean isAdmin);

    Page<TeamUserVO> getTeamHahJoinNum(Page<TeamUserVO> teamUserVOPage);

    /**
     * 根据用户id查询用户创建的队伍
     * @param userId
     * @return
     */
    List<TeamUserVO> listMyCreateTeams(Long userId);

    /**
     * 获取用户加入的队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listMyJoinTeams(TeamQuery teamQuery);

    TeamUserVO getTeam(Long teamId, Long userId);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loggedUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long teamId, User loginUser);

    Page<TeamUserVO> getUserJoinNum(User loginUser, Page<TeamUserVO> teamHahJoinNum);
}
