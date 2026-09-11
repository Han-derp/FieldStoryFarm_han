package com.fieldstory.farm.controller;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;

/**
 * 装饰选择上下文（P1 装配层内部使用）。
 *
 * <p>记录当前"待放置"的装饰；由仓库弹窗选择操作写入，
 * 由 FarmView 装饰区点击回调读取并执行放置。
 */
public final class DecorationSelectionContext {

    private static Decoration pendingDecoration;
    private static DecorationController controller;

    private DecorationSelectionContext() {
    }

    /** 由 FarmBootstrap 在装配时注入 controller。 */
    public static void install(DecorationController ctrl) {
        controller = ctrl;
        pendingDecoration = null;
    }

    /** 由仓库弹窗选择时调用，记录待放置的装饰。 */
    public static void select(Decoration decoration) {
        pendingDecoration = decoration;
    }

    /**
     * 尝试在指定格放置当前选中的装饰。
     *
     * @return 放置结果；未选择装饰或未装配时返回 null。
     */
    public static DecorationPlacementResult tryPlaceAt(int row, int column) {
        if (pendingDecoration == null || controller == null) {
            return null;
        }
        DecorationPlacementResult result = controller.place(pendingDecoration, row, column);
        if (result == DecorationPlacementResult.SUCCESS) {
            pendingDecoration = null;
        }
        return result;
    }

    public static void clear() {
        pendingDecoration = null;
    }
}