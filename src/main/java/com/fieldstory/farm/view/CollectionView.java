package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.CollectionController;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 收集图鉴界面（E 模块 P3；验收规范 §一百二十七/§一百二十八）。
 *
 * <p>至少显示规则要求六项（验收规范 §一百二十七）：
 * <pre>
 * 作物图鉴  x/15
 * 装饰图鉴  x/14
 * 传说      x/3
 * 套装      x/3
 * FarmScore x/147
 * 当前评价
 * </pre>
 * 并附目标提示，未完成目标给出可读条件（验收规范 §一百二十八）。
 *
 * <p>本视图只读 {@link CollectionController}，不写状态；每次 {@link #refresh()} 重新拉取，
 * 因此收获 / 购买 / 套装变化后调用一次即可反映最新进度。样式遵循 UI 美术规范配色表背景
 * {@code #FFF3DD}、木色边框 {@code #8B5E3C}、深棕字 {@code #493526}。
 */
public final class CollectionView extends VBox {

    private static final String PANEL_STYLE = "-fx-background-color: #FFF3DD;"
            + "-fx-background-radius: 12;"
            + "-fx-border-color: #8B5E3C;"
            + "-fx-border-radius: 12;"
            + "-fx-border-width: 2;";

    private static final String TITLE_STYLE = "-fx-text-fill: #493526; -fx-font-size: 18;";
    private static final String LINE_STYLE = "-fx-text-fill: #493526; -fx-font-size: 14;";
    private static final String GOAL_STYLE = "-fx-text-fill: #8B5E3C; -fx-font-size: 12;";

    private final CollectionController controller;

    private final Label cropLabel = new Label();
    private final Label decorationLabel = new Label();
    private final Label legendaryLabel = new Label();
    private final Label setLabel = new Label();
    private final Label farmScoreLabel = new Label();
    private final Label rankLabel = new Label();
    private final VBox goalBox = new VBox(4);

    /** 绑定控制器构造并完成首帧渲染。 */
    public CollectionView(CollectionController controller) {
        this.controller = Objects.requireNonNull(controller, "controller 不能为空");

        setSpacing(8);
        setPadding(new Insets(12));
        setStyle(PANEL_STYLE);
        setPrefWidth(360);

        Label title = new Label("收集图鉴");
        title.setStyle(TITLE_STYLE);

        for (Label label : new Label[]{
                cropLabel, decorationLabel, legendaryLabel,
                setLabel, farmScoreLabel, rankLabel}) {
            label.setStyle(LINE_STYLE);
        }

        Label goalTitle = new Label("目标提示");
        goalTitle.setStyle(LINE_STYLE);

        getChildren().addAll(
                title,
                cropLabel, decorationLabel, legendaryLabel, setLabel,
                farmScoreLabel, rankLabel,
                goalTitle, goalBox);

        refresh();
    }

    /** 重新拉取进度并刷新全部文本（收获 / 购买 / 套装变化后调用）。 */
    public void refresh() {
        cropLabel.setText("作物图鉴  " + controller.cropCollected() + "/" + controller.cropTarget());
        decorationLabel.setText("装饰图鉴  " + controller.decorationCollected()
                + "/" + controller.decorationTarget());
        legendaryLabel.setText("传说  " + controller.legendaryCollected()
                + "/" + controller.legendaryTarget());
        setLabel.setText("套装  " + controller.setCollected() + "/" + controller.setTarget());
        farmScoreLabel.setText("FarmScore  " + controller.farmScore() + "/" + controller.maxFarmScore());
        rankLabel.setText("当前评价  " + controller.currentRankName());

        goalBox.getChildren().clear();
        for (String hint : controller.goalHints()) {
            Label item = new Label(hint);
            item.setStyle(GOAL_STYLE);
            item.setWrapText(true);
            goalBox.getChildren().add(item);
        }
    }

    // ------------------------------------------------------------------
    // 只读访问（供测试断言，不改变行为）
    // ------------------------------------------------------------------

    public String cropText() {
        return cropLabel.getText();
    }

    public String decorationText() {
        return decorationLabel.getText();
    }

    public String legendaryText() {
        return legendaryLabel.getText();
    }

    public String setText() {
        return setLabel.getText();
    }

    public String farmScoreText() {
        return farmScoreLabel.getText();
    }

    public String rankText() {
        return rankLabel.getText();
    }

    /** 目标提示文本列表（顺序与 {@link CollectionController#goalHints()} 一致）。 */
    public List<String> goalTexts() {
        List<String> texts = new ArrayList<>();
        for (Node node : goalBox.getChildren()) {
            if (node instanceof Label label) {
                texts.add(label.getText());
            }
        }
        return texts;
    }
}
