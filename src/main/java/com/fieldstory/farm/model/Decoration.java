package com.fieldstory.farm.model;

import java.util.UUID;

/**
 * 装饰实例状态（统一 Model 原则：只保存状态，无业务判断）。
 */
public interface Decoration {

    UUID getDecorationUuid();

    void setDecorationUuid(UUID decorationUuid);

    DecorationType getDecorationType();

    void setDecorationType(DecorationType decorationType);

    /** 放置行；未放置时为 -1。 */
    int getRow();

    void setRow(int row);

    /** 放置列；未放置时为 -1。 */
    int getColumn();

    void setColumn(int column);

    boolean isPlaced();

    void setPlaced(boolean placed);
}