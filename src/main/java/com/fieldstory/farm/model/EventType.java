package com.fieldstory.farm.model;

/**
 * 随机事件类型枚举（D 模块 P0：世界环境，P2 预留）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§8.2、《P0-P4功能实现与验收规范》§90。
 *
 * <p><b>P0 约束：</b>本阶段只允许定义枚举常量，<b>禁止</b>在 P0 业务代码中引用
 * （验收规范 §10：P0 不实现随机事件，世界环境固定 {@code EventRate = 1.0}）。
 * 随机事件系统自 P2 起启用。
 */
public enum EventType {

    /** 流星夜（P2 启用，验收规范 §92）。 */
    METEOR_SHOWER,

    /** 神秘商人（P2 启用，验收规范 §92）。 */
    MYSTERY_MERCHANT,

    /** 小动物来访（P2 启用，验收规范 §92）。 */
    ANIMAL_VISIT,

    /** 彩虹日（P2 启用，验收规范 §92）。 */
    RAINBOW_DAY,

    /** 无事件（P0/P1 默认值）。 */
    NONE
}
