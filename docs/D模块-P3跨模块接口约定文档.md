# D 模块 P3 跨模块接口约定文档（只读接口 · 无新增业务接口）

> 版本：P3 · v0.4.0-collect
> 负责人：D（zsl）· 世界环境模块
> 依据：《FSF_P0-P4功能实现与验收规范》§一百零九~§一百三十二（P3 总体目标 / Collection / Set / FarmScore / FarmRank / Showcase / 土地解锁 / 毕业）、《游戏规则设计文档》§十九~§五十一、《模块分工.md》第 7 行
> 约束：D 模块**不修改** A/C/B/E 模块的类，本文件仅提出接口约定，需其他模块修改的部分以本文档形式提出。
> 前置文档：`docs/D模块-P2跨模块接口约定文档.md`（P2 随机事件系统）

---

## 〇、一句话结论

**P3 阶段 D 模块不新增任何业务接口**：C/E 模块对 D 的 P3 需求全部落在 P0~P2 已公开的**只读**接口上（`WeatherService` / `EventService` / `GameClock` / `WeatherState` / `EventState` / `FarmGameModel` 聚合入口）。D 在 P3 只做「被读取」，不做「被调用写状态」。

---

## 一、P3 中 C/E 模块对 D 模块的只读数据需求（L1.1）

### 1.1 C 模块（品质与传说域）的只读需求

| 需求点 | P3 场景 | 需要 D 提供的数据 | 来源 |
|---|---|---|---|
| 展示台传说故事 | `ShowcaseService` 展示「已获得过的传说作物历史记录」，需还原该株作物的**关键天气**与**关键事件** | 天气经历（雨/旱/绿雨次数）、事件经历（流星夜/彩虹日等） | 验收 §一百二十三/§一百二十四 |
| 展示内容字段 | 展示台至少显示「关键天气」「关键事件」 | 同上（历史经历，非当前天气/事件） | 验收 §一百二十四 |
| 传奇之光套装 Buff | 套装「传奇之光」激活 → 传说突破概率 +10% | 无（D 不参与套装判定；由 C 的 `LegendaryService.setLegendarySetBonus(int)` 承接） | 验收 §一百一十七/§一百 |

> **关键结论**：展示台所需的「关键天气/关键事件」是**历史经历**，已由 C 模块 `CropMemory`（P2 落档）持有（`rainCount`/`droughtCount`/`greenRainCount`/`events` 等字段），**不依赖 D 的实时接口**。D 的 `WeatherService`/`EventService` 仅在**记录当时**（P2 日结算）被调用写入 `CropMemory`，P3 展示台读取的是 `CropMemory` 快照，**不新增对 D 的读取**。

### 1.2 E 模块（存档与引擎）的只读需求

| 需求点 | P3 场景 | 需要 D 提供的数据 | 来源 |
|---|---|---|---|
| 存档装配 | 读档时把 `world_state.current_weather` / `active_event` 还原到 D 的状态对象 | `WeatherState` / `EventState` 的 setter（恢复入口） | 验收 §七十三/§九十一 |
| 存档落库 | 存档时从 D 状态对象读取当前天气/事件 | `WeatherState.getWeatherType()/getDayIndex()`、`EventState.getEventType()/getStartWorldTime()/getEndWorldTime()/getTargetCropType()/getPayload()` | 验收 §七十三/§九十一 |
| 时间落库 | 存档 `gameDay` / `currentWorldTime` | `GameClock.getGameDay()` / `getTotalMinutes()` | 验收 §四十一/§四十二 |
| P3 新表 | `CollectionDao` / `SetCollectionDao` / `ShowcaseDao` | 无（P3 新表与 D 无关，D 不持有图鉴/套装/展示台数据） | 验收 §一百五十一 |

> **关键结论**：E 对 D 的 P3 需求与 P1/P2 完全一致（存档装配/落库），**无新增字段、无新增表**。

### 1.3 明确「不需要 D 参与」的 P3 功能

| P3 功能 | 归属 | 与 D 的关系 |
|---|---|---|
| 作物图鉴（15 项） | C（CollectionService） | 无（收获品质落档即可） |
| 装饰图鉴（14 项） | B/C（CollectionService） | 无 |
| 套装系统（3 套） | C（SetService） | 无（传奇之光 Buff 由 C 内部承接） |
| FarmScore / FarmRank | C（FarmScoreService/FarmRankService） | 无 |
| 展示台 | C（ShowcaseService） | 只读 `CropMemory`（C 自有），不读 D |
| 土地解锁 | A/B（LandUnlockService） | 无（价格读 `balance-config.json`） |
| 永恒花园毕业 | C（GraduationService） | 无（唯一条件 FarmScore==147） |

---

## 二、D 模块 P3 只读接口清单（L1.2）

> 以下接口**全部为 P0~P2 已公开接口**，P3 不新增、不修改签名。调用方只应使用**查询方法**，写状态方法（`rollDailyWeather`/`rollDailyEvent`/`expireIfNeeded`）仅由上层日结算协调器调用。

### 2.1 天气（`WeatherService`，P1）

| 方法 | 说明 | P3 用途 |
|---|---|---|
| `WeatherType rollDailyWeather(int dayIndex)` | 每日 00:00 生成天气（**写状态**，仅日结算调用） | — |
| `double getGrowthRate(WeatherType)` | 成长倍率（晴 1.0/雨 1.5/旱 0.5/绿雨 2.0） | A 成长（P1 已接） |
| `int getQualityScore(WeatherType)` | 单次品质分（晴 0/雨 5/旱 8/绿雨 15） | C 品质（P1 已接） |
| `int getQualityScoreCap(WeatherType)` | 品质分上限（晴 0/雨 20/旱 24/绿雨 45） | C 品质（P1 已接） |
| `boolean isRain/isDrought/isGreenRain(WeatherType)` | 天气判定 | A/C（P1 已接） |
| `String getDisplayName/getIcon(WeatherType)` | UI 显示 | 状态栏（P1 已接） |

### 2.2 事件（`EventService`，P2）

| 方法 | 说明 | P3 用途 |
|---|---|---|
| `EventType rollDailyEvent(int dayIndex)` | 每日 00:00 抽取事件（**写状态**，仅日结算调用） | — |
| `boolean isEventActive(long currentWorldTime)` | 事件是否持续中 | A/C（P2 已接） |
| `void expireIfNeeded(long currentWorldTime)` | 到期关闭事件（**写状态**，仅日结算调用） | — |
| `boolean isMeteorShower/isMysteryMerchant/isRainbowDay(EventType)` | 事件判定 | C 品质/传说（P2 已接） |
| `String getDisplayName/getIcon(EventType)` | UI 显示 | 状态栏（P2 已接） |

### 2.3 时钟（`GameClock`，P0）

| 方法 | 说明 | P3 用途 |
|---|---|---|
| `int getGameDay()` | 当前游戏日（从 1 开始） | 存档 `gameDay` |
| `int getGameHour()` | 当前小时（0~23） | 世界时间适配 |
| `int getTotalMinutes()` | 累计总分钟数 | 存档 `currentWorldTime` |
| `LocalDateTime getRealTime()` | 现实时间 | 离线时长（B） |
| `long calculateOfflineDuration()` | 离线现实分钟数 | 离线模拟（B） |
| `boolean isDaytime()` / `String getTimeString()` | 显示辅助 | 状态栏 |

> **世界时间口径（决策 D14）**：世界时间 = `getGameDay() * 24 + getGameHour()`（游戏小时），与 A 模块 `plantWorldTime` 同一适配口径。D 侧**不新增** `getWorldTime()`、**不新增**第二时钟。

### 2.4 状态对象（只读字段）

| 类型 | 只读方法 | P3 用途 |
|---|---|---|
| `WeatherState` | `getWeatherType()` / `getDayIndex()` | E 存档落库 |
| `EventState` | `getEventType()` / `getStartWorldTime()` / `getEndWorldTime()` / `getTargetCropType()` / `getPayload()` | E 存档落库 |

### 2.5 聚合只读入口（`FarmGameModel`）

| 方法 | 说明 |
|---|---|
| `GameClock getGameClock()` | 时钟只读入口 |
| `WeatherService getWeatherService()` / `WeatherState getWeatherState()` | 天气只读入口 |
| `EventService getEventService()` / `EventState getEventState()` | 事件只读入口 |
| `int getWorldTimeTotalMinutes()` | 存档时间读取 |
| `void restoreWorldTime(int)` / `void restoreWeather(String, int)` | 存档恢复入口（E 装配层调用） |

---

## 三、D 不新增任何业务接口的确认（L1.3）

| 确认项 | 结论 | 依据 |
|---|---|---|
| P3 是否新增 Service 接口 | **否** | 验收 §一百五十「P3 新增」清单仅含 C 的 7 个 Service，无 D |
| P3 是否新增 DAO | **否** | 验收 §一百五十一「P3」仅含 `CollectionDao`/`SetCollectionDao`/`ShowcaseDao`，无 D |
| P3 是否新增表 | **否** | D 相关表 `world_state`（P1）、`active_event`/`event_log`（P2）已建，P3 无新增 |
| P3 是否修改 P0~P2 公开签名 | **否** | 本文件 §二 清单与 P0~P2 文档逐条一致 |
| P3 是否新增常量 | **否** | `GameConstants` 天气/事件常量 P1/P2 已定稿 |
| 传奇之光套装 Buff 承接方式 | C 侧 `LegendaryService.setLegendarySetBonus(int)`（P2 已预留，默认 0） | 验收 §一百 |

> **结论**：D 模块 P3 保持 P0~P2 公开接口**完全不变**，仅作为只读数据源被 C/E 读取。

---

## 四、与 C 模块（品质与传说）P3 接口约定

| 约定项 | 内容 | 来源 |
|---|---|---|
| 展示台数据来源 | `ShowcaseService` 展示的「关键天气/关键事件」取自 **C 自有 `CropMemory`**，不读 D 实时接口 | 验收 §一百二十三/§一百二十四 |
| 传奇之光 Buff | C 侧 `LegendaryService.setLegendarySetBonus(10)` 承接，D 不参与 | 验收 §一百一十七/§一百 |
| 天气/事件记录时机 | 仍由 P2 日结算调用 `MemoryService.recordRain/recordDrought/recordGreenRain/recordEvent` 写入 `CropMemory` | 验收 §九十四 |
| 只读调用 | C 若需当前天气/事件（如 UI），经 `FarmGameModel.getWeatherService()/getEventService()` 只读调用 | P1/P2 文档 |

> **需 C 模块修改的部分（以文档提出，D 不直接改）**：无新增对 D 的调用；展示台读取 `CropMemory` 即可。

---

## 五、与 E 模块（存档）P3 接口约定

| 约定项 | 内容 | 来源 |
|---|---|---|
| 存档装配 | 读档时 E 调用 `FarmGameModel.restoreWeather(name, dayIndex)` 与 `EventState` setter 还原 D 状态 | 验收 §七十三/§九十一 |
| 存档落库 | 存档时 E 从 `WeatherState`/`EventState` 只读方法取值落库 | 验收 §七十三/§九十一 |
| 时间落库 | `gameDay` ← `GameClock.getGameDay()`；`currentWorldTime` ← `getTotalMinutes()` | 验收 §四十一/§四十二 |
| P3 新表 | `CollectionDao`/`SetCollectionDao`/`ShowcaseDao` 与 D 无关 | 验收 §一百五十一 |
| 表结构变更 | **无**（D 相关表 P1/P2 已定稿） | 验收 §七十三/§九十一 |

> **需 E 模块修改的部分（以文档提出，D 不直接改）**：无。P3 存档装配沿用 P1/P2 既有入口。

---

## 六、跨模块矛盾与待裁定项（强制报告）

> 依据会话约束「发现文档间矛盾时立即停止并报告，禁止自行选择其中一种」。

### 矛盾 1：`lastHydratedTime` 字段类型（P1 遗留，未裁定）

| 文档 | 表述 |
|---|---|
| 验收规范 §五十 | `LocalDateTime lastHydratedTime;` |
| 规则文档 §二十五 | `LocalDateTime lastHydratedTime;` |
| 决策记录 D14 | 「A 侧时间字段统一 `long`」 |

- **影响范围**：A 模块 `Crop` 模型（非 D 模块）。
- **D 处理**：D 不持有该字段，**不自行裁定**；建议 A 模块（lyj）按 D14 统一为 `long`（游戏小时）。
- **待裁定人**：A 模块（lyj）。**状态：未裁定**。

### 矛盾 2：天气/事件生成时机的阶段归属（P1 遗留，未裁定）

| 文档 | 表述 |
|---|---|
| 规则文档 §八十一 | 完整 12 步日结流程（含天气生成第 ⑧ 步、事件抽取第 ⑩ 步） |
| 验收规范 §八十五 | 离线模拟属 P2 |

- **D 处理**：D 只提供 `rollDailyWeather()` / `rollDailyEvent()` 作为可调用单元，由上层协调器在跨日时调用；D **不**自行实现完整日结循环。
- **待裁定人**：团队（跨模块协调）。**状态：未裁定**。

### 矛盾 3：天气品质分上限的「次」口径（P1 遗留，未裁定）

- **D 处理**：D 只提供单次分值与上限常量，计次与封顶逻辑由 C 模块 `QualityService` 实现。
- **待裁定人**：C 模块（hy）。**状态：未裁定**。

### 矛盾 4：`EventService` 世界时间来源（P2 新增，已按 D14 处理）

| 文档 | 表述 |
|---|---|
| 任务书 | `BasicEventService` 依赖 `EventState`、`GameClock`、`RandomProvider` |
| 决策记录 D14 | D 侧 `GameClock` 不提供 `getWorldTime()`，A 侧时间由 `getGameDay()*24 + getGameHour()` 适配 |

- **D 处理**：`BasicEventService` 通过 `getGameDay()*24 + getGameHour()` 适配计算世界小时（与 A 模块 `plantWorldTime` 同一口径），**不新增第二时钟、不新增 `getWorldTime()`**。
- **待裁定人**：团队（跨模块协调）。**状态：待确认**。

### 矛盾 5：P3 展示台「关键天气/关键事件」数据来源（P3 新增，本次发现）

| 文档 | 表述 |
|---|---|
| 验收规范 §一百二十四 | 展示台至少显示「关键天气」「关键事件」 |
| 验收规范 §九十四 | `CropMemory` 记录「天气/干旱/绿雨/事件」经历 |
| 验收规范 §一百二十三 | 展示台展示的是 `CropMemory`（历史记录），非当前活 Crop |

- **分析**：展示台所需天气/事件为**历史经历**，`CropMemory` 已持有（`rainCount`/`droughtCount`/`greenRainCount`/`events`）；D 的实时接口不参与展示台读取。
- **D 处理**：D **不新增**展示台相关接口；展示台由 C 的 `ShowcaseService` 读取 `CropMemory` 实现。
- **待裁定人**：C 模块（hy）。**状态：待确认**（确认展示台不依赖 D 实时接口）。

---

## 七、溯源说明

| 关键数值/规则 | 来源文档 | 章节 |
|---|---|---|
| P3 总体目标（图鉴/套装/评价/展示台/土地/永恒花园） | 《FSF_P0-P4功能实现与验收规范.md》 | §一百零九 |
| 展示台展示 CropMemory 历史记录 | 《FSF_P0-P4功能实现与验收规范.md》 | §一百二十三 |
| 展示内容（关键天气/关键事件） | 《FSF_P0-P4功能实现与验收规范.md》 | §一百二十四 |
| 传奇之光套装 Buff +10% | 《FSF_P0-P4功能实现与验收规范.md》 | §一百一十七 |
| FarmScore 147 / 永恒花园唯一条件 | 《FSF_P0-P4功能实现与验收规范.md》 | §一百一十九/§一百二十二 |
| P3 新增 Service 清单（无 D） | 《FSF_P0-P4功能实现与验收规范.md》 | §一百五十 |
| P3 新增 DAO 清单（无 D） | 《FSF_P0-P4功能实现与验收规范.md》 | §一百五十一 |
| CropMemory 记录项（天气/事件） | 《FSF_P0-P4功能实现与验收规范.md》 | §九十四 |
| 天气概率/倍率/品质分 | 《FSF游戏规则设计文档.md》 | §十九/§三十五 |
| 事件概率/持续/效果 | 《FSF游戏规则设计文档.md》 | §四十七~§五十一 |
| 时间字段统一 long（D14） | 《FSF项目需求分析与开发计划书.md》 | 决策 D14 |
| D 模块 P3 = 只读数据源 | 《模块分工.md》 | 第 7 行 |
