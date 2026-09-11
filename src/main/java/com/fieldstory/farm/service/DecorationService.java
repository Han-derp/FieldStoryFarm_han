package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;

import java.util.List;

/**
 * 装饰实例与放置状态唯一状态源。
 */
public interface DecorationService {

    List<Decoration> getOwnedDecorations();

    List<Decoration> getPlacedDecorations();

    int getOwnedCount(DecorationType type);

    int getPlacedCount(DecorationType type);

    List<Decoration> addPurchasedDecoration(DecorationType type, int quantity);

    boolean canPlace(Decoration decoration, int row, int column);

    DecorationPlacementResult place(Decoration decoration, int row, int column);

    DecorationPlacementResult move(Decoration decoration, int newRow, int newColumn);

    void removeFromFarm(Decoration decoration);

    boolean isDecorationArea(int row, int column);

    boolean isOccupied(int row, int column, Decoration ignore);
}