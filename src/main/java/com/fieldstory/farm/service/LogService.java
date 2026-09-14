package com.fieldstory.farm.service;

import com.fieldstory.farm.model.OfflineLog;
import com.fieldstory.farm.model.OfflineSimulationResult;

import java.util.Optional;

/**
 * B 模块 P2 离线日志服务。
 *
 * <p>职责仅为把结构化离线事实转换为玩家可读叙事；不执行世界模拟、不访问数据库。
 */
public interface LogService {

    /**
     * 根据一次离线模拟结果生成返回日志。
     *
     * @return effectiveOfflineMinutes == 0 时返回 Optional.empty()
     */
    Optional<OfflineLog> buildOfflineLog(OfflineSimulationResult result);
}
