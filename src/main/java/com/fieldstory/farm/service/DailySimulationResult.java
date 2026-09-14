package com.fieldstory.farm.service;

import com.fieldstory.farm.model.EventType;
import com.fieldstory.farm.model.WeatherType;

/**
 * 每日结算摘要（A 模块 P2 持续世界引擎；验收规范 §八十九 第 ⑨ 步 DailyLog 的数据体；
 * 决策 D24：A 只产数据、不碰数据库与任何 DAO）。
 *
 * <p>不可变记录：一次日结一份摘要，由调用方（B 模块 OfflineSimulationService）
 * 消费或持久化为离线日志。
 *
 * @param gameDay           结算当日游戏日（§八十九 第 ⑩ 步：结果记录为结算当日，
 *                          A 不负责推进 D 的时钟）
 * @param weather           当日天气（昨日日末已掷出，与 {@link DaySettlementInput#weather()} 同源）
 * @param event             当日生效事件（决策 D29：与
 *                          {@link DaySettlementInput#eventInEffect()} 同源；
 *                          §八十九⑨ DailyLog 记录的是当日事件，非次日抽取结果）
 * @param maturedCount      当日成熟作物数（progress ≥ 100；不收获、不自动出售，
 *                          验收规范 §八十七）
 * @param witheredCount     当日枯萎作物数（§八十九 第 ⑦ 步判定触发数）
 * @param rainHydratedCount 当日雨天自动补水作物数（当日天气为 RAIN 时全部 PLANTED
 *                          作物获补水，规则文档 §二十一；第 ⑫ 步为下一日的补水，
 *                          其计数体现在下一日结算的摘要）
 */
public record DailySimulationResult(long gameDay, WeatherType weather,
                                    EventType event, int maturedCount,
                                    int witheredCount, int rainHydratedCount) {
}
