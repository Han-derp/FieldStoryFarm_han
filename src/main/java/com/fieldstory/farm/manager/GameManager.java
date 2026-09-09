package com.fieldstory.farm.manager;

import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.service.SaveService;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 游戏管理器（E 存档与引擎模块，脚手架 §七 manager 包）。
 *
 * <p>负责 P0 游戏生命周期（模块分工.md E 行 P0：GameManager）：
 * <ul>
 *   <li>启动：存在存档 → {@link SaveService#load()} 恢复退出瞬间状态；否则新建游戏；</li>
 *   <li>启动加载阶段禁止执行任何离线作物成长计算（离线推进属 P2，计划书 §P0禁止清单）；</li>
 *   <li>保存：任何关键操作完成/退出时调用 {@link #saveNow(LocalDateTime)}，
 *       把“金币、种子库存、GameClock 世界时间、土地与作物全部数据”写入存档（验收 §41-§42）；</li>
 *   <li>新游戏初始金币 500（验收规范 §三十五）。</li>
 * </ul>
 *
 * <p>本类只做生命周期与存档编排，不做开垦/买种/播种/浇水/收获等业务规则——
 * 业务规则由 A/B/C 模块对应 Service 负责，Manager 不越层实现。
 */
public class GameManager {

    private static final String DEFAULT_PLAYER_NAME = "农夫";
    private static final int INITIAL_GOLD = 500;

    private final SaveService saveService;
    private GameState state;

    public GameManager(SaveService saveService) {
        this.saveService = Objects.requireNonNull(saveService, "SaveService 不能为空");
    }

    /**
     * 启动游戏：
     * <pre>
     * 有存档 → 读取 JSON 恢复到退出瞬间（不做离线成长）
     * 无存档 → 新建游戏（金币 500，中心 8×8 EMPTY 土地）
     * </pre>
     */
    public GameState start() {
        if (saveService.hasSave()) {
            state = saveService.load();
        }
        if (state == null) {
            state = newGame();
        }
        return state;
    }

    /** 当前是否存在可恢复的存档。 */
    public boolean hasSavedGame() {
        return saveService.hasSave();
    }

    /** 当前游戏状态；未调用 {@link #start()} 前调用会抛出状态异常。 */
    public GameState currentState() {
        if (state == null) {
            throw new IllegalStateException("游戏尚未启动：请先调用 start()");
        }
        return state;
    }

    /**
     * 关键操作完成后的自动保存入口（验收标准 1）。
     *
     * @param currentWorldTime 此刻 GameClock 的世界时间（由 D 模块时钟提供；
     *                         骨架阶段允许传入当前状态已记录的时间）
     */
    public void saveNow(LocalDateTime currentWorldTime) {
        GameState current = currentState();
        if (currentWorldTime != null) {
            current.setCurrentWorldTime(currentWorldTime);
        }
        saveService.save(current);
    }

    /**
     * 退出游戏：保存当前状态并记录退出世界时间后退出（验收标准 2）。
     * 保存后本管理器不推进世界——退出后不推进属于 P2 离线模拟（验收规范 §42）。
     */
    public void saveAndExit(LocalDateTime currentWorldTime) {
        saveNow(currentWorldTime);
    }

    /** 新建游戏：金币 500、无种子、中心 8×8 空土地、世界时间未开始（由 GameClock 接管）。 */
    public static GameState newGame() {
        Player player = new Player(DEFAULT_PLAYER_NAME, INITIAL_GOLD);
        Farm farm = new Farm();
        return new GameState(player, farm, null);
    }
}
