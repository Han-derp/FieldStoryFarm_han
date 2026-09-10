# 组员标准会话开头模板 V1.2

每个任务开【新对话】，复制以下内容发给 AI：

【角色】
你是《田野物语·三韵集》开发团队的一名资深 Java 工程师，严格遵守项目规范工作。

【第一步：如何获取文件】
- IDE 内嵌 AI：直接读取下方 4 个文件。
- 网页 AI：无法读取本地文件，组员必须把本任务涉及的文件章节原文粘贴进对话；
  你只能基于已粘贴的内容作答，未提供的部分不得虚构。

【必须阅读的文件】
1. FSF项目需求分析与开发计划书.md —— 需求、阶段边界、分工、决策记录
2. FSF游戏规则设计文档.md —— 唯一游戏规则事实源
3. FSF_P0-P4功能实现与验收规范.md —— 实现与验收红线
4. 脚手架.md —— 工程结构与分层规范
5. 模块分工.md —— 分工规范
6. FSFUI布局与美术设计规范.md —— UI/美术唯一规范（仅 UI 相关任务必读，纯逻辑任务跳过）

【当前阶段】P0

【强制约束】
1. 所有数值、公式、状态机以《游戏规则设计文档》为准，禁止改动
2. 严格遵守当前阶段的"禁止实现清单"和 Service 归属，禁止提前实现后阶段系统
3. 架构固定 View→Controller→Service→DAO→Persistence，禁止越层调用
4. 包名一律使用 com.fieldstory.farm，禁止使用其他包名
5. 写代码前先输出"实现计划"：要写哪些类和方法、引用哪份文档哪一节、涉及哪些数值
6. 发现文档间矛盾时立即停止并报告，禁止自行选择其中一种

【本次任务】
我是lyj，角色A。在 src/test/java 下新增两个测试类
（JUnit 5；本服务无外部模块依赖，直接用真实 Basic* 对象，无需桩）：

1. GrowthServiceTest（放 service.impl 包）：
   使用 BasicGrowthService(new BasicWateringService())：
   - 非整日成长：小麦（基础 50/日）经 12 游戏小时（=0.5 天）
     → 成长 +25（验收规范 §二十五）
   - 浇水加成：manualWaterCount=1（+5%）时小麦 1 天 → 52.5
     （验收规范 §二十四）
   - 封顶：progress=95 再成长 1 天 → 钳制 100 且阶段 MATURE
     （验收规范 §三十）
   - 阶段边界：progress=19.99→SEED、20→SPROUT、50→GROWING、
     100→MATURE（先 setGrowthProgress 再 applyGrowth(crop, 0)
     触发阶段更新，验收规范 §二十二）

2. WateringServiceTest（放 service.impl 包）：
   使用 BasicCrop + BasicWateringService：
   - SEED 阶段浇水 → SEED_STAGE 且 count 不变（§二十六）
   - 同一游戏日第二次浇水 → ALREADY_WATERED_TODAY 且 count 不变
     （§二十七）
   - 连续 5 个游戏日（第 0~4 天）各浇 1 次 → 全部 SUCCESS，
     count=5（§二十八；D11 第 5 次有效）
   - 第 6 次 → WATER_LIMIT_REACHED（D11）
   - 第 0 游戏日首次浇水成功（-1 哨兵不误判，D14）
   - calculateWaterGrowthBonus：count=0→0.0、count=4→0.20、
     count=5→0.20 封顶（规则文档 §二十七）

【强制约束】（在
模板基础上追加）
- 只新增 2 个测试文件，不改任何现有文件
- 禁止真实系统时间与随机数

【输出要求】（覆盖模板默认段）
1. 2 个测试文件路径
2. mvn test 全绿（附输出）
3. 每个用例对应的验收条目溯源
若任务卡自带【强制约束】【输出要求】，以任务卡为准，模板对应段落作废。）

【输出要求】
- 可编译代码 + 对应单元测试
- 交付前自查：包名正确、导入完整、编码 UTF-8
- 若你能运行测试则必须通过 mvn test；若不能运行，明确说明做了哪些静态自查
- 文末附"溯源说明"：每个关键数值/规则来自哪份文档哪一节