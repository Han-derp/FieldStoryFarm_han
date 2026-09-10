package com.fieldstory.farm.model;

/**
 * 生长阶段枚举（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§6.3。
 */
public enum GrowthStage {

    /** [0, 20)，不可浇水，不可收获。 */
    SEED,

    /** [20, 50)。 */
    SPROUT,

    /** [50, 100)。 */
    GROWING,

    /** >=100 且封顶，可浇水，可收获。 */
    MATURE,

    /** 占位，P1 起枯萎系统。 */
    WITHERED
}
