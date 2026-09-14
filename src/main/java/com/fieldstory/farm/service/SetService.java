package com.fieldstory.farm.service;

import com.fieldstory.farm.model.DecorationSet;

import java.util.List;
import java.util.Set;

/**
 * P3 套装服务：判定三套装饰套装的「已收集 / 已激活」状态（验收规范 §一百一十四~§一百一十八）。
 *
 * <p>两个状态必须彼此独立（验收规范 §一百一十八）：
 * <ul>
 *   <li><b>setCollected</b>（{@link #isCollected}）：曾经「全部成员拥有且放置」而完整完成，
 *       永久保留，决定 FarmScore 套装分是否计入；</li>
 *   <li><b>setActive</b>（{@link #isActive}）：当前全部成员仍然放置，决定套装 Buff 是否生效。</li>
 * </ul>
 * 因此玩家完成套装后收走任一成员：{@code collected=true} 但 {@code active=false}，
 * FarmScore 仍保留套装分，而 Buff 立即停止（验收规范 §一百一十八）。
 *
 * <p>本服务只读写 {@code GameState.setCollection} 与只读 {@code DecorationService}，
 * 不在内部保存任何状态（统一 Model 原则）。
 */
public interface SetService {

    /** 三种套装（固定顺序，与 {@link DecorationSet} 枚举一致）。 */
    List<DecorationSet> allSets();

    /** 是否曾经完整完成过该套装（永久，验收规范 §一百一十八）。 */
    boolean isCollected(DecorationSet set);

    /** 该套装当前是否全部成员仍放置、Buff 是否生效。 */
    boolean isActive(DecorationSet set);

    /** 已收集套装数（0~3），用于 FarmScore 套装分与图鉴进度。 */
    int collectedCount();

    /** 当前生效套装数（0~3）。 */
    int activeCount();

    /** 已收集套装 id 集合（只读快照）。 */
    Set<String> getCollectedSetIds();

    /** 当前生效套装 id 集合（只读快照）。 */
    Set<String> getActiveSetIds();

    /** 当前生效的套装 Buff 描述；拆走任一成员后对应项立即消失（验收规范 §一百一十八）。 */
    List<String> getActiveBuffDescriptions();

    /**
     * 依据当前装饰「拥有 / 放置」状态重算两个状态：
     * 全部成员拥有且放置 → 加入 collected（永久）与 active；否则仅移除 active，collected 不回退。
     */
    void refresh();
}
