# D 模块 P2 离线模拟接口约定文档（随机事件系统）

> 版本：P2 · v0.1.0
> 负责人：D（zsl）· 世界环境模块
> 依据：《游戏规则设计文档》§四十七~§五十一、《FSF_P0-P4功能实现与验收规范》§八十五/§八十八/§八十九/§九十/§九十一/§九十二、《模块分工.md》第 5/7 行、《D模块-P2跨模块接口约定文档.md》§五
> 约束：D 模块**不修改** A/C/B/E 模块的类，本文件仅提出接口约定，需其他模块修改的部分以本文档形式提出。

---

## 一、目的与范围

本文件是 D 模块 P2 随机事件系统与**离线模拟**相关的接口约定，回答两个问题：

1. **B 模块的离线模拟如何复用 D 的规则**（L4.1）；
2. **`WorldSimulationService` 如何把 `EventService` 作为依赖接入日结**（L4.2）。

**D 模块的边界（硬约束）**：

| 做 | 不做 |
|---|---|
| 提供事件规则与状态（`EventService` / `EventState`） | **不执行**离线模拟 |
| 提供 `rollDailyEvent()` / `expireIfNeeded()` 可调用单元 | **不实现** `WorldSimulationService` |
| 提供事件判定与显示信息 | 不实现完整日结循环、不推进时间 |
| 提供常量（概率/时长/品质分/EventRate） | 不落库、不写 SQL（E 负责） |

> 依据：验收规范 §八十九「D 只供规则，不执行离线模拟」、模块分工第 5 行（B 的 P2 离线模拟「必须沿用世界环境模块规则」）、第 7 行（D 的 P2 = 随机事件）。

---

## 二、D 模块对外接口清单（离线模拟相关，只读）

| 接口 | 方法 | 离线模拟用途 |
|---|---|---|
| `EventService` | `EventType rollDailyEvent(int dayIndex)` | 每日 00:00 抽取事件（写状态） |
| `EventService` | `void expireIfNeeded(long currentWorldTime)` | 到期关闭事件 |
| `EventService` | `boolean isEventActive(long currentWorldTime)` | 判断事件是否持续中 |
| `EventService` | `String getDisplayName(EventType)` / `String getIcon(EventType)` | 离线日志/UI 显示 |
| `EventService` | `boolean isMeteorShower/isMysteryMerchant/isRainbowDay(EventType)` | 事件效果判定 |
| `EventState` | `getEventType/getStartWorldTime/getEndWorldTime/getTargetCropType/getPayload` | 事件状态读取 |
| `FarmGameModel` | `getEventService()` / `getEventState()` | 聚合只读入口 |

**世界时间口径（决策 D14）**：世界时间 = `getGameDay() * 24 + getGameHour()`（游戏小时），与 A 模块 `plantWorldTime` 同一适配口径。D 侧不新增第二时钟、不新增 `getWorldTime()`。

**随机口径（规则文档 §九十）**：`rollDailyEvent()` 内部经 `RandomProvider.nextInt(100)` 一次抽取，区间 `[0,74)` 无事件、`[74,79)` 流星夜、`[79,87)` 神秘商人、`[87,97)` 小动物来访、`[97,100)` 彩虹日。**一次随机抽取决定结果，禁止四个事件分别独立判断**（验收规范 §九十）。离线模拟**不得**另写一套随机算法。

---

## 三、L4.1 与 B 模块（`OfflineSimulationService`）的规则复用约定

### 3.1 约定总表

| 约定项 | 内容 | 来源 |
|---|---|---|
| 规则复用 | B 模块 `OfflineSimulationService` **必须复用** D 模块 `EventService` 规则，不得另写一套事件算法 | 验收 §八十九、模块分工第 5 行 |
| 抽取调用 | 离线每日循环调用 `EventService.rollDailyEvent(dayIndex)` 抽取当天事件 | 验收 §八十九 第 ⑬ 步 |
| 关闭调用 | 离线每日循环调用 `EventService.expireIfNeeded(now)` 关闭到期事件 | 验收 §八十九 第 ⑧ 步 |
| 供规则不执行 | D 模块 P2 **只供规则**，**不执行**离线模拟（离线模拟由 B 执行） | 验收 §八十九、模块分工第 5 行 |
| 统一入口 | 在线与离线共用同一套世界模拟规则（建议 `WorldSimulationService`） | 验收 §八十九 |
| 状态读取 | B 模块通过 `FarmGameModel.getEventState()` 读取当前事件（只读） | 验收 §九十一 |

### 3.2 离线每日循环中的事件调用点（验收 §八十九）

离线模拟**不能**简单 `offlineHours ÷ 24` 粗暴处理，必须按「游戏日 00:00 边界 / 事件结束时间 / 作物成熟时间」切段（验收 §八十八）。每个游戏日的日结顺序中，事件相关为第 ⑧ 步与第 ⑬ 步：

```text
① 处理当前时间段成长
② 更新阶段
③ 标记成熟
④ 到达日结边界
⑤ 处理补水
⑥ 更新 droughtStreak
⑦ 枯萎判定
⑧ 关闭过期 Event        ← EventService.expireIfNeeded(now)
⑨ 保存 DailyLog
⑩ GameDay + 1
⑪ 生成新 Weather
⑫ 雨天自动补水
⑬ 抽取当天 Event        ← EventService.rollDailyEvent(dayIndex)
⑭ 继续下一段
```

**调用顺序约束**：先 ⑧ 关闭（用**旧日**世界时间判定到期），再 ⑩ 跨日，最后 ⑬ 抽取（用**新日**世界时间作为事件起点）。`rollDailyEvent()` 内部以当前 `GameClock` 计算 `now` 作为 `startWorldTime`，因此调用前必须已完成 `GameDay + 1`。

### 3.3 离线模拟的伪代码（B 模块侧，供参考）

```java
// B 模块 OfflineSimulationService（示意，D 不实现）
for (int day = startDay; day <= endDay; day++) {
    // ... ①~⑦ 成长/补水/枯萎（A 模块规则）...

    long now = (long) clock.getGameDay() * 24 + clock.getGameHour();
    eventService.expireIfNeeded(now);          // ⑧ 关闭过期事件

    // ⑨ 保存 DailyLog（E 模块）
    clock.advanceDay();                        // ⑩ GameDay + 1
    weatherService.rollDailyWeather();         // ⑪ 生成新天气（D 模块 P1）
    // ⑫ 雨天自动补水（A 模块）

    eventService.rollDailyEvent(clock.getGameDay()); // ⑬ 抽取当天事件
}
```

> 说明：`clock.advanceDay()` 为示意；实际跨日由上层协调器按 A 模块 `GameClock` 契约执行。D 不持有该调用。

### 3.4 需 B 模块修改的部分（以文档提出，D 不直接改）

- `OfflineSimulationService` 在每日循环中调用 D 的 `EventService.rollDailyEvent()` 与 `expireIfNeeded()`；
- **不拥有**独立事件公式、独立概率表、独立随机源；
- 事件效果（品质分/售价/EventRate）由 A/C 模块按各自约定消费，B 不重复实现。

---

## 四、L4.2 与 `WorldSimulationService` 的接口

### 4.1 约定总表

| 约定项 | 内容 | 来源 |
|---|---|---|
| 依赖关系 | D 的 `EventService` 是 `WorldSimulationService` 的**依赖之一** | 验收 §八十九 |
| 日结调用 | `WorldSimulationService` 在日结时调用 `EventService.rollDailyEvent()` 与 `expireIfNeeded()` | 验收 §八十九 |
| 归属 | `WorldSimulationService` 由**上层协调器**实现（A 模块「持续世界引擎」/团队协调），**D 不实现** | 模块分工第 3 行、验收 §八十九 |
| 在线一致 | 在线每日结算也必须调用相同领域逻辑，禁止 Online/Offline 两套算法 | 验收 §八十九 |

### 4.2 依赖注入契约

`WorldSimulationService` 通过 `FarmGameModel` 聚合入口取得 D 的 `EventService`（只读），**不得**自行 `new BasicEventService(...)`：

```java
// 上层协调器（示意，D 不实现）
EventService eventService = model.getEventService();   // D 提供
EventState   eventState   = model.getEventState();     // D 提供（只读）

// 日结时：
eventService.expireIfNeeded(now);                      // ⑧
// ... GameDay + 1 ...
eventService.rollDailyEvent(model.getGameClock().getGameDay()); // ⑬
```

**构造顺序约束**：`BasicEventService` 依赖 `GameClock` 读取世界时间，因此 `FarmGameModel` 中事件系统**最后构造**（在 `gameClock` 之后）。上层协调器只需从 `model` 取用，无需关心构造顺序。

### 4.3 职责边界（D 不实现 `WorldSimulationService`）

| 组件 | 归属 | 职责 |
|---|---|---|
| `EventService` / `EventState` | **D** | 事件规则与状态（本模块） |
| `WorldSimulationService` | 上层协调器（A/团队） | 按时间窗口调用同一套世界模拟规则 |
| `OfflineSimulationService` | **B** | 按离线时间窗口驱动 `WorldSimulationService` |

> D 只提供 `rollDailyEvent()` / `expireIfNeeded()` 作为**可调用单元**，由上层协调器在跨日时调用；D **不**自行实现完整日结循环（见《D模块-P2跨模块接口约定文档.md》§六 矛盾 2）。

---

## 五、事件效果消费方（离线模拟同样生效）

离线模拟期间发生的事件，其效果由对应模块按同一规则消费（验收 §九十二）：

| 事件 | 持续 | 效果 | 消费方 | 常量 |
|---|---|---|---|---|
| 流星夜 | 24 游戏小时 | 新种作物品质 +20、传说概率 +10% | C | `EVENT_QUALITY_METEOR_SHOWER=20`、`EVENT_METEOR_LEGENDARY_BONUS=10` |
| 神秘商人 | 12 游戏小时 | 随机 1 种作物售价 ×2 | C | `EventState.getTargetCropType()` |
| 小动物来访 | 即时 | 种子/肥料/50~200 金币 | B/C | `EventType.isInstant()` |
| 彩虹日 | 24 游戏小时 | `EventRate ×2`、品质 +15 | A/C | `EVENT_RAINBOW_EVENT_RATE=2.0`、`EVENT_QUALITY_RAINBOW_DAY=15` |

**判定入口**：`isMeteorShower(type)` / `isMysteryMerchant(type)` / `isRainbowDay(type)` / `isEventActive(now)`。

---

## 六、常量与状态字段（离线模拟需知）

### 6.1 概率与时长（`GameConstants`）

| 常量 | 值 | 含义 |
|---|---:|---|
| `EVENT_PROB_NONE` | 74 | 无事件概率（%） |
| `EVENT_PROB_METEOR_SHOWER` | 5 | 流星夜概率（%） |
| `EVENT_PROB_MYSTERY_MERCHANT` | 8 | 神秘商人概率（%） |
| `EVENT_PROB_ANIMAL_VISIT` | 10 | 小动物来访概率（%） |
| `EVENT_PROB_RAINBOW_DAY` | 3 | 彩虹日概率（%） |
| `EVENT_DURATION_METEOR_SHOWER` | 24 | 流星夜时长（游戏小时） |
| `EVENT_DURATION_MYSTERY_MERCHANT` | 12 | 神秘商人时长（游戏小时） |
| `EVENT_DURATION_RAINBOW_DAY` | 24 | 彩虹日时长（游戏小时） |
| `EVENT_RAINBOW_EVENT_RATE` | 2.0 | 彩虹日成长倍率 |
| `EVENT_RATE_P0` | 1.0 | P0 占位（无事件倍率） |

### 6.2 事件状态字段（`EventState` ↔ E 模块 `active_event` 表）

| `EventState` 方法 | `active_event` 列 | 说明 |
|---|---|---|
| `getEventType()` | `event_type` | 枚举 `name()` 存取 |
| `getStartWorldTime()` | `start_world_time` | 游戏小时 |
| `getEndWorldTime()` | `end_world_time` | 游戏小时 |
| `getTargetCropType()` | `target_crop_type` | 神秘商人目标作物，否则 null |
| `getPayload()` | `payload` | 事件附加数据，否则 null |

> 离线模拟跨日时，事件状态由 D 的 `EventService` 维护；存档落库由 E 模块负责（验收 §九十一）。游戏在事件持续期间退出，回来后事件不能凭空消失。

---

## 七、跨模块矛盾与待裁定项（强制报告）

> 依据会话约束「发现文档间矛盾时立即停止并报告，禁止自行选择其中一种」。

### 矛盾 1：`WorldSimulationService` 的归属未明确

| 文档 | 表述 |
|---|---|
| 验收规范 §八十九 | 「建议共用 `WorldSimulationService`」，未指定归属模块 |
| 模块分工第 3 行 | A（lyj）P2 = 「持续世界引擎」 |
| 模块分工第 5 行 | B（hsy）P2 = 「离线模拟 + 离线日志」 |

- **影响范围**：`WorldSimulationService` 的归属与实现方。
- **D 处理**：D **不实现** `WorldSimulationService`，只提供 `EventService` 作为其依赖之一；归属由团队裁定。
- **待裁定人**：团队（跨模块协调）。**状态：未裁定**。

### 矛盾 2：天气/事件生成时机的阶段归属（P1 遗留，未裁定）

| 文档 | 表述 |
|---|---|
| 规则文档 §八十一 | 完整 12 步日结流程（含天气生成、事件抽取） |
| 验收规范 §八十五 | 离线模拟属 P2 |

- **D 处理**：D 只提供 `rollDailyWeather()` / `rollDailyEvent()` 作为可调用单元，由上层协调器在跨日时调用；D **不**自行实现完整日结循环。
- **待裁定人**：团队（跨模块协调）。**状态：未裁定**。

### 矛盾 3：`EventService` 世界时间来源（P2 新增）

| 文档 | 表述 |
|---|---|
| 任务书 | `BasicEventService` 依赖 `EventState`、`GameClock`、`RandomProvider` |
| 决策记录 D14 | D 侧 `GameClock` 不提供 `getWorldTime()`，A 侧时间由 `getGameDay()*24 + getGameHour()` 适配 |

- **D 处理**：`BasicEventService` 通过 `getGameDay()*24 + getGameHour()` 适配计算世界小时（与 A 模块 `plantWorldTime` 同一口径），**不新增第二时钟、不新增 `getWorldTime()`**。
- **待裁定人**：团队（跨模块协调）。**状态：待确认**。

---

## 八、溯源说明

| 关键数值/规则 | 来源文档 | 章节 |
|---|---|---|
| 事件概率 74/5/8/10/3 | 《FSF游戏规则设计文档.md》 | §四十七 |
| 流星夜持续 24h、品质+20、传说+10% | 《FSF游戏规则设计文档.md》 | §四十八 |
| 神秘商人持续 12h、售价×2 | 《FSF游戏规则设计文档.md》 | §四十九 |
| 小动物即时、50~200 金币 | 《FSF游戏规则设计文档.md》 | §五十 |
| 彩虹日持续 24h、EventRate×2、品质+15 | 《FSF游戏规则设计文档.md》 | §五十一 |
| 每日最多 1 事件、一次抽取 | 《FSF游戏规则设计文档.md》 | §四十七 |
| 随机统一使用 RandomProvider | 《FSF游戏规则设计文档.md》 | §九十 |
| 离线模拟允许项 | 《FSF_P0-P4功能实现与验收规范.md》 | §八十五 |
| 离线模拟分段 | 《FSF_P0-P4功能实现与验收规范.md》 | §八十八 |
| 每日离线顺序（⑧⑬） | 《FSF_P0-P4功能实现与验收规范.md》 | §八十九 |
| 离线模拟复用在线规则 | 《FSF_P0-P4功能实现与验收规范.md》 | §八十九 |
| active_event 表字段 | 《FSF_P0-P4功能实现与验收规范.md》 | §九十一 |
| 事件效果 | 《FSF_P0-P4功能实现与验收规范.md》 | §九十二 |
| 时间字段统一 long | 《FSF项目需求分析与开发计划书.md》 | 决策 D14 |
| D 模块 P2 = 随机事件 | 《模块分工.md》 | 第 7 行 |
| B 模块 P2 必须沿用世界环境规则 | 《模块分工.md》 | 第 5 行 |
