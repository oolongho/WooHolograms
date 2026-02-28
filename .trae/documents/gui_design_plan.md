# WooHolograms GUI 管理系统设计计划

## 一、概述

设计一套完整的箱子GUI系统，用于管理全息图，功能与命令系统完全对应。

---

## 二、GUI界面设计

### 2.1 主菜单
- **标题**: `&8全息图列表`
- **大小**: 54格 (6行)
- **布局**:
  - 槽位 0-44: 全息图列表 (分页显示)
  - 槽位 45: 上一页
  - 槽位 49: 创建全息图
  - 槽位 53: 下一页
- **全息图图标**: 命名牌
  - 显示名称: 全息图名称
  - 描述: 位置、状态、行数

### 2.2 全息图详情页
- **标题**: `&8全息图: {name}`
- **大小**: 54格 (6行)
- **布局**:
  - 槽位 0: 返回按钮
  - 槽位 4: 全息图名称
  - 槽位 9-35: 行列表 (每行一个图标)
  - 槽位 36-44: 功能按钮区
    - 槽位 36: 添加行
    - 槽位 37: 启用/禁用
    - 槽位 38: 传送
    - 槽位 39: 移动到此处
    - 槽位 40: 克隆
    - 槽位 41: 删除
    - 槽位 42: 设置权限
    - 槽位 43: 设置范围
    - 槽位 44: 设置间隔
  - 槽位 45-53: 页面导航
    - 槽位 45: 上一页
    - 槽位 49: 页面信息
    - 槽位 53: 下一页

### 2.3 行编辑页
- **标题**: `&8编辑行: {hologram} #{line}`
- **大小**: 27格 (3行)
- **布局**:
  - 槽位 0: 返回按钮
  - 槽位 4: 当前行内容
  - 槽位 10: 设置文本 (聊天框输入)
  - 槽位 11: 设置偏移 (聊天框输入)
  - 槽位 12: 设置高度 (聊天框输入)
  - 槽位 16: 删除行
  - 槽位 18: 上移
  - 槽位 22: 下移

### 2.4 页面管理页
- **标题**: `&8页面管理: {hologram}`
- **大小**: 54格 (6行)
- **布局**:
  - 槽位 0: 返回按钮
  - 槽位 9-35: 页面列表
  - 槽位 45: 添加页面
  - 槽位 53: 删除页面

### 2.5 动作管理页
- **标题**: `&8动作管理: {hologram} P{page}`
- **大小**: 54格 (6行)
- **布局**:
  - 槽位 0: 返回按钮
  - 槽位 9-35: 动作列表
  - 槽位 36: 左键动作
  - 槽位 37: 右键动作
  - 槽位 38: Shift+左键动作
  - 槽位 39: Shift+右键动作
  - 槽位 45: 添加动作
  - 槽位 53: 删除动作

### 2.6 属性设置页
- **标题**: `&8属性设置: {hologram}`
- **大小**: 27格 (3行)
- **布局**:
  - 槽位 0: 返回按钮
  - 槽位 10: 显示范围 (聊天框输入)
  - 槽位 11: 更新间隔 (聊天框输入)
  - 槽位 12: 查看权限 (聊天框输入)
  - 槽位 16: 重置为默认

### 2.7 确认删除页
- **标题**: `&8确认删除`
- **大小**: 27格 (3行)
- **布局**:
  - 槽位 11: 确认删除 (红色)
  - 槽位 13: 警告信息
  - 槽位 15: 取消 (绿色)

---

## 三、聊天框输入系统

### 3.1 设计思路
由于铁砧输入存在长度限制，改用聊天框输入方式：
1. 玩家点击GUI中的输入按钮
2. 关闭GUI，提示玩家在聊天框输入
3. 玩家发送消息后，拦截该消息（不发送给其他玩家）
4. 处理输入内容，重新打开GUI

### 3.2 ChatInputManager 功能
```java
public class ChatInputManager {
    // 存储等待输入的玩家
    private Map<UUID, InputContext> pendingInputs;
    
    // 输入上下文
    public static class InputContext {
        private InputType type;        // 输入类型
        private String hologramName;   // 全息图名称
        private int lineNumber;        // 行号
        private int pageIndex;         // 页码
        private Runnable callback;     // 输入完成回调
    }
    
    // 输入类型枚举
    public enum InputType {
        HOLOGRAM_NAME,     // 全息图名称
        LINE_TEXT,         // 行文本
        LINE_OFFSET,       // 行偏移
        LINE_HEIGHT,       // 行高度
        DISPLAY_RANGE,     // 显示范围
        UPDATE_INTERVAL,   // 更新间隔
        PERMISSION,        // 权限
        ACTION_VALUE       // 动作值
    }
}
```

### 3.3 输入流程示例
```
玩家点击"设置文本"按钮
    ↓
关闭GUI，发送提示: "&a请在聊天框输入新的文本内容:"
    ↓
玩家输入: "&e欢迎来到服务器!"
    ↓
拦截消息（取消AsyncPlayerChatEvent）
    ↓
处理输入，更新全息图
    ↓
重新打开GUI，显示更新后的内容
```

### 3.4 消息拦截机制
- 监听 `AsyncPlayerChatEvent`
- 检查玩家是否在等待输入
- 如果是，取消事件（不发送给其他玩家）
- 处理输入内容

---

## 四、文件结构

```
src/main/java/com/oolonghoo/holograms/gui/
├── GuiScreen.java          # 基础GUI类 (已存在)
├── GuiButton.java          # 按钮类 (已存在)
├── GuiManager.java         # GUI管理器 (新建)
├── ChatInputManager.java   # 聊天框输入管理器 (新建)
├── HologramListGui.java    # 主菜单 (新建)
├── HologramDetailGui.java  # 详情页 (新建)
├── LineEditGui.java        # 行编辑页 (新建)
├── PageManageGui.java      # 页面管理页 (新建)
├── ActionManageGui.java    # 动作管理页 (新建)
├── PropertyGui.java        # 属性设置页 (新建)
└── ConfirmGui.java         # 确认页 (新建)
```

---

## 五、实现步骤

### 阶段一：基础设施
- [ ] Task 1: 创建 GuiManager.java - 管理所有GUI实例和事件监听
- [ ] Task 2: 创建 ChatInputManager.java - 聊天框输入管理器
- [ ] Task 3: 创建 ConfirmGui.java - 确认对话框

### 阶段二：核心界面
- [ ] Task 4: 创建 HologramListGui.java - 主菜单
- [ ] Task 5: 创建 HologramDetailGui.java - 详情页

### 阶段三：编辑功能
- [ ] Task 6: 创建 LineEditGui.java - 行编辑
- [ ] Task 7: 创建 PageManageGui.java - 页面管理
- [ ] Task 8: 创建 PropertyGui.java - 属性设置

### 阶段四：高级功能
- [ ] Task 9: 创建 ActionManageGui.java - 动作管理
- [ ] Task 10: 添加命令入口 `/wh gui` 和事件监听

---

## 六、物品材质设计

| 功能 | 材质 | 描述 |
|------|------|------|
| 全息图图标 | NAME_TAG | 命名牌 |
| 创建全息图 | EMERALD | 绿宝石 |
| 删除 | REDSTONE_BLOCK | 红石块 |
| 启用 | LIME_DYE | 绿色染料 |
| 禁用 | GRAY_DYE | 灰色染料 |
| 传送 | ENDER_PEARL | 末影珍珠 |
| 移动 | COMPASS | 指南针 |
| 克隆 | SLIME_BALL | 粘液球 |
| 添加行 | PAPER | 纸 |
| 编辑行 | WRITABLE_BOOK | 成书 |
| 删除行 | BARRIER | 屏障 |
| 页面 | BOOK | 书 |
| 上一页 | ARROW | 箭 |
| 下一页 | ARROW | 箭 |
| 返回 | BOOK | 书 |
| 确认 | GREEN_WOOL | 绿色羊毛 |
| 取消 | RED_WOOL | 红色羊毛 |
| 范围 | EYE_OF_ENDER | 末影之眼 |
| 间隔 | CLOCK | 时钟 |
| 权限 | TRIPWIRE_HOOK | 绊线钩 |
| 动作 | COMMAND_BLOCK | 命令方块 |
| 偏移 | STICK | 木棍 |
| 高度 | RAIL | 铁轨 |
| 输入框 | SIGN | 告示牌 |

---

## 七、命令入口

添加新命令：
- `/wh gui` - 打开主菜单
- `/wh gui <名称>` - 直接打开指定全息图的详情页

---

## 八、与命令功能对照

| 命令 | GUI对应功能 |
|------|------------|
| `/wh create` | 主菜单"创建全息图"按钮 → 聊天框输入名称 |
| `/wh delete` | 详情页"删除"按钮 → 确认页 |
| `/wh copy` | 详情页"克隆"按钮 → 聊天框输入目标名称 |
| `/wh near` | 主菜单"附近全息图"按钮 |
| `/wh enable/disable` | 详情页"启用/禁用"按钮 |
| `/wh list` | 主菜单全息图列表 |
| `/wh info` | 详情页显示 |
| `/wh teleport` | 详情页"传送"按钮 |
| `/wh movehere` | 详情页"移动到此处"按钮 |
| `/wh moveto` | 属性设置页"移动到坐标" → 聊天框输入坐标 |
| `/wh addline` | 详情页"添加行"按钮 → 聊天框输入文本 |
| `/wh deleteline` | 行编辑页"删除行"按钮 |
| `/wh setline` | 行编辑页"设置文本"按钮 → 聊天框输入文本 |
| `/wh insertline` | 行编辑页"插入行"按钮 → 聊天框输入文本 |
| `/wh addpage` | 页面管理页"添加页面"按钮 |
| `/wh deletepage` | 页面管理页"删除页面"按钮 |
| `/wh setrange` | 属性设置页"显示范围"按钮 → 聊天框输入数字 |
| `/wh setinterval` | 属性设置页"更新间隔"按钮 → 聊天框输入数字 |
| `/wh setpermission` | 属性设置页"查看权限"按钮 → 聊天框输入权限 |
| `/wh addaction` | 动作管理页"添加动作"按钮 → 聊天框输入动作值 |
| `/wh deleteaction` | 动作管理页"删除动作"按钮 |
| `/wh actions` | 动作管理页动作列表 |
| `/wh offset` | 行编辑页"设置偏移"按钮 → 聊天框输入坐标 |
| `/wh height` | 行编辑页"设置高度"按钮 → 聊天框输入数字 |
| `/wh reload` | 主菜单"重载配置"按钮 |

---

## 九、聊天框输入提示语设计

| 输入类型 | 提示语 | 示例输入 |
|---------|--------|---------|
| 全息图名称 | `&a请输入全息图名称:` | `test` |
| 行文本 | `&a请输入行文本 (支持颜色代码):` | `&e欢迎!` |
| 行偏移 | `&a请输入偏移值 (x y z):` | `0.5 0 -0.5` |
| 行高度 | `&a请输入高度值:` | `0.25` |
| 显示范围 | `&a请输入显示范围 (格):` | `48` |
| 更新间隔 | `&a请输入更新间隔 (tick):` | `20` |
| 权限 | `&a请输入权限节点 (输入 clear 清除):` | `vip.use` |
| 动作值 | `&a请输入动作值:` | `message:&a你好!` |
| 取消输入 | `&c输入已取消` | - |
| 输入成功 | `&a输入成功!` | - |

---

## 十、注意事项

1. **权限检查**: 每个操作都需要检查 `wooholograms.admin` 权限
2. **数据同步**: GUI操作后需要调用 `hologram.save()` 保存数据
3. **刷新机制**: 操作后需要刷新GUI显示
4. **聊天框输入**: 拦截消息事件，不发送给其他玩家
5. **分页显示**: 大量数据时需要分页
6. **国际化**: 使用消息管理器显示文本
7. **事件监听**: 需要注册 `InventoryClickEvent` 和 `AsyncPlayerChatEvent` 监听器
8. **GUI状态管理**: 使用 `GuiManager` 管理当前打开的GUI
9. **输入超时**: 可选添加输入超时机制（如30秒后自动取消）
10. **取消输入**: 玩家可以输入 `cancel` 或 `取消` 来取消输入
