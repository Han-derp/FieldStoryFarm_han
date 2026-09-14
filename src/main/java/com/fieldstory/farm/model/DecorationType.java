package com.fieldstory.farm.model;

import java.util.List;

/**
 * B 模块 P1 的 14 种装饰定义。
 *
 * <p>价格和效果严格来自 V4.0 规则文档。当前规则文档只规定“普通 1x1 / 大型 2x2”，
 * 但没有给出 D01~D14 的逐项尺寸映射，因此本版本把 footprint 独立保留为字段。
 * 尺寸已按团队冻结决策（D32）落实：D08~D14 为 2x2（占 4 格），D01~D07 保持 1x1；
 * 冻结只改 width/height，不改 Service 接口。放置校验与渲染已原生按 footprint 处理。
 */
public enum DecorationType {

    SUNFLOWER("D01", "向日葵", 80, "sunflower.png", 1, 1,
            List.of(new DecorationEffect(BuffType.ADJACENT_GROWTH, 0.05, 0.15, null))),

    ROSE_BED("D02", "玫瑰花坛", 120, "rose_red.png", 1, 1,
            List.of(new DecorationEffect(BuffType.WATER_OPERATION_MULTIPLIER, 1.10, 1.10, null))),

    WOODEN_FENCE("D03", "木栅栏", 50, "wooden_fence.png", 1, 1, List.of()),

    STREET_LAMP("D04", "路灯", 100, "street_lamp.png", 1, 1, List.of()),

    BIG_TREE("D05", "大树", 200, "big_tree.png", 1, 1,
            List.of(new DecorationEffect(BuffType.GLOBAL_GROWTH, 0.03, 0.03, null))),

    STONE_LANTERN("D06", "石灯笼", 150, "stone_lantern.png", 1, 1,
            List.of(new DecorationEffect(BuffType.WITHER_RESISTANCE, 0.70, 0.70, null))),

    SMALL_FOUNTAIN("D07", "小喷泉", 250, "small_fountain.png", 1, 1,
            List.of(new DecorationEffect(BuffType.FERTILIZER_OPERATION_MULTIPLIER, 1.20, 1.20, null))),

    WHEAT_WATCHER("D08", "麦田守望者", 200, "wheat_watcher.png", 2, 2,
            List.of(new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.WHEAT))),

    CORN_HARVEST("D09", "玉米丰收", 200, "corn_harvest.png", 2, 2,
            List.of(new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.CORN))),

    CARROT_FIELD("D10", "胡萝卜地", 200, "carrot_field.png", 2, 2,
            List.of(new DecorationEffect(BuffType.CROP_SPECIFIC_GROWTH, 0.10, 0.10, CropType.CARROT))),

    GOLDEN_FOUNTAIN("D11", "金色喷泉", 500, "golden_fountain.png", 2, 2,
            List.of(new DecorationEffect(BuffType.GLOBAL_GROWTH, 0.05, 0.05, null))),

    RAINBOW_FOUNTAIN("D12", "彩虹喷泉", 500, "rainbow_fountain.png", 2, 2,
            List.of(new DecorationEffect(BuffType.QUALITY_SCORE, 10, 10, null))),

    GOLDEN_THRONE("D13", "金色王座", 600, "golden_throne.png", 2, 2,
            List.of(new DecorationEffect(BuffType.PRICE_RATE, 0.10, 0.10, null))),

    HARVEST_GODDESS("D14", "丰收女神像", 800, "havest_goddess.png", 2, 2,
            List.of(
                    new DecorationEffect(BuffType.PRICE_RATE, 0.15, 0.15, null),
                    new DecorationEffect(BuffType.QUALITY_SCORE, 5, 5, null)
            ));

    private final String id;
    private final String displayName;
    private final int price;
    private final String assetFileName;
    private final int width;
    private final int height;
    private final List<DecorationEffect> effects;

    DecorationType(String id,
                   String displayName,
                   int price,
                   String assetFileName,
                   int width,
                   int height,
                   List<DecorationEffect> effects) {
        this.id = id;
        this.displayName = displayName;
        this.price = price;
        this.assetFileName = assetFileName;
        this.width = width;
        this.height = height;
        this.effects = List.copyOf(effects);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrice() {
        return price;
    }

    public String getAssetFileName() {
        return assetFileName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<DecorationEffect> getEffects() {
        return effects;
    }

    public static int totalBaseCost() {
        int total = 0;
        for (DecorationType type : values()) {
            total += type.price;
        }
        return total;
    }
}
