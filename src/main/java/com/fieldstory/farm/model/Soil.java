package com.fieldstory.farm.model;

/**
 * 土壤接口（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§7.2。实现类 {@code BasicSoil} 由 A 模块提供。
 *
 * <p>设计原则：Model 只描述"对象当前状态"，全部 setter 由 Service 或 E 模块切换使用，
 * 不含任何业务判断。
 */
public interface Soil {

    /**
     * 获取土壤唯一标识。
     *
     * @return 土壤 id
     */
    long getId();

    /**
     * 获取所在行（0-based）。
     *
     * @return 行
     */
    int getRow();

    /**
     * 获取所在列（0-based）。
     *
     * @return 列
     */
    int getColumn();

    /**
     * 获取土壤状态。
     *
     * @return 土壤状态
     */
    SoilState getState();

    /**
     * 设置土壤状态。
     *
     * @param state 土壤状态
     */
    void setState(SoilState state);

    /**
     * 获取作物；非 PLANTED 时返回 null。
     *
     * @return 作物，无作物返回 null
     */
    Crop getCrop();

    /**
     * 设置作物。
     *
     * @param crop 作物
     */
    void setCrop(Crop crop);
}
