# A模块-P1作物贴图接入决策记录

> 模块：A（视图层，com.fieldstory.farm.view）
> 阶段：P1 作物贴图接入（纯函数层：枚举 + 帧映射）
> 关联实现：`CropSpriteSheet`（图集元数据枚举）、`FarmView#cropFrameIndexFor`（阶段→帧索引纯函数）
> 关联规范：FSFUI布局与美术设计规范 §7 Tile组合策略、§8 作物资源规范

---

## D1：统一 2× 整数倍放大，底对齐允许向上越界

- **结论**：作物贴图按 2× 整数倍放大显示；以格子底边为基准底对齐，放大后超出格子上边界的部分允许向上越界绘制。
- **背景**：小麦（18×32）、玉米（16×32）2× 放大后高度为 64px，超出单格 44px 的格子高度；胡萝卜（16×16）2× 后 32×32 不越界。
- **做法**：经确认采用星露谷式做法——作物高于格子时底对齐、向上溢出，不做裁切也不缩小贴图（保持像素风整数倍缩放）。
- **本卡范围**：本卡为纯函数/纯数据层，不实现渲染；该策略仅在此记录，渲染接入卡按此执行。

## D2：MATURE 显示作物末帧贴图，保留整格高亮底色

- **结论**：MATURE 阶段显示作物图集末帧（totalFrames-1）贴图，同时保留现有整格高亮底色（#E8C45C，UI规范 §14），取代 P0「MATURE 只显示高亮不画作物块」的占位表现。
- **对应实现**：`cropFrameIndexFor(MATURE, totalFrames) → totalFrames - 1`。
- **背景**：P0 中 MATURE 返回 0 不绘制作物块（见 `cropBlockSizeFor`）；P1 接入贴图后改为显示成熟贴图 + 高亮底色，收获提示仍依赖高亮色。

## D3：WITHERED 不显示贴图，沿用整格枯萎色（D17 候选色 A）

- **结论**：WITHERED 阶段不显示任何作物贴图，仅保留整格枯萎底色 #857766（D17 候选色 A，见 `FarmView.COLOR_WITHERED`）。
- **对应实现**：`cropFrameIndexFor(WITHERED, totalFrames) → -1`，-1 约定为「不显示贴图」哨兵。
- **背景**：与 P0 的 `tileColorFor` 枯萎色处理一致（D17 候选色 A 待团队确认，如有变更仅改 `COLOR_WITHERED` 常量，本决策不受影响）。

---

## 附：帧映射数字依据（溯源说明）

- **帧映射 0/2/4/末帧**：依据 UI规范 §7 Tile组合策略（作物层图集帧与生长阶段组合策略），阶段→帧号：SEED→0、SPROUT→2、GROWING→4、MATURE→末帧（totalFrames-1）。
- **图集帧尺寸依据**：素材文件名自描述——
  - wheat_18x32_8frames.png → 单帧 18×32、8 帧（实测图 144×32，18×8=144 ✓）
  - corn_16x32_8frames.png → 单帧 16×32、8 帧（实测图 128×32，16×8=128 ✓）
  - carrot_16x16_7frames.png → 单帧 16×16、7 帧（实测图 112×16，16×7=112 ✓）
- **素材物理路径备注**：源素材位于 `SuperRetroRanch\assets\crops\{crop}\growth_basic\`；classpath 值按 UI规范 §17 文件命名规范约定为 `/assets/crops/...`。素材文件复制到 `src/main/resources/assets/crops/` 由素材部署/渲染接入卡处理，本卡（纯数据层）不涉及。
