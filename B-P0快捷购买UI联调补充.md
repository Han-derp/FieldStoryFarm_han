# 31. P0快捷购买UI联调补充

## 31.1 定位

P0快捷购买属于B模块UI接入层，
不新增经济Service。

调用链：

SeedQuickBuyView
↓
SeedQuickBuyController
↓
EconomyService.buySeed(type, 1)

## 31.2 新增类型

### SeedQuickBuyController

包：

com.fieldstory.farm.controller

职责：

- 接收快捷购买操作
- 调用 EconomyService
- 查询金币
- 查询种子库存
- 将 PurchaseResult 转换为UI状态

不得：

- 直接修改Player
- 自己计算价格
- 自己扣金币
- 自己写JSON

### SeedQuickBuyView

包：

com.fieldstory.farm.view

职责：

- 显示当前金币
- 显示三种种子库存
- 显示三种购买按钮
- 显示购买结果

价格读取：

CropType.getSeedPrice()

不得保存第二份价格表。

## 31.3 场景组装

B只提供View和Controller。

最终挂载由E负责：

SceneManager.Slot.RIGHT

B不得：

new Scene(...)
new Stage(...)
setRoot(...)

## 31.4 持久化约定

B不实现JsonSaveService。

E必须保存并恢复：

Player.gold
Player.seedInventory

B负责联调验收恢复后的Player能继续被EconomyService使用。

## 31.5 P0状态

核心经济接口：
FROZEN

快捷购买UI：
IMPLEMENTED / WAITING_INTEGRATION

seedInventory持久化：
WAITING_FOR_E

全项目P0验收：
NOT_FINISHED