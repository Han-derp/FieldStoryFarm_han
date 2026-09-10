package com.fieldstory.farm.model;


/**
 * 作物类型。
 *
 * 同时作为：
 * 1.种子类型
 * 2.价格数据源
 */
public enum CropType {


    WHEAT(
            10,
            50
    ),


    CORN(
            15,
            70
    ),


    CARROT(
            20,
            60
    );


    /**
     * 种子价格。
     */
    private final int seedPrice;


    /**
     * 基础出售价格。
     */
    private final int baseSellPrice;



    CropType(
            int seedPrice,
            int baseSellPrice
    ){

        this.seedPrice = seedPrice;

        this.baseSellPrice = baseSellPrice;
    }



    public int getSeedPrice(){

        return seedPrice;
    }



    public int getBaseSellPrice(){

        return baseSellPrice;
    }

}
