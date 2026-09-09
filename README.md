# Field Story Farm P0 MVP — Single World Clock 修正版

## 本版解决什么

上一版虽然把 `refresh()` 与 `save()` 中的 `tick()` 删除了，但 `DemoGameClock.now()` 仍然基于 `System.nanoTime()` 实时换算720倍世界时间。
因此任何按钮完成后的 `refresh()` 都会立即读出“按钮处理期间已经流逝的世界时间”，视觉上仍像按钮推进时间。

本版彻底修改为 **提交式世界时钟**：

```text
App Timeline（唯一推进源）
        ↓
GameManager.advanceWorld(1现实秒)
        ↓
GameClock.advance(1现实秒)
        ↓
Demo: +12游戏分钟
        ↓
GrowthService.update(farm)
        ↓
FarmView.refresh()
```

以下操作都不会调用 `advance()`：

```text
选择土地
购买种子
开垦
播种
浇水
收获
save()
refresh()
```

因此按钮即使执行JSON同步保存，也只会让UI短暂卡顿，不会造成下一次世界时间“补跳”。

## 为什么符合P0规范

- GameClock从P0存在；Service不直接读系统时间。
- Model只保存状态；Controller只接收操作并调用Service。
- 世界时间与成长统一由一个入口推进。
- P0使用DemoClock ×12；同时补了 RealGameClock 与 TestGameClock 类型，便于后续按规范切换。
- P0退出后不做离线推进；load后从保存的 `currentWorldTime` 继续。

## 新增可观测信息

顶部增加：

```text
世界推进: N次 | 最近一次 +12游戏分钟
```

只有统一Timeline触发时N才会增加。点击任意右侧按钮时N必须保持不变。

右侧选中作物增加 `ProgressBar`：

```text
成长推进进度：31.4% | 发芽 → MATURE
```

方便验证每个统一世界脉冲后成长是否正确变化。

## 推荐验证

1. 启动后记住 `世界推进` 计数。
2. 在同一个推进周期内连续点击购买、开垦、选择土地。
3. 计数不应因按钮增加，游戏时间也不应因按钮独立改变。
4. 只有每次Timeline世界脉冲到达时，计数+1，Demo世界时间+12分钟。
5. 播种后等待统一推进，成长进度条随世界脉冲变化。

## 运行

```bash
mvn test
mvn clean javafx:run
```

---

# P0 集成测试版新增内容

## 什么是集成测试

单元测试关注“一个类/一个Service自己是否正确”；集成测试关注多个真实组件连接后，一条完整业务链是否仍然正确。

本项目P0集成测试连接：

```text
GameManager
  ↓
LandService + EconomyService + PlantingService
  ↓
GameClock + GrowthService + WateringService
  ↓
BasicHarvestService + LandService
  ↓
SaveService / JsonSaveService
  ↓
重新创建GameManager并恢复
```

JavaFX View不进入核心集成测试，避免UI线程和动画影响业务验证。UI只负责输入/显示，核心规则应当在Service层可独立测试。

## 新增正式测试

```text
src/test/java/com/fieldstory/farm/integration/
└── P0AcceptanceIntegrationTest.java
```

包含两条链：

### 1. fullP0AcceptanceFlowShouldPersistAndRestore

对应P0官方核心验收流程：

```text
新建游戏
→ 金币500
→ 开垦3块地
→ 买小麦/玉米/胡萝卜
→ 三块地播种
→ TestGameClock推进到SPROUT
→ 三株主动浇水
→ 验证浇水成长倍率
→ 推进到全部MATURE
→ 全部收获并按基础售价结算
→ 再买1颗小麦
→ 在TILLED土地重新播种
→ 保存JSON
→ 创建新的GameManager模拟重启
→ load
→ 验证金币/库存/土地/Crop/世界时间全部恢复
```

### 2. failedOperationsShouldNotCorruptIntegratedState

验证失败操作不能破坏状态：

```text
EMPTY直接播种 → 失败，种子/金币/土地不变
重复开垦 → 失败，不再次扣5金币
SEED浇水 → 失败，manualWaterCount不变
未成熟收获 → 失败，不加金币、不移除Crop
同游戏日第二次浇水 → 失败，只记录1次
```

## 为测试做的生产代码改造

`GameManager`增加依赖注入构造器：

```java
public GameManager(
        SaveService saveService,
        Function<LocalDateTime, GameClock> clockFactory)
```

默认构造器行为完全不变：

```text
JsonSaveService(data/save-p0.json)
+
DemoGameClock
```

测试使用：

```text
JsonSaveService(@TempDir临时路径)
+
TestGameClock
```

好处：

- 不污染玩家真实存档；
- 不需要Thread.sleep；
- 可以确定性推进20小时、24小时、49小时；
- P1以后SaveService切换SQLite时GameManager不需要重写业务调用。

## 测试运行

```bash
mvn test
```

重点应看到：

```text
P0AcceptanceIntegrationTest
BasicEconomyServiceTest
BasicGrowthServiceTest
BasicWateringServiceTest
JsonSaveServiceTest
DemoGameClockTest
ButtonTimeIsolationTest
SoilStateTest
```

## 当前P0备用MVP推进进度

```text
[完成] 基础Model
[完成] B基础经济
[完成] 土地开垦
[完成] 三种种子购买
[完成] 播种
[完成] 单一世界时钟
[完成] 成长阶段与百分比
[完成] 主动浇水限制与Buff
[完成] MATURE 100%封顶
[完成] 基础收获/自动出售
[完成] 收获后LandService回TILLED
[完成] JSON保存/恢复
[完成] 按钮与世界时间隔离
[完成] 单元测试
[完成] P0官方主链集成测试
[完成] P0失败操作集成测试
[待优化] 12×12完整MapTile Model（当前中心8×8为真实Soil，外围为View占位）
[待优化] 最终P0回归测试/答辩验收脚本
```

当前下一优先级：**12×12完整MapTile Model**，然后做最终P0回归测试，不提前进入P1功能。

---

# P0 12×12 MapTile Model 版新增内容

## 本版目标

上一版虽然UI显示12×12，但Farm Model内部只有中心8×8的64个Soil，外围80格由View硬编码占位。
本版把地图结构正式下沉到Model：

```text
Farm
└── 144 × MapTile
    ├── row / column        // 12×12全局坐标
    ├── FarmPlot plotType
    └── Soil?               // 仅FARM_PLOT非null
```

P0保持：

```text
12×12 = 144格
中心8×8 = 64个FARM_PLOT + Soil
外围2格 = 80个功能区占位，无Soil，不可操作
```

文档没有规定SHOP/SHOWCASE的精确地图坐标，因此备用MVP不擅自发明正式布局。外围P0统一为 `DECORATION_AREA` 占位；以后确定正式布局时只需修改 `MapTile.plotType`，不需要重构Farm或Soil。

## 新增类

```text
model/MapTile.java
```

Farm现在提供：

```java
List<MapTile> getTiles();
MapTile getTile(int mapRow, int mapColumn);
Soil getSoil(int farmRow, int farmColumn); // 保持0..7局部农田坐标
List<Soil> allSoils();
long countTiles(FarmPlot type);
```

`FarmView`不再写：

```java
if (r >= 2 && r < 10 && c >= 2 && c < 10) ...
```

而是只读取 `MapTile.plotType` / `MapTile.soil` 来决定显示与交互。地图结构只有一个事实源：Farm Model。

## 存档兼容

新JSON保存 `farm.tiles`。

为了不让此前备用MVP的 `farm.soils` 存档直接报废，旧结构迁移由：

```text
JsonSaveService
```

负责，在load时转换成新的144个MapTile。

迁移逻辑没有放进Farm Model，因为数据格式兼容属于Persistence职责。

## 新增测试

```text
FarmMapModelTest
```

自动验证：

```text
总MapTile = 144
FARM_PLOT = 64
外围功能格 = 80
Soil = 64
只有中心8×8拥有Soil
外围MapTile的Soil必须为null
0..7农田局部坐标仍正确映射到2..9地图坐标
```

`P0AcceptanceIntegrationTest`也增加了地图断言，保证保存重启后仍然是144格/64块Soil。

`JsonSaveServiceTest`增加旧 `farm.soils` JSON迁移测试。

## 当前P0备用MVP推进进度

```text
[完成] 基础Model
[完成] B基础经济
[完成] 土地开垦
[完成] 三种种子购买
[完成] 播种
[完成] 单一世界时钟
[完成] 成长阶段与百分比
[完成] 主动浇水限制与Buff
[完成] MATURE 100%封顶
[完成] 基础收获/自动出售
[完成] 收获后LandService回TILLED
[完成] JSON保存/恢复
[完成] 按钮与世界时间隔离
[完成] 单元测试
[完成] P0官方主链集成测试
[完成] P0失败操作集成测试
[完成] 12×12完整MapTile Model
[完成] 旧P0 JSON地图结构迁移
[待推进] P0最终回归测试/答辩验收脚本
[待推进] 统一业务变更后的自动保存协调策略
```

下一优先级：**P0最终回归测试 + 答辩验收脚本**。完成后建议冻结P0核心，不提前引入P1玩法。


## SaveFix：MapTile JSON字段修复

修复 Jackson 将 `MapTile.isFarmPlot()` 误识别为 JSON 属性 `farmPlot` 的问题。

- 持久化字段统一为 `row / column / plotType / soil`。
- `isFarmPlot()` 只是业务查询方法，使用 `@JsonIgnore` 排除。
- 不采用全局 `FAIL_ON_UNKNOWN_PROPERTIES=false`，避免真实存档字段错误被静默吞掉。
- 新增回归测试，保证保存文件包含 `plotType` 且不再出现 `farmPlot`，并验证 round-trip load。

- `MapTile` 同时声明 `@JsonIgnoreProperties("farmPlot")`，因此上一版已经写出的 `farmPlot` 字段也可以继续加载，不要求手动删存档。
