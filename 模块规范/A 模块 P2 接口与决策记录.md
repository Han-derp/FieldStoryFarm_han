> 作者：A 模块（lyj）｜ 阶段：P2（v0.3.0-feature）｜ 编码：UTF-8
> 上位规则：《FSF游戏规则设计文档.md》（唯一数值事实源）
> 阶段红线：《FSF_P0-P4功能实现与验收规范.md》
> 工程结构：《脚手架.md》｜ 分工：《模块分工.md》第 4 行（A 模块 P2 = 持续世界引擎）
> 前置文档：《模块规范/A 模块 P1 接口与类设计文档.md》（决策编号衔接：P1 止于 D22）
> 决策记录：本文档 §1（P2 新决策自 D23 起）
> 本卡约束：只产本文档，未改任何 .java ✓

---

## 1. 文档信息

| 项    | 内容                     |
| ---- | ---------------------- |
| 模块名称 | A——土地与作物模块             |
| 负责角色 | lyj                    |
| 开发阶段 | P2 / v0.3.0-feature    |
| 文档版本 | v1.0-draft             |
| 日期   | 2026-09-14             |
| 状态   | 待团队确认（D27 口径、D30 转交卡） |
| 包名   | `com.fieldstory.farm`  |

**P2 负责内容**：持续世界引擎——`WorldTimeService`（时间口径与分段切点）、`WorldSimulationService`（分段成长 + 每日结算）、三件套/入参/摘要三个记录类。在线日结与离线模拟共用同一套领域逻辑（验收 §八十九），循环与切段由调用方（B 的 OfflineSimulationService）驱动。

**主要协作模块**：B（切段、驱动循环、组装三件套含装饰倍率、离线日志聚合）、D（WeatherService/EventService 注入、WeatherState/EventState 状态读取）、E（在线日结接线、DailyLog 落库）、C（成熟作物收获事务，A 不触碰）。

**依赖文档**：《FSF游戏规则设计文档》§五、§七、§十九~§二十三、§二十五、§二十八~§三十一、§四十七~§五十一、§八十一；《FSF_P0-P4功能实现与验收规范》§二十四、§二十五、§四十九、§五十、§八十三、§八十五~§九十一；《FSF项目需求分析与开发计划书.md》P0 三率恒 1.0 与成长公式；《模块规范/A 模块 P1 接口与类设计文档.md》；《docs/D模块-P2详细设计说明书.md》§二。

---

## 2. §1 决策记录表（P2 新决策 D23~D30）

> 编号接续任务卡口径：P1 止于 D22（注：仓库内 P1 文档决策表可见至 D20，D21/D22 未见于仓库文档，本文按任务卡口径自 D23 起编号）。

| 编号 | 事项 | 裁决 | 依据 |
| ---- | ---- | ---- | ---- |
| D23 | GrowthRates 记录类替代无限加参（P0/P1 兼容红线） | 成长倍率三件套打包为不可变 `record GrowthRates(weatherRate, decorationRate, eventRate)`，一次组装、多作物复用，避免 growSegment 入参随倍率种数无限膨胀；P0/P1 三率恒 1.0（常量 `GrowthRates.P0 = (1.0, 1.0, 1.0)`，P0/P1 兼容红线）；紧凑构造内建钳制：非法值（<0 或 NaN）一律钳制为 0（异常输入不破坏状态，取 0 只让成长暂停） | `service/GrowthRates.java` 源码；计划书 P0「三率恒 1.0」与成长公式六因子；验收 §二十四/§四十九 |
| D24 | A 只产 DailySimulationResult，落库归 B | `settleDay` 只返回摘要（§八十九 第⑨步 DailyLog 数据体），不收获、不出售、不动金币、不碰数据库与任何 DAO（验收 §八十六/§八十七）；DailyLog 落库由 E 的 DAO 完成，离线日志聚合归 B（模块分工第 5 行） | 验收 §八十六/§八十七/§八十九；《模块分工.md》第 4/5 行 |
| D25 | 分段精度：游戏小时逐段，切点=日边界+事件结束+作物成熟 | 段时长用 `double gameHours`（游戏小时，验收 §二十五 支持非整日成长，禁止 `offlineHours ÷ 24` 粗暴处理）；切点三要素 = 游戏日 00:00 边界 + 事件结束时间 + 作物成熟时间（验收 §八十八）；`WorldTimeService.segmentCutPoints` 输出含起止点的升序去重切点列表，事件结束为 null 则忽略 | 验收 §二十五/§八十八；`service/WorldTimeService.java` 源码 |
| D26 | 在线接入改造走转交卡（E/D 改自己的文件） | A 只交付 `WorldTimeService`/`WorldSimulationService` 与三个记录类；在线跨天接线改造（E 装配层 onDayChanged 替换 P1 简接线）与 D 侧依赖调整以转交卡形式提出，由 E/D 改自己的文件，A 不修改 E/D 任何 .java | 《模块分工.md》第 4/8 行；P1 文档 §8.2 先例（advanceCrops 3 参改造由 D/E 执行） |
| D27 | 雨天自动补水只增 rainCount 不动 manualWaterCount（口径，**待团队确认**） | 雨天补水效果：rainCount+1、lastHydratedWorldTime 更新、droughtStreak 重置；**不**增加 manualWaterCount、**不**加主动浇水成长 +5% / 品质 +3——雨天补水 ≠ 玩家主动浇水。补水计数的发生步骤口径（⑤⑥ 计数 vs ⑫ 计数）见 D30，本项与 D30 共同待团队确认 | 规则 §二十一；验收 §五十一 |
| D28 | 72h 封顶后剩余时长丢弃 | `capOfflineRealMinutes = min(raw, 72)`，负数/0 返回 0；超出 72 现实分钟的部分**不模拟、不补偿、不结转到下次离线**，直接丢弃 | 验收 §八十三；规则 §七 |
| D29 | settleDay 入参带 eventInEffect（当日生效事件，调用方读 EventState） | `DaySettlementInput.eventInEffect` 由调用方在**结算前**从 D 的 `EventState.getEventType()` 读取；§八十九 第⑨步 DailyLog 记录的是**当日生效事件**，非第⑬步次日抽取结果；`DailySimulationResult.event` 与入参同源 | 验收 §八十九 ⑨/⑬；`model/EventState.java` 只读用法；D P2 文档 §二 |
| D30 | ⑫ 雨天自动补水只写 lastHydratedWorldTime 不计数（计数仅在 ⑤⑥）（转交卡，**待团队确认**） | §八十九 第⑫步（次日新天气的补水）**只写** `lastHydratedWorldTime`，不计数；rainCount/greenRainCount/droughtStreak 一律在次日日结 ⑤⑥（`WitherService.recordDailyWeather`）计数（每雨日 +1 一次，验收 §五十「累计雨日数」口径）。本项为转交卡，待团队确认 | 验收 §八十九 ⑫/§五十；规则 §二十一；P1 文档 §5.2 分支表 |

---

## 3. §2 接口清单（签名照抄代码原文）

### 3.1 `service/WorldTimeService.java`（接口）

```java
/** 世界小时口径（决策 D14）：gameDay*24+gameHour，long。 */
long toWorldHour(int gameDay, int gameHour);

/** 离线结算上限（验收§八十三）：min(raw, 72)，负数/0 返回 0。 */
long capOfflineRealMinutes(long rawOfflineRealMinutes);

/** 分段切点（规则§八十三）：输入起止世界小时+事件结束+作物成熟时刻，
 *  输出含起止点的升序去重切点列表；事件结束为 null 则忽略。 */
List<Long> segmentCutPoints(long startWorldHour, long endWorldHour,
                            Long eventEndWorldHour,
                            List<Long> cropMatureWorldHours);
```

职责：统一世界时间口径（决策 D14：gameDay×24+gameHour，long）与离线结算封顶/分段切点计算。全部为纯函数：不读系统时间、不依赖 GameClock、不 new Random，仅依据入参计算。

### 3.2 `service/WorldSimulationService.java`（接口）

```java
/** 分段成长：全部 PLANTED 作物按 gameHours/24 折算天数成长，返回本段
 *  新成熟（成长进度跨过 100）的作物列表；不判枯萎、不换天气。 */
List<Crop> growSegment(Farm farm, double gameHours, GrowthRates rates);

/** 每日结算：严格按 §八十九 14 步顺序执行，返回当日摘要；
 *  禁止任何步骤重排或增减，循环由调用方驱动。 */
DailySimulationResult settleDay(Farm farm, DaySettlementInput input);
```

职责：在线每日结算与离线模拟共用同一套领域逻辑（验收 §八十九：禁止两套算法）。纯函数约束（D18/D19 精神延续）：不依赖 GameClock、不读系统时间、不调用 RandomProvider；只产数据（D24）。

### 3.3 `service/GrowthRates.java`（记录类）

```java
public record GrowthRates(double weatherRate, double decorationRate,
        double eventRate) {

    /** P0 占位倍率：三率全 1.0（验收规范 §二十四/§四十九，P0/P1 兼容红线） */
    public static final GrowthRates P0 = new GrowthRates(1.0, 1.0, 1.0);

    /** 紧凑构造：非法值（<0 或 NaN）一律钳制为 0 */
    public GrowthRates { weatherRate = clamp(weatherRate); ... }
}
```

职责：成长倍率三件套（计划书成长公式六因子中的 WeatherRate / DecorationRate / EventRate）。不可变记录：一次组装、多作物复用，避免跨天循环内重复构造；P0 占位倍率 `P0 = (1.0, 1.0, 1.0)`。

### 3.4 `service/DailySimulationResult.java`（记录类）

```java
public record DailySimulationResult(long gameDay, WeatherType weather,
                                    EventType event, int maturedCount,
                                    int witheredCount, int rainHydratedCount) {
}
```

职责：每日结算摘要 = §八十九 第⑨步 DailyLog 数据体。不可变记录：一次日结一份摘要，由调用方（B 模块 OfflineSimulationService）消费或持久化为离线日志（决策 D24：A 不碰数据库）。

### 3.5 `service/DaySettlementInput.java`（记录类）

```java
public record DaySettlementInput(long gameDay, long worldTimeAtSettle,
                                 WeatherType weather, EventType eventInEffect,
                                 GrowthRates rates,
                                 double witherMitigationRate,
                                 List<Double> witherRolls) {

    /** 紧凑构造：非法抗性倍率（<0 或 NaN）钳制为 0；掷骰列表防御性拷贝 */
    public DaySettlementInput { ... }
}
```

职责：一次日结的全部入参（验收 §八十九）。不可变记录 + 防御性钳制（GrowthRates 先例）：`witherRolls` 按农场遍历顺序消费，每次枯萎掷骰取下一个，取尽视为 1.0（必不枯萎，异常输入不破坏状态）。

---

## 4. §3 调用示例

### 4.1 B 离线循环（OfflineSimulationService，循环与切段归 B）

```text
// ===== 世界小时起点（D14 口径：gameDay*24+gameHour）=====
raw = gameClock.calculateOfflineDuration()            // D 的 GameClock，现实分钟
capped = worldTimeService.capOfflineRealMinutes(raw)  // D28：min(raw, 72)，负数/0→0
if (capped <= 0) 结束                                  // D28：超出 72 的部分直接丢弃
startWorldHour = worldTimeService.toWorldHour(gameDay, gameHour)
endWorldHour   = startWorldHour + capped              // 规则 §5.1：1 现实分钟 = 1 游戏小时

// ===== 切段（D25：切点 = 日边界 + 事件结束 + 作物成熟）=====
eventEnd = (eventState.getEventType() == NONE) ? null
         : eventState.getEndWorldTime()               // 读 D 的 EventState（只读）
matureHours = 逐株 PLANTED 作物推算「进度跨 100 的世界小时」列表
cutPoints = worldTimeService.segmentCutPoints(
        startWorldHour, endWorldHour, eventEnd, matureHours)   // 含起止点，升序去重

// ===== 逐段循环（§八十九 ⑭：继续下一段，循环由 B 驱动）=====
for (i = 0; i < cutPoints.size() - 1; i++):
    segStart = cutPoints[i]; segEnd = cutPoints[i + 1]
    hours = segEnd - segStart                          // D25：游戏小时逐段（double）
    rates = 组装 GrowthRates(
        weatherRate,      // 读 D WeatherState：晴 1.0/雨 1.5/旱 0.5/绿雨 2.0（规则 §十九）
        decorationRate,   // B 装饰 Buff 倍率（B 领地；未上线恒 1.0）
        eventRate)        // 读 D EventState：彩虹日 2.0，其余 1.0（D P2 文档 §二）
    matured = worldSimulationService.growSegment(farm, hours, rates)
                          // 段内只成长、不判枯萎、不换天气（验收 §八十八）

    if (segEnd % 24 == 0):                             // 到达日末切点（日边界）
        // D29 读取时机：必须在调 settleDay 之前读——
        // settleDay 内部 ⑪⑬ 会掷出新天气/新事件并覆盖 D 的 WeatherState/EventState
        eventInEffect = eventState.getEventType()      // 当日生效事件（§八十九⑨ 记录对象）
        weather       = weatherState.getWeatherType()  // 当日天气（昨日日末已掷出）
        input = new DaySettlementInput(
            gameDay,                 // 结算当日游戏日
            segEnd,                  // worldTimeAtSettle = 日末切点世界小时（D14 口径）
            weather,                 // 当日天气
            eventInEffect,           // D29：结算前读取的当日生效事件
            rates,                   // 当日三件套
            witherMitigationRate,    // B 石灯笼上线后传 0.7，否则 1.0（规则 §三十一）
            witherRolls)             // 按 Farm.getSoils() 遍历顺序消费，取尽视为 1.0
        result = worldSimulationService.settleDay(farm, input)
                          // 引擎内部已按 ⑧⑪⑬ 关闭过期事件/掷新天气/抽新事件
                          // 并写入 D 的 WeatherState/EventState
        logService.collect(result)   // 摘要交 B 的离线日志聚合（LogService，B 领地），
                          // 持久化经 E 的 DAO 落库（D24：A 不碰数据库）
        gameDay += 1
        // 下一段开始前：按 settleDay 掷出的新天气/新事件重新组装 rates

// ===== 循环结束后：B 统一推进 D 的 GameClock =====
//（A 不推进 D 的时钟——DailySimulationResult.gameDay 记录为结算当日，不负责推进）
```

### 4.2 E 在线日结（onDayChanged 跨天回调，与 4.1 共用同一服务）

```text
// E 装配层跨天回调（游戏日 00:00 边界触发；验收 §八十九：在线日结必须调用相同领域逻辑）
onDayChanged(day):
    worldTimeAtSettle = day * 24                    // 日末切点世界小时（D14 口径）
    // D29 读取时机：调 settleDay 之前读（⑪⑬ 会覆盖状态）
    eventInEffect = eventState.getEventType()       // 当日生效事件（读 D 的 EventState）
    weather       = weatherState.getWeatherType()   // 当日天气（昨日日末已掷出）
    rates = 组装 GrowthRates(weatherRate, decorationRate, eventRate)   // 同 4.1
    input = new DaySettlementInput(
        day, worldTimeAtSettle, weather, eventInEffect,
        rates, witherMitigationRate, witherRolls)   // witherRolls 经 RandomProvider 生成（D19）
    result = worldSimulationService.settleDay(farm, input)
                          // 与 4.1 是同一个服务：禁止 OnlineDailyService 另一套算法
    // D24：E 经 DAO 落库 DailyLog；随后 UI 刷新
    farmView.refreshAll()
```

> 在线白天的逐 tick 成长沿用 P1 已接线（D 的 advanceCrops → GrowthService.applyGrowth 3 参）；若在线期间需要跨日分段成长，复用 4.1 同款「segmentCutPoints → growSegment」循环（验收 §八十八），引擎与切段逻辑完全同一套。

---

## 5. §4 溯源说明

| 关键数值/规则 | 来源文档 | 章节 |
| --- | --- | --- |
| 72 现实分钟离线封顶（= 3 游戏日 = 72 游戏小时；超出不模拟、不补偿） | 《FSF游戏规则设计文档.md》§七「单次离线最大结算3游戏日」；《FSF_P0-P4功能实现与验收规范.md》§八十三「min(RawOfflineDuration, 72现实分钟)」 | 规则 §七；验收 §八十三 |
| 24 小时/日（1 现实分钟 = 1 游戏小时；24 现实分钟 = 24 游戏小时） | 《FSF游戏规则设计文档.md》§5.1「持续世界时间系统」 | 规则 §5.1 |
| 世界小时口径 gameDay×24+gameHour（long） | 《FSF项目需求分析与开发计划书.md》决策记录 | D14 |
| 14 步每日结算顺序 ①~⑭（含 ⑫ 雨天自动补水、⑬ 抽取当天 Event、⑭ 继续下一段） | 《FSF_P0-P4功能实现与验收规范.md》§八十九「P2每日离线顺序」 | 验收 §八十九 |
| 游戏日 00:00 为世界结算边界（规则侧日结流程） | 《FSF游戏规则设计文档.md》§八十一「游戏日边界」 | 规则 §八十一 |
| 三率（WeatherRate / DecorationRate / EventRate）六因子成长公式；P0 三率恒 1.0 | 《FSF项目需求分析与开发计划书.md》P0「世界环境固定」与「成长公式」GrowthDelta = 基础每日成长进度 × 时间比例 × WeatherRate × DecorationRate × OperationRate × EventRate | 计划书 P0/成长公式 |
| WeatherRate 四态：晴 1.0 / 雨 1.5 / 旱 0.5 / 绿雨 2.0 | 《FSF游戏规则设计文档.md》§十九「天气」；《FSF_P0-P4功能实现与验收规范.md》§四十九「P1 WeatherRate」 | 规则 §十九；验收 §四十九 |
| EventRate：彩虹日 2.0，其余 1.0 | 《FSF游戏规则设计文档.md》§五十一（彩虹日 EventRate×2）；《docs/D模块-P2详细设计说明书.md》§二 | 规则 §五十一；D P2 文档 §二 |
| DecorationRate：P1 装饰上线后加入 × DecorationRate | 《FSF_P0-P4功能实现与验收规范.md》§四十九 | 验收 §四十九 |
| 非整日成长（gameHours/24 折算，禁止 ÷24 粗暴处理） | 《FSF_P0-P4功能实现与验收规范.md》§二十五「P0成长必须支持非整日」、§八十八「P2离线模拟分段」 | 验收 §二十五/§八十八 |
| 切段三要素：游戏日 00:00 边界 / 事件结束时间 / 作物成熟时间 | 《FSF_P0-P4功能实现与验收规范.md》§八十八 | 验收 §八十八 |
| 雨天自动补水口径：rainCount+1、lastHydratedTime 更新、streak 重置；不加 manualWaterCount、不加浇水 Buff | 《FSF游戏规则设计文档.md》§二十一「雨天」；《FSF_P0-P4功能实现与验收规范.md》§五十一 | 规则 §二十一；验收 §五十一 |
| 枯萎抗性 ×0.7（石灯笼，只降枯萎概率） | 《FSF游戏规则设计文档.md》§三十一「石灯笼抗性」 | 规则 §三十一 |
| 每日最多 1 事件、一次随机抽取（74/5/8/10/3） | 《FSF游戏规则设计文档.md》§四十七；《FSF_P0-P4功能实现与验收规范.md》§九十 | 规则 §四十七；验收 §九十 |
| 成熟不自动出售、玩家返回后亲自收获 | 《FSF_P0-P4功能实现与验收规范.md》§八十七「P2成熟作物」 | 验收 §八十七 |
| 离线期间禁止主动浇水/施肥/购买/移动装饰/收获 | 《FSF_P0-P4功能实现与验收规范.md》§八十六「P2离线禁止行为」 | 验收 §八十六 |
| A 模块 P2 = 持续世界引擎；B 模块 P2 = 离线模拟+离线日志 | 《模块分工.md》 | 第 4/5 行 |

---

## 6. 待确认事项与风险

| # | 事项 | 影响 | 状态 |
| --- | --- | --- | --- |
| 1 | **D30 转交卡**：⑫ 雨天自动补水只写 lastHydratedWorldTime 不计数，计数仅在次日 ⑤⑥ recordDailyWeather（每雨日 +1 一次） | 雨天补水计数口径（验收 §五十「累计雨日数」） | **待团队确认**（转交卡） |
| 2 | **D27 口径**：雨天补水只增 rainCount、不动 manualWaterCount、不加浇水 Buff（规则 §二十一/验收 §五十一字面）与 D30 的「⑫ 不计数」配套口径 | 与 D 模块 P2 文档雨天补水描述的一致性 | **待团队确认** |
| 3 | D26 在线接入改造（onDayChanged 替换 P1 简接线）动 E/D 文件 | 集成进度依赖 E/D 接受转交卡 | 转交 E（hyt）、D（zsl） |
| 4 | 决策编号衔接：仓库内 P1 文档决策表可见至 D20，D21/D22 未见于仓库文档 | 编号连续性 | 本文按任务卡口径自 D23 起，请团队确认编号衔接 |

---

## 7. 变更记录

| 版本 | 日期 | 说明 |
| --- | --- | --- |
| v1.0-draft | 2026-09-14 | P2 初稿：D23~D30 决策、五个接口/记录类签名（照抄代码原文）、B 离线循环与 E 在线日结调用示例、72/24/14 步/三率溯源说明；D27/D30 标注待团队确认 |
