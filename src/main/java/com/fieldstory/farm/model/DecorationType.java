package com.fieldstory.farm.model;

import java.util.List;

/**
 * 14 种装饰（规则文档 §53）。
 * P1 阶段硬编码为唯一数据源；P4 平衡阶段可迁移为 JSON 配置。
 */
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

    DecorationType(String id, String displayName, int price,
                   int width, int height, List<DecorationEffect> effects) {
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

    /** 全部装饰基础购买成本合计（规则文档 §53 = 3950）。 */
    public static int totalBaseCost() {
        int sum = 0;
        for (DecorationType type : values()) {
            sum += type.price;
        }
        return sum;
    }
}