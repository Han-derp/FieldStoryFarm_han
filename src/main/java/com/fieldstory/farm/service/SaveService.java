package com.fieldstory.farm.service;

import com.fieldstory.farm.model.GameState;

/**
 * 存档服务接口（P0 Service 归属，验收规范 §3.2 / §四十）。
 *
 * <p>接口约定（验收规范 §39-§42）：
 * <ul>
 *   <li>业务层与 Controller 一律通过本接口读写存档；</li>
 *   <li>Controller 不得知道 JSON 文件在哪里（§39）；</li>
 *   <li>P0 实现为 {@code JsonSaveService}，P1 替换为 {@code SqliteSaveService}，
 *       业务层调用方式不变（§40）；</li>
 *   <li>退出必须完整保存；重新进入恢复到退出瞬间，不做离线推进（§42，离线模拟属 P2）。</li>
 * </ul>
 */
public interface SaveService {

    /** 将全部游戏状态持久化。 */
    void save(GameState state);

    /**
     * 读取存档并恢复全部状态到退出瞬间。
     *
     * @return 恢复后的完整游戏状态；不存在有效存档时返回 {@code null}
     */
    GameState load();

    /** 是否存在有效存档。 */
    boolean hasSave();
}
