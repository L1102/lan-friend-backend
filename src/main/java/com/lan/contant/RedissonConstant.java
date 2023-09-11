package com.lan.contant;

/**
 * redisson常量
 * @author lan
 */
public interface RedissonConstant {

    /**
     * 应用锁
     */
    String APPLY_LOCK = "lan:apply:lock:";

    String DISBAND_EXPIRED_TEAM_LOCK = "lan:disbandTeam:lock";

    String USER_RECOMMEND_LOCK = "lan:user:recommend:lock";
}
