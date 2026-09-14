package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.HarvestLog;
import com.fieldstory.farm.service.LogService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link LogService} 基础实现（C 模块 品质与传说域，P2 收获事务第⑮步）。
 *
 * <p>内存日志表：按追加顺序（= 收获时间顺序）保存 HarvestLog；
 * 返回快照为只读列表，外部不可修改内部状态。
 */
public class BasicLogService implements LogService {

    /** 收获日志注册表（按追加顺序） */
    private final List<HarvestLog> logs = new ArrayList<>();

    @Override
    public void append(HarvestLog log) {
        logs.add(Objects.requireNonNull(log, "日志不能为空"));
    }

    @Override
    public List<HarvestLog> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }
}
