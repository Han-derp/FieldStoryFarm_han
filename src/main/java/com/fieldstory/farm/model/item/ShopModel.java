package com.fieldstory.farm.model.item;

import com.fieldstory.farm.model.Player;

import java.util.Objects;

/**
 * 商店模型（C 商店模块）：物品买卖与金币校验。
 *
 * <p>对外接口（《C任务跨模块开发约束文档》§九，签名已锁定，不得随意改动）：
 * <ul>
 *   <li>{@link #buyItem(Item, Player)}：购买（扣金币，成功后由调用方入包）；</li>
 *   <li>{@link #sellItem(Item, Player)}：出售（加金币，成功后由调用方移除物品）。</li>
 * </ul>
 *
 * <p>金币唯一数据源：{@link Player#getGold()} / {@link Player#setGold(int)}
 * （《接口要求清单》§D4：改钱一律走 player，禁止维护第二份金币）。
 *
 * <p>P0 范围：只售三种种子（验收规范 §十九「最小购买入口」，P1 扩展完整商店）。
 */
public class ShopModel {

    /** 购买失败提示（金币不足），UI 层展示用。 */
    public static final String MSG_GOLD_NOT_ENOUGH = "金币不足";

    /**
     * 购买物品：校验金币后扣除。
     *
     * <p>对外接口；签名已锁定。背包入库由调用方在返回 true 后执行
     * {@link Inventory#addItem(Item)}。
     *
     * @param item   待购物品（单价 = item.getUnitPrice()）
     * @param player 玩家（金币唯一数据源）
     * @return 金币足够且扣款成功返回 true；金币不足或参数非法返回 false
     */
    public boolean buyItem(Item item, Player player) {
        if (!validate(item, player)) {
            return false;
        }
        int total = item.totalPrice();
        if (!canAfford(player, total)) {
            return false;
        }
        player.setGold(player.getGold() - total);
        return true;
    }

    /**
     * 出售物品：按单件价格结算金币。
     *
     * <p>对外接口；签名已锁定。物品移除由调用方在返回 true 后执行
     * {@link Inventory#removeItem(int)}。
     *
     * @param item   待售物品
     * @param player 玩家（金币唯一数据源）
     * @return 结算成功返回 true；参数非法返回 false
     */
    public boolean sellItem(Item item, Player player) {
        if (!validate(item, player)) {
            return false;
        }
        player.setGold(player.getGold() + item.totalPrice());
        return true;
    }

    /** 玩家金币是否足以支付 amount。 */
    public boolean canAfford(Player player, int amount) {
        return player.getGold() >= amount;
    }

    /** 购买数量为 n 的总价（n × 单件价格）。 */
    public int priceFor(ItemType type, int quantity) {
        Objects.requireNonNull(type, "物品类型不能为空");
        if (quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须为正数: " + quantity);
        }
        return type.getDefaultPrice() * quantity;
    }

    private boolean validate(Item item, Player player) {
        return item != null && player != null && item.getUnitPrice() > 0;
    }
}
