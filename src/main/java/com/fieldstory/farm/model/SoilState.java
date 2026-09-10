package com.fieldstory.farm.model;

/**
 * 土壤状态枚举（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§6.2。
 */
public enum SoilState {

    /** 可开垦。 */
    EMPTY,

    /** 可播种。 */
    TILLED,

    /** 可浇水/收获/铲除。 */
    PLANTED,

    /** 占位，P3 起启用。 */
    LOCKED
}
