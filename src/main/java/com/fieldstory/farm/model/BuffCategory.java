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