package com.fieldstory.farm.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * P0统一游戏时钟。
 *
 * 关键约束：
 * 1. now() 只读取“已经提交”的世界时间，不读取系统时间来偷偷推进世界；
 * 2. 只有统一世界循环允许调用 advance(realElapsed)；
 * 3. Controller / View / SaveService 不得调用 advance()。
 */
public interface GameClock {
    LocalDateTime now();

    default LocalDate currentGameDay() {
        return now().toLocalDate();
    }

    /**
     * 根据一段现实时间推进世界时间。
     * 正式/演示/测试实现可使用不同倍率。
     */
    void advance(Duration realElapsed);
}
