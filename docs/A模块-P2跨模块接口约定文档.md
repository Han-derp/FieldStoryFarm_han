# A 模块 P2 跨模块接口约定文档（持续世界引擎）

> 版本：P2 · v1.0
> 负责人：A（lyj）· 土地与作物模块
> 依据：《FSF_P0-P4功能实现与验收规范》§八十八/§八十九、《概要设计说明书-lyj.md》§6.3、《模块分工.md》第 4 行、决策 D14/D18/D19/D24/D29/D30
> 约束：A 模块**不修改** B/C/D/E 模块的类，本文件仅提出接口约定，需其他模块修改的部分以本文档形式提出。
> 冻结声明：`WorldSimulationService` 两个方法签名自本文档发布起冻结，任何调整须先在团队同步，不得单方变更。

---

## 一、A 模块 P2 对外接口清单（已实现，含单测）

| 接口/记录 | 签名 | 说明 |
|---|---|---|
| `WorldSimulationService` | `List<Crop> growSegment(Farm farm, double gameHours, GrowthRates rates)` | 分段成长：全部 PLANTED 作物按 `gameHours / 24` 折算天数成长，返回本段新成熟作物；不判枯萎、不换天气（验收 §八十八/§二十五） |
| `WorldSimulationService` | `DailySimulationResult settleDay(Farm farm, DaySettlementInput input)` | 每日结算：严格按 §八十九 14 步执行，返回当日摘要；只产数据（决策 D24） |
| `DaySettlementInput` | `record(long gameDay, long worldTimeAtSettle, WeatherType weather, EventType eventInEffect, GrowthRates rates, double witherMitigationRate, List<Double> witherRolls)` | 一次日结的全部入参；不可变记录 + 防御性钳制 |
| `DailySimulationResult` | `record(long gameDay, WeatherType weather, EventType event, int maturedCount, int witheredCount, int rainHydratedCount)` | 当日结算摘要 = §八十九 第⑨步 DailyLog 数据体 |
| `GrowthRates` | `record(double weatherRate, double decorationRate, double eventRate)` | 成长倍率三件套；`P0 = 全 1.0`；非法值钳制为 0 |

实现：`BasicWorldSimulationService`（210 行）；单测 `BasicWorldSimulationServiceTest`（10 用例，2026-09-14 实测全绿）。

**纯函数约束（决策 D18/D19 精神延续）**：引擎不依赖 `GameClock`、不读系统时间、不调用 `RandomProvider`——时间与掷骰值全部由调用方传入；天气/事件抽取由注入的 D 模块 `WeatherService`/`EventService` 内部完成。

**只产数据（决策 D24）**：不收获、不出售、不动金币（验收 §八十六/§八十七）、不碰数据库与任何 DAO；不推进 D 的时钟。

---

## 二、与 B 模块（玩家与经营）接口约定 —— B-P2-1 唯一消费协议

### 2.1 冻结结论：不需要新签名

B 提案 `WorldSimulationResult advanceHours(long gameHours)` **不采纳**，理由：

1. 架构已按《概要设计说明书》§6.3 冻结：**B 切段、B 驱动循环、A 只执行当前段/当日**，且验收 §八十九明确「OfflineSimulationService 只是负责按时间窗口调用同一套世界模拟规则」；
2. `advanceHours` 把切段挪进 A，与已冻结的「循环由调用方（B 模块 OfflineSimulationService）驱动」冲突；
3. 要求引擎内部执行 Memory 违反决策 D24（A 不碰 DAO），Memory 归 B/E；
4. 已冻结签名的段时长是 `double gameHours`（验收 §二十五支持非整日成长），`long` 参数口径不一致。

B-P2-1 直接消费下述已冻结接口即可，无需新合同。

### 2.2 B 侧调用协议（照抄）

```text
gameHours = min(rawOfflineMinutes, 72)        // 72 现实分钟上限（概要设计 §6.3）；
                                              // 1 现实分钟 = 1 游戏小时（规则 §5.1）
while (gameHours > 0):
    segment = 按 min(下一 00:00 边界, 下一事件结束时间, 下一作物成熟时间) 切段（验收 §八十八）
    growSegment(farm, segment.hours, rates)    // 段内成长；gameHours 为 double
    if segment 到达日末切点:
        input = DaySettlementInput(
            gameDay,              // 当日游戏日（读 D 的 GameClock.getGameDay()）
            gameDay * 24,         // worldTimeAtSettle = 日末切点世界小时（决策 D14 口径）
            当日天气,              // 读 D 的 WeatherState
            当日事件,              // 读 D 的 EventState（决策 D29：结算前读取）
            rates,                // 天气/装饰(B)/事件 三件套，B 组装
            witherMitigationRate, // B 石灯笼 0.7，否则 1.0（规则 §三十一）
            witherRolls           // 按 Farm.getSoils() 遍历顺序，取尽视为 1.0
        )
        result = settleDay(farm, input)
        // 引擎内部已完成 §八十九 ⑪⑬：新天气/新事件已写入 D 的 WeatherState/EventState
        收集 result 为当日离线日志（B 组装数据，E 提供 DAO 落库）
    gameHours -= segment.hours
循环结束后推进 GameClock                        // A 不推进 D 的时钟，归 B（tick/advance/setTotalMinutes）
```

### 2.3 B 的职责边界（缺一不可）

| 职责 | 说明 | 来源 |
|---|---|---|
| 切段 | 00:00 边界 / 事件结束 / 作物成熟时间 | 验收 §八十八 |
| 组装入参 | 倍率三件套含装饰倍率（装饰/Buff 是 B 的领域）；witherRolls 经 D 的 RandomProvider 生成 | 验收 §四十九、决策 D18/D19 |
| 循环驱动 | 反复调用 growSegment/settleDay，直至窗口耗尽 | 概要设计 §6.3 |
| 时钟推进 | 模拟结束后推进 GameClock（D 模块） | 引擎 javadoc 冻结约定 |
| 离线日志 | B P2 领地：聚合 DailySimulationResult 为离线日志，持久化经 E 的 DAO | 模块分工第 5 行 |
| 禁止两套算法 | 不得实现第二套成长/天气/事件公式 | 验收 §八十九 |

### 2.4 时间类型口径（回应 B 的时间类型关切）

- B 公开输入只保留 `long rawOfflineMinutes` —— **成立**。取值来源为 D 的 `GameClock.calculateOfflineDuration()`（返回 `long` 现实分钟，规则 §八/§九），B 不接触 `LocalDateTime`、不自造第二套世界时间类型。
- 世界时间统一口径（决策 D14）：`worldTimeAtSettle = gameDay * 24 + gameHour`（`long` 游戏小时），与 `Crop.plantWorldTime`/`lastHydratedWorldTime` 同一口径。
- 即使后续团队统一世界时间表达，B 的 `OfflineSimulationService` 输入 `rawOfflineMinutes` 无需推翻。

---

## 三、与 D 模块（世界环境）接口约定

| 约定项 | 内容 | 来源 |
|---|---|---|
| 引擎依赖 | `BasicWorldSimulationService` 构造注入 D 的 `WeatherService`/`EventService` | 验收 §八十九 |
| 天气/事件生成 | 引擎日末调 `rollDailyWeather(dayIndex)`/`rollDailyEvent(dayIndex)`，结果由 D 的实现写入 `WeatherState`/`EventState` | 验收 §八十九 ⑪⑬ |
| 事件关闭 | 引擎调 `EventService.expireIfNeeded(worldTimeAtSettle)` | §八十九 ⑧ |
| 状态读取 | B 从 D 的 `WeatherState`/`EventState` 读当日天气/事件（D29） | 验收 §八十九 |
| 时间口径 | `worldTimeAtSettle = gameDay × 24`（决策 D14）；引擎不读 `GameClock` | 决策 D14/D18 |
| 离线时长 | B 的 `rawOfflineMinutes` 取 D 的 `GameClock.calculateOfflineDuration()` | 规则 §八/§九 |
| 世界时间来源 | D 文档矛盾 4 的适配口径（`gameDay*24 + gameHour`）与 A 侧 `plantWorldTime` 一致，引擎按同一口径传参 | D 文档 §六 矛盾 4 |

---

## 四、与 E 模块（存档）接口约定

| 约定项 | 内容 | 来源 |
|---|---|---|
| DailyLog 落库 | `DailySimulationResult` 是 DailyLog 数据体（§八十九 ⑨），落库由 E 的 DAO 完成（决策 D24：A 不碰 DAO） | 验收 §八十九 |
| 离线日志 | B 生成离线日志数据，持久化经 E；`world_state.last_real_time` 已在 P1 写库，供 B 计算离线时长 | 模块分工第 5/8 行 |
| 恢复语义 | 读档只恢复退出瞬间状态，离线成长由 B 的 OfflineSimulationService 在恢复后执行 | 验收 §八十五 |

---

## 五、与 C 模块（品质与传说）边界

A 引擎**不收获、不出售**（验收 §八十六/§八十七）：离线成熟作物停留在 `MATURE`，等待玩家上线后由 C 模块收获事务处理。A 引擎不触碰品质/传说计算。

---

## 六、跨模块矛盾与待裁定项（强制报告）

> 依据会话约束「发现文档间矛盾时立即停止并报告，禁止自行选择其中一种」。

### 裁定 1：`lastHydratedTime` 字段类型（D 文档矛盾 1，A 已裁定）

D 模块 P2 文档 §六 矛盾 1 将待裁定人指向 A（lyj）。**A 裁定结果**：`Crop` 模型已按决策 D14 全部统一为 `long`——`getLastHydratedWorldTime()`（游戏小时，-1 表示无记录，决策 D16），代码中不存在 `LocalDateTime` 字段。**状态：A 已裁定，请团队确认关闭该矛盾。**

### 说明 2：天气/事件生成时机的阶段归属（D 文档矛盾 2）

D 文档将矛盾 2 的待裁定人指向团队。**A 侧现状**：`settleDay` 内部按 §八十九 顺序调用 D 的 `rollDailyWeather`/`rollDailyEvent`，A 的引擎即 D 文档所述「上层协调器」的落地形态。**状态：A 已实现，请团队确认。**

### 待裁定 3：D30 雨天补水计数口径（转交卡）

A 实现（`BasicWorldSimulationService` §八十九 第⑫步）按 D30：只写 `lastHydratedWorldTime`（补水不计数），`rainCount`/`greenRainCount`/`streak` 一律在次日日结 ⑤⑥ `recordDailyWeather` 计数（每雨日 +1 一次，验收 §五十「累计雨日数」口径）。**状态：待团队确认。**

### 说明 4：D29 事件摘要口径

A 实现：`settleDay` 返回值 `event` 为当日生效事件（与入参 `eventInEffect` 同源），次日新抽取事件不进当日摘要；次日 `EventRate` 由 B 读 `EventState` 组装。**状态：A 已按此实现，请 D 确认一致。**

---

## 七、溯源说明

| 关键约定/数值 | 来源文档 | 章节 |
|---|---|---|
| 切段三要素：日边界/事件结束/成熟时间 | 《FSF_P0-P4功能实现与验收规范.md》 | §八十八 |
| 每日离线 14 步顺序 | 《FSF_P0-P4功能实现与验收规范.md》 | §八十九 |
| 禁止两套算法、共用 WorldSimulationService | 《FSF_P0-P4功能实现与验收规范.md》 | §八十九 |
| 在线/离线一套规则、B 反复调用引擎 | 《概要设计说明书-lyj.md》 | §6.3 |
| 72 现实分钟上限 | 《概要设计说明书-lyj.md》 | §6.3 |
| 1 现实分钟 = 1 游戏小时 | 《FSF游戏规则设计文档.md》 | §5.1 |
| 石灯笼枯萎抗性 0.7 | 《FSF游戏规则设计文档.md》 | §三十一 |
| 世界时间统一 long（D14） | 《FSF项目需求分析与开发计划书.md》 | 决策 D14 |
| A 只产数据不碰 DAO（D24） | 《FSF项目需求分析与开发计划书.md》 | 决策 D24 |
| 引擎不读 GameClock/系统时间/RandomProvider | 《FSF项目需求分析与开发计划书.md》 | 决策 D18/D19 |
| A 模块 P2 = 持续世界引擎、B 模块 P2 = 离线模拟+离线日志 | 《模块分工.md》 | 第 4/5 行 |
