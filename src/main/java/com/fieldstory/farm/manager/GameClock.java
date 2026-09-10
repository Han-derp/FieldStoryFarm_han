package com.fieldstory.farm.manager;

/**
 * 临时桩，D 交付后删除本文件。
 *
 * <p>GameClock 正式接口由 D 模块（世界环境，zsl）交付，按《D模块 P0 接口与类设计文档》
 * §1.1 落位于 {@code com.fieldstory.farm.model.GameClock}。本桩仅为 A 模块
 * （播种）先行开发提供最小依赖面：仅含 A 所需 2 个方法，方法签名严格按
 * 《D模块 P0 接口与类设计文档》§1.2。
 *
 * <p>D 交付后删除本文件，{@code service.impl.BasicPlantingService} 的导入与
 * {@code testutil.TestGameClock} 一并切换为 D 的正式接口
 * （A 模块设计文档 §12.3 消费约定）。
 */
public interface GameClock {

    /**
     * 当前游戏日（《D模块 P0 接口与类设计文档》§1.2）。
     *
     * @return 游戏日序号
     */
    int getGameDay();

    /**
     * 当前游戏小时（《D模块 P0 接口与类设计文档》§1.2）。
     *
     * @return 当日游戏小时
     */
    int getGameHour();
}
