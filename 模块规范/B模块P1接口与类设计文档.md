B 模块 P1 接口与类设计文档
1. 文档信息
 
 
项目
内容
模块
B：玩家与经营模块
阶段
P1 / v0.2.0-playable
设计版本
B-P1-DESIGN v1.0
基线版本
B-P0-DESIGN v1.1-aligned
状态
接口冻结候选版
P1 新增职责
完整商店、14 种装饰、装饰放置/移动/移除、5 类 Buff 聚合
保持职责
金币、种子库存、种子购买、基础售价访问
明确不实现
品质计算、肥料业务、天气生成、枯萎判定、随机事件、离线模拟、套装 SetBonus、土地解锁、SQLite DAO、收获事务
主要协作
A 土地与作物、C 品质与传说/肥料、D 世界环境、E 存档与场景组装
参考文档
《FSF游戏规则设计文档》§52~§61、§65、§90；《FSF_P0-P4功能实现与验收规范》§46~§70；决策 D13、D14
 
2. P1 范围
2.1 必须实现
1. 完整商店：替换 P0 快捷购买区，含种子、装饰两个分类（验收规范 §65）。
2. 种子购买：仍调用 P0 冻结的 EconomyService.buySeed(type, quantity)，不重写（验收规范 §66）。
3. 14 种装饰：D01~D14 按规则文档 §53 硬编码于 DecorationType。
4. 装饰放置：只能放 FarmPlot.DECORATION_AREA（验收规范 §68）。
5. 装饰尺寸：普通 1×1，大型 2×2（规则文档 §59）。
6. 装饰移动：免费（规则文档 §59）。
7. Buff 五类：Growth/Quality/Price/WitherResistance/OperationModifier（规则文档 §54）。
8. 只有已放置装饰提供 Buff：背包中不生效（规则文档 §52）。
9. P1 SetBonus = 0：套装属 P3（验收规范 §70）。
10. 与 E 对接：装饰持久化字段、SceneManager.mount(Slot.RIGHT, shopView)。
2.2 明确不实现
text
 
 
复制
 
 
下载
品质评分计算
肥料库存与施肥业务
天气生成与天气倍率
枯萎判定
随机事件
离线模拟
套装 SetBonus
土地解锁
SQLite DAO / SQL
收获事务
HARVESTED 阶段
 
 
 
3. 架构与包结构
3.1 分层原则
沿用 P0：
text
 
 
复制
 
 
下载
View
↓
Controller
↓
Service
↓
Model
↓
DAO / Persistence
 
 
P1 新增的 B 模块类严格遵循 D13：接口在包根，实现类 Basic 前缀入 impl 子包。
3.2 B 模块 P1 包结构
text
 
 
复制
 
 
下载
com.fieldstory.farm
│
├── model
│   ├── CropType.java                      ← P0 共享
│   ├── DecorationType.java                ← P1 B 新增，14 种装饰 + 数值
│   ├── BuffCategory.java                  ← P1 B 新增，五类 Buff
│   ├── BuffType.java                      ← P1 B 新增，具体 Buff 类型
│   ├── Decoration.java                    ← P1 B 新增，装饰实例接口
│   ├── DecorationEffect.java              ← P1 B 新增，装饰效果值对象
│   ├── BuffSnapshot.java                  ← P1 B 新增，Buff 聚合快照
│   ├── economy
│   │   ├── PurchaseResult.java            ← P0
│   │   ├── DecorationPurchaseResult.java  ← P1 B 新增
│   │   └── DecorationPlacementResult.java ← P1 B 新增
│   └── impl
│       └── BasicDecoration.java           ← P1 B 新增
│
├── service
│   ├── economy
│   │   ├── EconomyService.java            ← P0 冻结
│   │   └── impl
│   │       └── EconomyServiceImpl.java    ← P0
│   ├── ShopService.java                   ← P1 B 新增
│   ├── DecorationService.java             ← P1 B 新增
│   ├── BuffService.java                   ← P1 B 新增
│   └── impl
│       ├── BasicShopService.java          ← P1 B 新增
│       ├── BasicDecorationService.java    ← P1 B 新增
│       └── BasicBuffService.java          ← P1 B 新增
│
├── controller
│   ├── SeedQuickBuyController.java        ← P0（保留）
│   ├── ShopController.java                ← P1 B 新增
│   └── DecorationController.java          ← P1 B 新增
│
└── view
    ├── StatusView.java                    ← P0 已改造（右上角商城按钮）
    ├── SeedQuickBuyView.java              ← P0（保留，作为种子商店面板）
    ├── ShopPopupView.java                 ← 已改造，含“种子商店 / 装饰品商店”Tab
    ├── DecorationShopView.java            ← P1 B 新增
    └── DecorationPanelView.java           ← P1 B 新增，装饰库存与放置面板
 
 
 
4. 核心类型总表
 
 
类型
性质
职责
归属
DecorationType
Enum
14 种装饰 ID、名称、价格、尺寸、效果
B
BuffCategory
Enum
五类 Buff 分类
B
BuffType
Enum
具体 Buff 类型，供 BuffService 解释
B
Decoration
Interface
装饰实例状态：UUID、类型、坐标、是否放置
B
BasicDecoration
Impl
Decoration 默认实现
B
DecorationEffect
Record
单个装饰效果：类型、数值、上限、目标作物
B
BuffSnapshot
Record
某位置/作物的 Buff 聚合结果
B
DecorationPurchaseResult
Enum
购买装饰结果
B
DecorationPlacementResult
Enum
放置/移动装饰结果
B
ShopService
Interface
完整商店：种子委托 P0，装饰购买
B
BasicShopService
Impl
ShopService 默认实现
B
DecorationService
Interface
装饰拥有、放置、移动、移除、区域校验
B
BasicDecorationService
Impl
DecorationService 默认实现
B
BuffService
Interface
装饰 Buff 聚合，供 A/C 调用
B
BasicBuffService
Impl
BuffService 默认实现
B
ShopController
Controller
商店 UI 操作入口
B
DecorationController
Controller
装饰放置/移动/移除操作入口
B
DecorationShopView
View
装饰商店面板（弹窗内 Tab 内容）
B
DecorationPanelView
View
装饰库存与选择面板
B
 
5. 枚举设计
5.1 BuffCategory
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

/**
 * Buff 五类分类（规则文档 §54）。
 */
public enum BuffCategory {
    /** 成长类：影响 GrowthDelta 的 DecorationRate 部分 */
    GROWTH,

    /** 品质类：影响品质评分 */
    QUALITY,

    /** 售价类：影响最终出售价格倍率 */
    PRICE,

    /** 枯萎抗性：只降低枯萎概率，不改变干旱成长倍率 */
    WITHER_RESISTANCE,

    /** 操作修正：修正主动浇水/施肥的成长效果 */
    OPERATION_MODIFIER
}
 
 
5.2 BuffType
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

/**
 * 具体 Buff 类型，供 BuffService 解释与聚合。
 */
public enum BuffType {

    /** D01 向日葵：8 邻格作物成长 +5%，最多 +15% */
    ADJACENT_GROWTH(BuffCategory.GROWTH),

    /** D05 大树 / D11 金色喷泉：全局成长加成 */
    GLOBAL_GROWTH(BuffCategory.GROWTH),

    /** D08/D09/D10：指定作物成长加成 */
    CROP_SPECIFIC_GROWTH(BuffCategory.GROWTH),

    /** D12 彩虹喷泉 / D14 丰收女神像：品质评分加成 */
    QUALITY_SCORE(BuffCategory.QUALITY),

    /** D13 金色王座 / D14 丰收女神像：售价倍率加成 */
    PRICE_RATE(BuffCategory.PRICE),

    /** D06 石灯笼：枯萎概率乘数（<1 表示降低） */
    WITHER_RESISTANCE(BuffCategory.WITHER_RESISTANCE),

    /** D02 玫瑰花坛：主动浇水成长效果乘数 */
    WATER_OPERATION_MULTIPLIER(BuffCategory.OPERATION_MODIFIER),

    /** D07 小喷泉：施肥成长效果乘数 */
    FERTILIZER_OPERATION_MULTIPLIER(BuffCategory.OPERATION_MODIFIER);

    private final BuffCategory category;

    BuffType(BuffCategory category) {
        this.category = category;
    }

    public BuffCategory getCategory() {
        return category;
    }
}
 
 
5.3 DecorationType
依据《FSF游戏规则设计文档》§53，P1 硬编码为唯一数据源。
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

import java.util.List;

public enum DecorationType {

    SUNFLOWER("D01", "向日葵", 80, 1, 1, List.of(
            new DecorationEffect(BuffType.ADJACENT_GROWTH, 0.05, 0.15, null)
    )),

    ROSE_BED("D02", "玫瑰花坛", 120, 1, 1, List.of(
            new DecorationEffect(BuffType.WATER_OPERATION_MULTIPLIER, 1.10, 1.10, null)
    )),

    WOODEN_FENCE("D03", "木栅栏", 50, 1, 1, List.of()),

    STREET_LAMP("D04", "路灯", 100, 1, 1, List.of()),

    BIG_TREE("D05", "大树", 200, 1, 1, List.of(
            new DecorationEffect(BuffType.GLOBAL_GROWTH, 0.03, 0.03, null)
    )),

    STONE_LANTERN("D06", "石灯笼", 150, 1, 1, List.of(
            new DecorationEffect(BuffType.WITHER_RESISTANCE, 0.70, 0.70, null)
    )),

    SMALL_FOUNTAIN("D07", "小喷泉", 250, 1, 1, List.of(
            new DecorationEffect(BuffType.FERTILIZER_OPERATION_MULTIPLIER, 1.20, 1.20, null)
    )),

    WHEAT_WATCHER("D08", "麦田守望者", 200, 1, 1, List.of(
            new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.WHEAT)
    )),

    CORN_HARVEST("D09", "玉米丰收", 200, 1, 1, List.of(
            new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.CORN)
    )),

    CARROT_FIELD("D10", "胡萝卜地", 200, 1, 1, List.of(
            new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.CARROT)
    )),

    GOLDEN_FOUNTAIN("D11", "金色喷泉", 500, 1, 1, List.of(
            new DecorationEffect(BuffType.GLOBAL_GROWTH, 0.05, 0.05, null)
    )),

    RAINBOW_FOUNTAIN("D12", "彩虹喷泉", 500, 1, 1, List.of(
            new DecorationEffect(BuffType.QUALITY_SCORE, 10, 10, null)
    )),

    GOLDEN_THRONE("D13", "金色王座", 600, 1, 1, List.of(
            new DecorationEffect(BuffType.PRICE_RATE, 0.10, 0.10, null)
    )),

    HARVEST_GODDESS("D14", "丰收女神像", 800, 1, 1, List.of(
            new DecorationEffect(BuffType.PRICE_RATE, 0.15, 0.15, null),
            new DecorationEffect(BuffType.QUALITY_SCORE, 5, 5, null)
    ));

    private final String id;
    private final String displayName;
    private final int price;
    private final int width;
    private final int height;
    private final List<DecorationEffect> effects;

    DecorationType(
            String id,
            String displayName,
            int price,
            int width,
            int height,
            List<DecorationEffect> effects
    ) {
        this.id = id;
        this.displayName = displayName;
        this.price = price;
        this.width = width;
        this.height = height;
        this.effects = effects;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getPrice() { return price; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<DecorationEffect> getEffects() { return effects; }

    /** 全部装饰基础购买成本合计（规则文档 §53，P1 装饰图鉴/成就预留）。 */
    public static int totalBaseCost() {
        int sum = 0;
        for (DecorationType type : values()) {
            sum += type.price;
        }
        return sum; // 3950
    }
}
 
 
5.4 结果枚举
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model.economy;

public enum DecorationPurchaseResult {
    SUCCESS,
    INSUFFICIENT_GOLD,
    INVALID_QUANTITY
}
 
 
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model.economy;

public enum DecorationPlacementResult {
    SUCCESS,
    OUT_OF_BOUNDS,
    NOT_DECORATION_AREA,
    CELL_OCCUPIED,
    INVALID_SIZE,
    NOT_OWNED,
    ALREADY_PLACED
}
 
 
 
6. 模型接口设计
6.1 Decoration / BasicDecoration
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

import java.util.UUID;

public interface Decoration {

    UUID getDecorationUuid();

    void setDecorationUuid(UUID decorationUuid);

    DecorationType getDecorationType();

    void setDecorationType(DecorationType decorationType);

    int getRow();

    void setRow(int row);

    int getColumn();

    void setColumn(int column);

    boolean isPlaced();

    void setPlaced(boolean placed);
}
 
 
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model.impl;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;

import java.util.UUID;

public class BasicDecoration implements Decoration {

    private UUID decorationUuid;
    private DecorationType decorationType;
    private int row = -1;
    private int column = -1;
    private boolean placed = false;

    public BasicDecoration() {
        // 供 E 反序列化使用。
    }

    public BasicDecoration(DecorationType type) {
        this.decorationUuid = UUID.randomUUID();
        this.decorationType = type;
    }

    @Override
    public UUID getDecorationUuid() { return decorationUuid; }

    @Override
    public void setDecorationUuid(UUID decorationUuid) {
        this.decorationUuid = decorationUuid;
    }

    @Override
    public DecorationType getDecorationType() { return decorationType; }

    @Override
    public void setDecorationType(DecorationType decorationType) {
        this.decorationType = decorationType;
    }

    @Override
    public int getRow() { return row; }

    @Override
    public void setRow(int row) { this.row = row; }

    @Override
    public int getColumn() { return column; }

    @Override
    public void setColumn(int column) { this.column = column; }

    @Override
    public boolean isPlaced() { return placed; }

    @Override
    public void setPlaced(boolean placed) { this.placed = placed; }
}
 
 
6.2 DecorationEffect（值对象）
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

/**
 * 装饰效果值对象（不可变）。
 *
 * @param buffType       具体 Buff 类型
 * @param value          效果数值（成长/品质/售价为加性；抗枯萎/操作乘数为乘性）
 * @param maxValue       该效果上限（如 D01 相邻成长最多 +15%）
 * @param targetCropType 仅 CROP_SPECIFIC_GROWTH 时非 null
 */
public record DecorationEffect(
        BuffType buffType,
        double value,
        double maxValue,
        CropType targetCropType
) {
}
 
 
6.3 BuffSnapshot（聚合结果）
java
 
 
复制
 
 
下载
package com.fieldstory.farm.model;

/**
 * 某位置/某作物的 Buff 聚合快照，由 BuffService 计算返回。
 *
 * <p>P1 套装加成全部为 0，字段预留以兼容 P3。
 */
public record BuffSnapshot(
        double growthRate,
        double qualityScoreBonus,
        double priceRate,
        double witherResistanceRate,
        double waterOperationMultiplier,
        double fertilizerOperationMultiplier,
        double setGrowthBonus,
        double setPriceRate,
        double setLegendaryBonus
) {
    /** P1 全默认值，便于测试与空实现。 */
    public static BuffSnapshot neutral() {
        return new BuffSnapshot(1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0);
    }
}
 
 
 
7. Service 接口设计
7.1 ShopService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import com.fieldstory.farm.model.economy.PurchaseResult;

/**
 * B 模块 P1 完整商店服务。
 *
 * <p>种子购买委托 P0 冻结的 {@code EconomyService.buySeed}，不重写（验收规范 §66）。
 * 装饰购买由本服务直接完成（扣金币 + 入库）。
 */
public interface ShopService {

    /**
     * 种子购买（委托 P0 EconomyService）。
     */
    PurchaseResult buySeed(CropType type, int quantity);

    /**
     * 装饰购买。成功后装饰进入 DecorationService 的未放置库存。
     *
     * <p>业务事务：扣金币 + 装饰入库，必须一次完成，不能一半成功一半失败。
     */
    DecorationPurchaseResult buyDecoration(DecorationType type, int quantity);

    /**
     * 判断金币是否足以购买 quantity 个指定装饰。
     */
    boolean canAffordDecoration(DecorationType type, int quantity);

    /**
     * 当前金币（只读）。
     */
    int getGold();
}
 
 
7.2 DecorationService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;

import java.util.List;

/**
 * 装饰实例与放置状态唯一状态源。
 */
public interface DecorationService {

    /** 所有已拥有装饰（含已放置与未放置）。 */
    List<Decoration> getOwnedDecorations();

    /** 所有已放置装饰（只有这些生效 Buff）。 */
    List<Decoration> getPlacedDecorations();

    int getOwnedCount(DecorationType type);

    int getPlacedCount(DecorationType type);

    /**
     * 购买后入库：返回新建的 Decoration 列表（未放置状态）。
     */
    List<Decoration> addPurchasedDecoration(DecorationType type, int quantity);

    boolean canPlace(Decoration decoration, int row, int column);

    DecorationPlacementResult place(Decoration decoration, int row, int column);

    DecorationPlacementResult move(Decoration decoration, int newRow, int newColumn);

    void removeFromFarm(Decoration decoration);

    boolean isDecorationArea(int row, int column);

    boolean isOccupied(int row, int column, Decoration ignore);
}
 
 
7.3 BuffService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service;

import com.fieldstory.farm.model.BuffSnapshot;
import com.fieldstory.farm.model.CropType;

/**
 * 装饰 Buff 唯一聚合入口。
 *
 * <p>供 A（成长/浇水/枯萎）、C（品质/售价/施肥）只读调用。
 * 只有已放置装饰生效（规则文档 §52）。
 */
public interface BuffService {

    BuffSnapshot getSnapshot(int row, int column, CropType cropType);

    double getGrowthRate(int row, int column, CropType cropType);

    double getQualityScoreBonus(int row, int column, CropType cropType);

    double getPriceRate(int row, int column, CropType cropType);

    double getWitherResistanceRate(int row, int column, CropType cropType);

    double getWaterOperationMultiplier(int row, int column, CropType cropType);

    double getFertilizerOperationMultiplier(int row, int column, CropType cropType);

    double getSetGrowthBonus();

    double getSetPriceRate();

    double getSetLegendaryBonus();
}
 
 
 
8. 实现类骨架
8.1 BasicShopService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import com.fieldstory.farm.model.economy.PurchaseResult;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.service.ShopService;
import com.fieldstory.farm.service.economy.EconomyService;

import java.util.Objects;

public class BasicShopService implements ShopService {

    private final EconomyService economyService;
    private final DecorationService decorationService;

    public BasicShopService(
            EconomyService economyService,
            DecorationService decorationService
    ) {
        this.economyService = Objects.requireNonNull(economyService);
        this.decorationService = Objects.requireNonNull(decorationService);
    }

    @Override
    public PurchaseResult buySeed(CropType type, int quantity) {
        return economyService.buySeed(type, quantity);
    }

    @Override
    public DecorationPurchaseResult buyDecoration(DecorationType type, int quantity) {
        Objects.requireNonNull(type, "decoration type cannot be null");
        if (quantity <= 0) {
            return DecorationPurchaseResult.INVALID_QUANTITY;
        }
        int totalPrice = type.getPrice() * quantity;
        if (!economyService.canAfford(totalPrice)) {
            return DecorationPurchaseResult.INSUFFICIENT_GOLD;
        }
        economyService.spendGold(totalPrice);
        decorationService.addPurchasedDecoration(type, quantity);
        return DecorationPurchaseResult.SUCCESS;
    }

    @Override
    public boolean canAffordDecoration(DecorationType type, int quantity) {
        if (type == null || quantity <= 0) {
            return false;
        }
        return economyService.canAfford(type.getPrice() * quantity);
    }

    @Override
    public int getGold() {
        return economyService.getGold();
    }
}
 
 
8.2 BasicDecorationService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmPlot;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import com.fieldstory.farm.model.impl.BasicDecoration;
import com.fieldstory.farm.service.DecorationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasicDecorationService implements DecorationService {

    private final Farm farm;
    private final List<Decoration> ownedDecorations = new ArrayList<>();

    public BasicDecorationService(Farm farm) {
        this.farm = Objects.requireNonNull(farm);
    }

    @Override
    public List<Decoration> getOwnedDecorations() {
        return List.copyOf(ownedDecorations);
    }

    @Override
    public List<Decoration> getPlacedDecorations() {
        return ownedDecorations.stream().filter(Decoration::isPlaced).toList();
    }

    @Override
    public int getOwnedCount(DecorationType type) {
        return (int) ownedDecorations.stream()
                .filter(d -> d.getDecorationType() == type)
                .count();
    }

    @Override
    public int getPlacedCount(DecorationType type) {
        return (int) ownedDecorations.stream()
                .filter(Decoration::isPlaced)
                .filter(d -> d.getDecorationType() == type)
                .count();
    }

    @Override
    public List<Decoration> addPurchasedDecoration(DecorationType type, int quantity) {
        List<Decoration> created = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Decoration decoration = new BasicDecoration(type);
            ownedDecorations.add(decoration);
            created.add(decoration);
        }
        return created;
    }

    @Override
    public boolean canPlace(Decoration decoration, int row, int column) {
        return placeCheck(decoration, row, column) == DecorationPlacementResult.SUCCESS;
    }

    @Override
    public DecorationPlacementResult place(Decoration decoration, int row, int column) {
        DecorationPlacementResult result = placeCheck(decoration, row, column);
        if (result != DecorationPlacementResult.SUCCESS) {
            return result;
        }
        decoration.setRow(row);
        decoration.setColumn(column);
        decoration.setPlaced(true);
        return DecorationPlacementResult.SUCCESS;
    }

    @Override
    public DecorationPlacementResult move(Decoration decoration, int newRow, int newColumn) {
        if (!ownedDecorations.contains(decoration)) {
            return DecorationPlacementResult.NOT_OWNED;
        }
        boolean oldPlaced = decoration.isPlaced();
        int oldRow = decoration.getRow();
        int oldColumn = decoration.getColumn();

        decoration.setPlaced(false);
        decoration.setRow(-1);
        decoration.setColumn(-1);

        DecorationPlacementResult result = placeCheck(decoration, newRow, newColumn);
        if (result != DecorationPlacementResult.SUCCESS) {
            decoration.setPlaced(oldPlaced);
            decoration.setRow(oldRow);
            decoration.setColumn(oldColumn);
            return result;
        }
        decoration.setRow(newRow);
        decoration.setColumn(newColumn);
        decoration.setPlaced(true);
        return DecorationPlacementResult.SUCCESS;
    }

    @Override
    public void removeFromFarm(Decoration decoration) {
        decoration.setPlaced(false);
        decoration.setRow(-1);
        decoration.setColumn(-1);
    }

    @Override
    public boolean isDecorationArea(int row, int column) {
        if (row < 0 || column < 0 || row >= farm.getHeight() || column >= farm.getWidth()) {
            return false;
        }
        return farm.getPlotType(row, column) == FarmPlot.DECORATION_AREA;
    }

    @Override
    public boolean isOccupied(int row, int column, Decoration ignore) {
        for (Decoration d : ownedDecorations) {
            if (!d.isPlaced() || d == ignore) {
                continue;
            }
            int w = d.getDecorationType().getWidth();
            int h = d.getDecorationType().getHeight();
            if (row >= d.getRow() && row < d.getRow() + h
                    && column >= d.getColumn() && column < d.getColumn() + w) {
                return true;
            }
        }
        return false;
    }

    private DecorationPlacementResult placeCheck(Decoration decoration, int row, int column) {
        if (!ownedDecorations.contains(decoration)) {
            return DecorationPlacementResult.NOT_OWNED;
        }
        DecorationType type = decoration.getDecorationType();
        int w = type.getWidth();
        int h = type.getHeight();

        if (row < 0 || column < 0
                || row + h > farm.getHeight()
                || column + w > farm.getWidth()) {
            return DecorationPlacementResult.OUT_OF_BOUNDS;
        }
        for (int r = row; r < row + h; r++) {
            for (int c = column; c < column + w; c++) {
                if (!isDecorationArea(r, c)) {
                    return DecorationPlacementResult.NOT_DECORATION_AREA;
                }
                if (isOccupied(r, c, decoration)) {
                    return DecorationPlacementResult.CELL_OCCUPIED;
                }
            }
        }
        return DecorationPlacementResult.SUCCESS;
    }
}
 
 
8.3 BasicBuffService
java
 
 
复制
 
 
下载
package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.BuffSnapshot;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;

import java.util.Objects;

/**
 * Buff 聚合实现。
 *
 * <p>所有查询只遍历已放置装饰；背包中不生效（规则文档 §52）。
 * P1 套装加成为 0，字段为 P3 预留。
 */
public class BasicBuffService implements BuffService {

    /** 成长类装饰 Rate 上限（规则文档 §55）。 */
    private static final double MAX_DECORATION_GROWTH_RATE = 1.5;

    private final DecorationService decorationService;

    public BasicBuffService(DecorationService decorationService) {
        this.decorationService = Objects.requireNonNull(decorationService);
    }

    @Override
    public BuffSnapshot getSnapshot(int row, int column, CropType cropType) {
        double adjacent = calcAdjacentGrowth(row, column);
        double global = calcGlobalGrowth();
        double cropSpecific = calcCropSpecificGrowth(cropType);
        double growthRate = Math.min(
                MAX_DECORATION_GROWTH_RATE,
                1.0 + adjacent + global + cropSpecific
        );

        return new BuffSnapshot(
                growthRate,
                calcQualityScoreBonus(),
                calcPriceRate(),
                calcWitherResistanceRate(),
                calcWaterOperationMultiplier(),
                calcFertilizerOperationMultiplier(),
                getSetGrowthBonus(),
                getSetPriceRate(),
                getSetLegendaryBonus()
        );
    }

    @Override
    public double getGrowthRate(int row, int column, CropType cropType) {
        return getSnapshot(row, column, cropType).growthRate();
    }

    @Override
    public double getQualityScoreBonus(int row, int column, CropType cropType) {
        return calcQualityScoreBonus();
    }

    @Override
    public double getPriceRate(int row, int column, CropType cropType) {
        return calcPriceRate();
    }

    @Override
    public double getWitherResistanceRate(int row, int column, CropType cropType) {
        return calcWitherResistanceRate();
    }

    @Override
    public double getWaterOperationMultiplier(int row, int column, CropType cropType) {
        return calcWaterOperationMultiplier();
    }

    @Override
    public double getFertilizerOperationMultiplier(int row, int column, CropType cropType) {
        return calcFertilizerOperationMultiplier();
    }

    @Override
    public double getSetGrowthBonus() {
        return 0.0; // P1 无套装，P3 接入
    }

    @Override
    public double getSetPriceRate() {
        return 0.0;
    }

    @Override
    public double getSetLegendaryBonus() {
        return 0.0;
    }

    // ==================== 内部聚合 ====================

    private double calcAdjacentGrowth(int row, int column) {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() != DecorationType.SUNFLOWER) {
                continue;
            }
            if (Math.abs(d.getRow() - row) <= 1 && Math.abs(d.getColumn() - column) <= 1) {
                total += 0.05;
            }
        }
        return Math.min(total, 0.15); // 最多统计 3 个有效向日葵
    }

    private double calcGlobalGrowth() {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.BIG_TREE) {
                total += 0.03;
            } else if (t == DecorationType.GOLDEN_FOUNTAIN) {
                total += 0.05;
            }
        }
        return total;
    }

    private double calcCropSpecificGrowth(CropType cropType) {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.WHEAT_WATCHER && cropType == CropType.WHEAT) {
                total += 0.10;
            } else if (t == DecorationType.CORN_HARVEST && cropType == CropType.CORN) {
                total += 0.10;
            } else if (t == DecorationType.CARROT_FIELD && cropType == CropType.CARROT) {
                total += 0.10;
            }
        }
        return total;
    }

    private double calcQualityScoreBonus() {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.RAINBOW_FOUNTAIN) {
                total += 10;
            } else if (t == DecorationType.HARVEST_GODDESS) {
                total += 5;
            }
        }
        return total;
    }

    private double calcPriceRate() {
        double total = 1.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.GOLDEN_THRONE) {
                total += 0.10;
            } else if (t == DecorationType.HARVEST_GODDESS) {
                total += 0.15;
            }
        }
        return total;
    }

    private double calcWitherResistanceRate() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.STONE_LANTERN) {
                return 0.70;
            }
        }
        return 1.0;
    }

    private double calcWaterOperationMultiplier() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.ROSE_BED) {
                return 1.10;
            }
        }
        return 1.0;
    }

    private double calcFertilizerOperationMultiplier() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.SMALL_FOUNTAIN) {
                return 1.20;
            }
        }
        return 1.0;
    }
}
 
 
 
9. Controller 与 View 设计
9.1 ShopController
包：com.fieldstory.farm.controller
职责：
text
 
 
复制
 
 
下载
接收 ShopView / ShopPopupView 的种子购买与装饰购买操作
调用 ShopService / EconomyService / DecorationService
查询金币、种子库存、装饰拥有数量
将 PurchaseResult / DecorationPurchaseResult 转为 UI 状态
 
 
禁止：
text
 
 
复制
 
 
下载
直接修改 Player
自己计算价格
自己扣金币
自己写 JSON / SQL
new Scene / new Stage / setRoot
 
 
9.2 DecorationController
包：com.fieldstory.farm.controller
职责：
text
 
 
复制
 
 
下载
接收装饰选择、放置、移动、移除操作
调用 DecorationService
调用 BuffService 刷新 Buff 快照
 
 
9.3 DecorationShopView
包：com.fieldstory.farm.view
职责：
text
 
 
复制
 
 
下载
显示 14 种装饰及其价格（DecorationType.getPrice()）
显示已拥有数量
触发装饰购买
显示购买结果
 
 
挂载方式：由 ShopPopupView 作为“装饰品商店”Tab 的内容嵌装，不直接挂到 SceneManager。
9.4 DecorationPanelView
包：com.fieldstory.farm.view
职责：
text
 
 
复制
 
 
下载
显示已拥有未放置装饰
显示已放置装饰
提供选择、移动、收回操作入口
 
 
 
10. 跨模块接口契约
10.1 与 A 土地与作物
 
 
A 消费 B
用途
BuffService.getGrowthRate(row, col, cropType)
A 的 GrowthService 获取 DecorationRate
BuffService.getWaterOperationMultiplier(...)
A 的 WateringService 调整浇水成长加成
BuffService.getWitherResistanceRate(...)
A 的 WitherService 计算枯萎概率
DecorationService.isDecorationArea(row, col)
判断某格是否为装饰区
Farm.getPlotType(row, col)
B 只读判断 DECORATION_AREA
⚠ 矛盾报告：A P0 的 GrowthService.applyGrowth(Crop, double)不携带作物位置，P1 无法直接查询相邻 Buff（D01 向日葵）。建议 A 在 P1 通过以下方式之一解决：
1. 新增 applyGrowth(Soil soil, double elapsedGameDays) 重载，由 Controller 从 Soil 反查 row/col；
2. 保留 P0 签名，Controller 层先根据 Soil 拿到 row/col，把 getGrowthRate(row, col, type) 结果以参数方式传入。
本设计不修改 A 的 P0 签名，请 A 模块在 P1 设计文档中裁定并同步。
B 不修改 Farm、Soil、Crop。
10.2 与 C 品质与传说 / 肥料
 
 
C 消费 B
用途
BuffService.getQualityScoreBonus(...)
QualityService 计算品质分
BuffService.getPriceRate(...)
售价计算
BuffService.getFertilizerOperationMultiplier(...)
C 的施肥成长效果修正
ShopService.buySeed(...)
种子购买仍走 P0 接口
肥料业务归 C。B 商店 P1 可预留肥料入口，不实现肥料库存与施肥公式。
10.3 与 D 世界环境
D 的 StatusView 只读金币。B 不依赖 WeatherService；天气倍率由 A 的成长公式组合。
10.4 与 E 存档与场景
E 负责：
text
 
 
复制
 
 
下载
DecorationDao
decoration 表持久化
SceneManager 挂载
GameState / SQLite 扩展
 
 
B 提供持久化字段：
text
 
 
复制
 
 
下载
decorationUuid  : String
decorationType  : String（枚举 name()）
row             : int
column          : int
placed          : boolean
 
 
B 不实现SaveService、JsonSaveService、DecorationDao。
 
11. 数值与规则溯源
 
 
数值/规则
值
来源
完整商店 P1
种子、装饰
验收规范 §65
种子购买仍调用 buySeed
不变
验收规范 §66
14 种装饰
D01~D14
规则文档 §53
装饰只有放置才生效
是
规则文档 §52
装饰区域
DECORATION_AREA
规则文档 §59
普通装饰
1×1
规则文档 §59
大型装饰
2×2
规则文档 §59
移动装饰
免费
规则文档 §59
Buff 五类
Growth/Quality/Price/WitherResistance/OperationModifier
规则文档 §54
向日葵
8 邻格 +5%，最多 +15%
规则文档 §53、§56
玫瑰花坛
浇水成长 ×1.10
规则文档 §53、§57
小喷泉
施肥成长 ×1.20
规则文档 §53、§58
石灯笼
枯萎概率 ×0.7
规则文档 §53、§31
彩虹喷泉
品质 +10
规则文档 §53
丰收女神像
售价 +15%，品质 +5
规则文档 §53
P1 SetBonus
0
验收规范 §70
成长装饰倍率上限
1.5
规则文档 §55
装饰总基础成本
3950 金币
规则文档 §53 合计
随机统一
RandomProvider
规则文档 §90
 
 
12. 单元测试要求
 
 
测试类
覆盖点
ShopServiceTest
购买装饰成功扣钱并加库存；金币不足失败且金币/库存不变；非法数量失败；种子购买仍委托 EconomyService.buySeed
DecorationServiceTest
只能放 DECORATION_AREA；越界拒绝；重叠拒绝；移动免费；收回后 Buff 不生效；NOT_OWNED 拒绝放置
BuffServiceTest
D01 相邻叠加最多 +15%；D05/D11 全局成长；D08/D09/D10 作物特定；D06 ×0.7；D13 ×1.10；D14 ×1.15 +5；P1 SetBonus=0；DecorationRate 封顶 1.5
DecorationTypeTest
14 种装饰数量、D01~D14 ID 连续性、totalBaseCost() == 3950、价格与规则文档 §53 一致
ShopControllerTest
不直接改 Player；价格只读 CropType.getSeedPrice() / DecorationType.getPrice()
DecorationPersistenceContractTest
字段名与 E 的 decoration 表契约一致（uuid/type/row/column/placed）
 
13. 跨模块矛盾与待裁定项（强制报告）
矛盾 1：A 的 GrowthService.applyGrowth 不携带位置，无法查询相邻 Buff
 
 
文档/代码
表述
A P0 §8.3
applyGrowth(Crop crop, double elapsedGameDays)
规则文档 §17
GrowthDelta = Base × Days × WeatherRate × DecorationRate × OperationRate × EventRate
规则文档 §53/§56
D01 向日葵效果为 8 邻格 +5%，需要作物位置
• 影响范围：A 模块 GrowthService 与 FarmController。
• B 模块处理：B 不修改 A 的 P0 签名，只提供 BuffService.getGrowthRate(row, col, cropType)；建议 A 在 P1 通过重载或 Controller 层补位置参数解决，由 A 模块（lyj）最终裁定并同步 A 设计文档。
• 待裁定人：A 模块（lyj）。
矛盾 2：旧 C 文档与最终模块分工冲突
 
 
文档
表述
旧《C 任务跨模块开发约束文档》
C 负责 Item、Inventory、ShopModel、ShopController、InventoryView、ShopView
《模块分工.md》P1 行
B = 完整商店、装饰 + Buff；C = 品质、肥料
• 分析：最终分工明确商店归 B，品质/肥料归 C。
• B 模块处理：按最终《模块分工.md》执行；本文档的 ShopService、DecorationService 归 B。
• 待裁定人：团队（跨模块协调），B 与 C 之间需确认肥料商品入口的归属。
矛盾 3：DecorationType 硬编码 vs. 配置化
 
 
文档
表述
规则文档 §53
14 种装饰数值为唯一事实源
验收规范 §47
P1 不得改变 P0 基础数值
A 文档 §6.4
提及 P1 后由 JsonConfigLoader 切换 crop-config.json
• 分析：P1 阶段装饰数值可硬编码；P4 平衡测试后再迁移 JSON。
• B 模块处理：DecorationType 硬编码为 P1 唯一数据源；P4 若迁移配置，接口签名不变。
• 待裁定人：团队（P4 平衡阶段）。
 
14. 结论
B 模块 P1 的边界严格限定为：
text
 
 
复制
 
 
下载
P0 经济接口保持冻结
+
完整商店 UI 与服务
+
14 种装饰
+
装饰购买/放置/移动/移除
+
5 类 Buff 聚合
 
 
它向 A/C 提供 Buff 查询，向 E 提供装饰持久化字段，向 UI 提供商店和装饰面板；
它不实现品质、肥料、天气、枯萎、事件、离线、套装、土地解锁、SQLite DAO 与收获事务。
核心原则：
text
 
 
复制
 
 
下载
EconomyService     = 金币与种子唯一业务入口（P0 冻结）
ShopService        = 完整商店入口，种子委托 EconomyService
DecorationService  = 装饰实例与放置唯一状态源
BuffService        = 装饰 Buff 唯一聚合入口
DecorationType     = P1 装饰价格、尺寸、效果唯一数据源
 
 
本文档为 B-P1-DESIGN v1.0 评审候选版。
请团队重点裁定 §15 矛盾 1（A 的 applyGrowth 位置参数），裁定后 B 的 BuffService 接口即可冻结。