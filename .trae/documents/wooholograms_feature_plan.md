# WooHolograms 功能完善计划

## 一、现状分析

### 已实现功能
- 基础命令：create, delete, list, info, teleport, movehere, moveto, reload, help
- 行管理：addline, deleteline, setline, insertline
- 全息图类型：TEXT, ICON, HEAD, SMALLHEAD, ENTITY（代码存在但无命令入口）
- 动画系统：代码存在但无命令入口
- 点击动作：代码存在但无命令入口

### 欠缺功能（参考 DH）
1. 克隆全息图 `/wh copy`
2. 附近全息图 `/wh near`
3. 启用/禁用 `/wh enable|disable`
4. 添加页面 `/wh addpage`
5. 删除页面 `/wh deletepage`
6. 设置显示范围 `/wh setrange`
7. 设置更新间隔 `/wh setinterval`
8. 设置权限 `/wh setpermission`
9. 点击动作管理 `/wh action`
10. 行偏移设置 `/wh offset`

---

## 二、实现计划

### 阶段一：基础管理命令（高优先级）

#### 1.1 克隆全息图 `/wh copy <源名称> <目标名称>`
- 文件：`HologramCommand.java` 内部类 `CopyCommand`
- 功能：复制全息图及其所有行、页面、动作
- 实现：
  1. 检查源全息图是否存在
  2. 检查目标名称是否已存在
  3. 创建新全息图
  4. 复制所有页面和行
  5. 复制点击动作
  6. 保存并显示

#### 1.2 附近全息图 `/wh near [范围]`
- 文件：`HologramCommand.java` 内部类 `NearCommand`
- 功能：列出玩家附近的全息图
- 实现：
  1. 获取玩家位置
  2. 遍历所有全息图计算距离
  3. 按距离排序输出

#### 1.3 启用/禁用 `/wh enable|disable <名称>`
- 文件：`HologramCommand.java` 内部类 `EnableCommand`、`DisableCommand`
- 功能：启用或禁用全息图显示
- 实现：
  1. 设置 hologram.enabled 属性
  2. 禁用时隐藏所有观看者
  3. 启用时显示给附近玩家

### 阶段二：页面管理命令（高优先级）

#### 2.1 添加页面 `/wh addpage <名称> [内容]`
- 文件：`HologramCommand.java` 内部类 `AddPageCommand`
- 功能：添加新页面
- 实现：
  1. 调用 `hologram.addPage()`
  2. 可选添加初始内容

#### 2.2 删除页面 `/wh deletepage <名称> <页码>`
- 文件：`HologramCommand.java` 内部类 `DeletePageCommand`
- 功能：删除指定页面
- 实现：
  1. 检查页码有效性
  2. 调用 `hologram.removePage(index)`

### 阶段三：属性设置命令（中优先级）

#### 3.1 设置显示范围 `/wh setrange <名称> <范围>`
- 功能：设置全息图的显示距离
- 实现：
  1. 调用 `hologram.setDisplayRange(range)`
  2. 更新观看者列表

#### 3.2 设置更新间隔 `/wh setinterval <名称> <tick>`
- 功能：设置全息图的更新频率
- 实现：
  1. 调用 `hologram.setUpdateInterval(ticks)`

#### 3.3 设置权限 `/wh setpermission <名称> [权限]`
- 功能：设置查看全息图所需的权限
- 实现：
  1. 调用 `hologram.setPermission(permission)`
  2. 空权限表示取消权限要求

### 阶段四：点击动作管理（中优先级）

#### 4.1 添加动作 `/wh addaction <名称> <页码> <点击类型> <动作类型> [值]`
- 点击类型：left, right, shift_left, shift_right
- 动作类型：message, command, console, sound, teleport, connect, next_page, prev_page, page
- 实现：
  1. 解析参数
  2. 创建 ActionData
  3. 添加到页面

#### 4.2 删除动作 `/wh deleteaction <名称> <页码> <点击类型> <索引>`
- 功能：删除指定的点击动作

#### 4.3 列出动作 `/wh actions <名称> <页码> <点击类型>`
- 功能：列出页面的所有点击动作

### 阶段五：行属性设置（低优先级）

#### 5.1 设置行偏移 `/wh offset <名称> <行号> <x> <y> <z>`
- 功能：设置行的位置偏移

#### 5.2 设置行高度 `/wh height <名称> <行号> <高度>`
- 功能：设置行的高度

---

## 三、文件修改清单

### 主要修改文件
1. `HologramCommand.java` - 添加新子命令
2. `Hologram.java` - 确保属性设置方法存在
3. `HologramPage.java` - 确保动作管理方法存在
4. `messages/zh-CN.yml` - 添加新消息

### 可能需要新增的文件
1. 无需新增文件，所有命令在 `HologramCommand.java` 中以内部类实现

---

## 四、实现顺序

1. **第一批**：copy, near, enable, disable
2. **第二批**：addpage, deletepage
3. **第三批**：setrange, setinterval, setpermission
4. **第四批**：addaction, deleteaction, actions
5. **第五批**：offset, height（可选）

---

## 五、测试计划

每个功能实现后进行以下测试：
1. 命令语法正确性
2. Tab 补全功能
3. 权限检查
4. 错误处理
5. 功能正确性
