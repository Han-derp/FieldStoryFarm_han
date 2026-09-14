# A模块-P1地面Tile美化决策记录

> 模块：A（视图层，com.fieldstory.farm.view）
> 阶段：P1 地面 Tile 美化（纯函数层：帧元数据 + 变体判定）
> 关联实现：`GroundSpriteSheet`（地面图集元数据枚举）、`GroundVariant`（贴图变体枚举）、
>   `FarmView#groundVariantFor`（变体判定纯函数）、`FarmView#updateGroundTexture`（地面贴图层刷新）、
>   `FarmView#setWetToday`（湿天标记 setter）
> 关联规范：FSFUI布局与美术设计规范 §6 农场地图、§7 土地Tile规范、§14 颜色规范

---

## D-G1：贴图 2× 缩放（32×32）居中显示，保留格子底色

- **结论**：地面贴图（单帧 16×16）统一 2× 整数倍缩放为 32×32，在 44×44 格子内居中显示；
  格子底色（`FarmView#tileColorFor` 的纯色）保留，贴图作为叠加层覆盖其上。
- **背景**：素材单帧 16×16，2× 后 32×32 小于格子 44×44，居中后四周留 6px 底色边；
  与作物贴图决策 D1 的整数倍缩放口径一致（像素风最近邻采样，不做平滑）。
- **本卡范围**：本卡为纯函数/纯数据层，不实现渲染；该策略仅在此记录，渲染接入卡按此执行。

## D-G2：湿判定 = 今日已浇 或 今日天气 ∈ {RAIN, GREEN_RAIN}

- **结论**：某格显示 WET（湿地深色）贴图当且仅当满足其一：
  1. 该格今日已主动浇水：`crop.lastManualWaterGameDay == currentGameDay`（决策 D14 时间口径 long，用 ==）；
  2. 今日天气 ∈ {RAIN, GREEN_RAIN}（雨天自动补水，规则文档 §二十一）。
  SUNNY / DROUGHT / 未知天气一律按干处理。
- **对应实现**：`groundVariantFor` 内湿判定 =
  `(crop != null && crop.lastManualWaterGameDay == currentGameDay) || wetToday`；
  TILLED 无作物，湿判定仅依赖 `wetToday`。
- **GREEN_RAIN 归属说明**：绿雨（规则文档 §二十三）是雨天变体（成长倍率 ×2.0 的降水天气），
  当天土地同样被雨水滋润，故与 RAIN 同属湿天；SUNNY（晴）与 DROUGHT（旱）无降水按干。
  天气 → `wetToday` 布尔值的换算由调用方（渲染接入卡/Controller）完成，本纯函数只消费布尔，
  不直接依赖 WeatherType（纯函数层保持无天气模块依赖）。

## D-G3：MATURE / WITHERED / EMPTY / LOCKED 不铺贴图

- **结论**：MATURE（整格高亮 #E8C45C）、WITHERED（整格枯萎 #857766）、EMPTY（木色）、
  LOCKED（木色占位）四类格不铺地面贴图，保持纯色语义，与 P0 表现一致。
- **对应实现**：`groundVariantFor` 对上述状态返回 `GroundVariant.NONE`（「不铺贴图」哨兵）。

## D-G5：移除 tile 1px 描边，选中高亮保留

- **结论**：普通格的 1px 分隔描边（#493526）移除，贴图铺满更干净；
  点击选中的 3px 高亮描边（#E8C45C，UI规范 §11）保留不动。
- **本卡范围**：描边移除属 FarmView 渲染改造，本卡（纯函数层）不涉及，仅在此记录。

> 注：编号 D-G4 未随本卡下发（推测属渲染接入卡决策），本卡不杜撰其内容，编号留白。

---

## 附：帧坐标表与湿判定公式（溯源说明）

### 帧坐标（人工对照原图确认，禁止自行更改）

来源：`.qoder-temp/ground_survey.txt`（帧勘察原始数据：256×256、16×16 帧网格）。

| 变体 | 帧坐标 | avgRGB（勘察数据） | 说明 |
|---|---|---|---|
| GRASS（草地） | col=3, row=6 | (113,171,49) | 绿色系，与主色表草地 #7FAE55 相近 |
| TILLED（耕地干） | col=3, row=1 | (181,109,86) | 褐色系，与主色表土地 #A97850 相近 |
| WET（湿地深色） | col=8, row=10 | (134,73,75) | 深红褐，明显深于 TILLED |

- viewport 换算：`frameX(col) = col × 16`、`frameY(row) = row × 16`（`GroundSpriteSheet` 静态纯函数）。
- 素材物理路径：`SuperRetroRanch\assets\tiles\ground_01_16x16.png`；
  classpath 已按 UI规范 §17 文件命名规范部署为 `/assets/tiles/ground_01_16x16.png`
  （`src/main/resources/assets/tiles/` 已存在）。

### 湿判定公式

```text
格内湿状态 = (crop != null && crop.lastManualWaterGameDay == currentGameDay) || wetToday
wetToday   = (今日天气 ∈ {RAIN, GREEN_RAIN})
```

- PLANTED 且 crop 为 null（坏数据）：退化按仅天气判定（wetToday ? WET : TILLED）。
- TILLED：无作物，仅按 wetToday。
- 变体最终裁决：MATURE/WITHERED/EMPTY/LOCKED → NONE（D-G3，优先于湿判定）。

---

## 渲染接入（FarmView 渲染接入卡）

> 实现：`FarmView` 新增地面贴图层（`groundTextures` 占位 ImageView 二维表 +
> `updateGroundTexture` 每格刷新）、`setWetToday` 湿天 setter、tile 描边移除。
> 关联规范：FSFUI布局与美术设计规范 §6 农场地图、§7 土地Tile规范。

### Z 序（固定四层）

每格渲染 Z 序固定为：tile 底色 → 地面贴图（groundTextures）→ 作物占位块（cropBlocks）
→ 作物贴图（cropSprites）。`buildTiles` 按此顺序加入 children；地面贴图复用构造期
占位 ImageView（原地更新 image/viewport/fitWidth/fitHeight），不替换节点，Z 序稳定。

### 居中公式落地（D-G1）

`updateGroundTexture` 内：
`setX(column*TILE_SIZE+(TILE_SIZE-fitWidth)/2.0)`、
`setY(row*TILE_SIZE+(TILE_SIZE-fitHeight)/2.0)`。
贴图恒 32×32，故落地为 col*44+6 / row*44+6，四周留 6px 底色边；
格子底色（`tileColorFor`）保留不动。

### 去描边落地（D-G5）

`buildTiles` 删除 `tile.setStroke(COLOR_TEXT)` 与 `tile.setStrokeWidth(1)` 两行；
点击选中的 3px 高亮描边（selectionRect，#E8C45C，UI规范 §11）保留不动。

### setWetToday 为 D-G4 接线预留

`FarmView#setWetToday(boolean)` 纯 setter（同 `setCurrentGameDay` 模式），
供 MainController 跨天回调同步（天气 ∈ {RAIN, GREEN_RAIN} 换算由调用方完成，D-G2）；
D-G4 跨模块接线不在本卡范围，本卡只提供存储与使用（`groundVariantFor` 的
wetToday 入参来源）。
