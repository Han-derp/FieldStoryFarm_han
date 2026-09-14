package com.fieldstory.farm.service;

import com.fieldstory.farm.model.OfflineSimulationResult;

/**
 * B 模块 P2 离线模拟正式入口。
 *
 * <p>B 负责离线时间窗口编排：复用 {@link WorldTimeService} 执行 72 分钟封顶与
 * 日界/事件结束/作物成熟三类切点计算，并驱动循环调用 A 模块
 * {@link WorldSimulationService}。
 *
 * <p>职责红线：本 Service 不拥有独立的成长、天气、枯萎或事件算法；不执行主动浇水、
 * 主动施肥、购买、移动装饰、主动收获或自动出售；不访问数据库。
 */
public interface OfflineSimulationService {

    /**
     * 执行一次离线模拟。
     *
     * <p>1 现实分钟 = 1 游戏小时；有效时长由 {@link WorldTimeService} 封顶为
     * 72 现实分钟（最多 72 游戏小时 / 3 游戏日）。
     *
     * @param rawOfflineMinutes 实际离线现实分钟数
     * @return 本次离线模拟的结构化结果
     */
    OfflineSimulationResult simulate(long rawOfflineMinutes);
}
