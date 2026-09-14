package com.fieldstory.farm.service;

import com.fieldstory.farm.model.EventType;
import com.fieldstory.farm.model.WeatherType;

import java.util.List;

/**
 * 一次日结的全部入参（A 模块 P2 持续世界引擎；验收规范 §八十九）。
 *
 * <p>不可变记录 + 防御性钳制（{@link GrowthRates} 先例）：非法输入不破坏状态。
 *
 * @param gameDay              结算当日游戏日（从 1 开始）
 * @param worldTimeAtSettle    日末切点世界时间（游戏小时 = 游戏日 × 24 + 小时，
 *                             决策 D14 口径；§八十九 第 ④ 步由调用方保证为日末切点）
 * @param weather              当日天气（昨日日末已掷出，调用方读取 D 的 WeatherState）
 * @param eventInEffect        当日生效事件（决策 D29：调用方在结算前从 D 的
 *                             EventState.getEventType() 读取；§八十九⑨ DailyLog
 *                             记录对象）
 * @param rates                当日成长倍率三件套（调用方按天气/装饰/事件组装；
 *                             彩虹日 EventRate = 2.0，D 模块 P2 文档 §二）
 * @param witherMitigationRate 枯萎抗性倍率（规则文档 §三十一；P1 恒传 1.0，
 *                             B 石灯笼上线后传 0.7）
 * @param witherRolls          枯萎掷骰值列表：按农场遍历顺序消费，每次枯萎掷骰取下一个，
 *                             取尽视为 1.0（必不枯萎，异常输入不破坏状态）
 */
public record DaySettlementInput(long gameDay, long worldTimeAtSettle,
                                 WeatherType weather, EventType eventInEffect,
                                 GrowthRates rates,
                                 double witherMitigationRate,
                                 List<Double> witherRolls) {

    /** 紧凑构造：非法抗性倍率（&lt;0 或 NaN）钳制为 0（GrowthRates 钳制先例）；掷骰列表防御性拷贝 */
    public DaySettlementInput {
        witherMitigationRate = clamp(witherMitigationRate);
        witherRolls = witherRolls == null ? List.of() : List.copyOf(witherRolls);
    }

    /** 非法倍率钳制：&lt;0 或 NaN → 0（与 {@link GrowthRates} 同一约定）。 */
    private static double clamp(double rate) {
        return (rate < 0 || Double.isNaN(rate)) ? 0.0 : rate;
    }
}
