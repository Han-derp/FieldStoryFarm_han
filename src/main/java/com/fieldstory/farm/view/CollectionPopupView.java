package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.CollectionController;
import javafx.scene.Node;
import javafx.stage.Popup;

import java.util.Objects;

/**
 * 收集图鉴弹窗（E 模块 P3；验收规范 §一百二十七）。
 *
 * <p>把 {@link CollectionView} 包进 {@link Popup}，由顶栏「图鉴」按钮在下方展开，
 * 与 B 的 {@code ShopPopupView} 一致：每次打开都 {@link CollectionView#refresh()}，
 * 避免关闭期间收获 / 购买 / 套装变化后仍显示旧进度。
 */
public final class CollectionPopupView extends Popup {

    private final CollectionView content;

    public CollectionPopupView(CollectionController controller) {
        this.content = new CollectionView(Objects.requireNonNull(controller, "controller 不能为空"));
        getContent().add(content);
        setAutoHide(true);
    }

    /** 在 owner 下方切换显示；已显示则收起。 */
    public void toggleBelow(Node owner) {
        if (isShowing()) {
            hide();
            return;
        }
        content.refresh();
        var bounds = owner.localToScreen(owner.getBoundsInLocal());
        if (bounds != null) {
            show(owner, bounds.getMinX(), bounds.getMaxY() + 4);
        }
    }

    /** 内嵌图鉴视图（供测试读取文本，不改变行为）。 */
    public CollectionView getCollectionView() {
        return content;
    }
}
