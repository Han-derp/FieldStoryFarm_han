package com.fieldstory.farm.service;

import com.fieldstory.farm.model.HarvestLog;

import java.util.List;

/**
 * 收获日志服务接口（C 模块 品质与传说域，P2 收获事务第⑮步）。
 *
 * <p>规则文档 §六十八 ⑮：收获事务提交前写入 HarvestLog；
 * 验收规范 §一百零三"记录HarvestLog"、§一百零五 离线日志 UI 数据来源。
 *
 * <p>P2 使用内存态；持久化由 E 存档统一处理（与 CropMemory 同模式）。
 */
public interface LogService {

    /**
     * 追加一条收获日志（成功收获事务提交前调用，按收获时间顺序保存）。
     *
     * @param log 收获日志（不可为 null）
     */
    void append(HarvestLog log);

    /**
     * 全部日志快照（只读，按追加顺序 = 收获时间顺序）。
     *
     * @return 日志列表
     */
    List<HarvestLog> listAll();
}
