# Tasks

## 阶段一：基础管理命令

- [x] Task 1: 实现克隆全息图命令 `/wh copy <源名称> <目标名称>`
  - [x] 1.1 在 HologramCommand.java 中添加 CopyCommand 内部类
  - [x] 1.2 实现复制逻辑：创建新全息图、复制页面和行、复制动作
  - [x] 1.3 添加 Tab 补全：源名称补全
  - [x] 1.4 添加消息到 zh-CN.yml
  - [x] 1.5 测试克隆功能

- [x] Task 2: 实现附近全息图命令 `/wh near [范围]`
  - [x] 2.1 在 HologramCommand.java 中添加 NearCommand 内部类
  - [x] 2.2 实现距离计算和排序逻辑
  - [x] 2.3 默认范围 50 格
  - [x] 2.4 添加消息到 zh-CN.yml
  - [x] 2.5 测试附近搜索功能

- [x] Task 3: 实现启用/禁用命令 `/wh enable|disable <名称>`
  - [x] 3.1 在 HologramCommand.java 中添加 EnableCommand 内部类
  - [x] 3.2 在 HologramCommand.java 中添加 DisableCommand 内部类
  - [x] 3.3 实现启用逻辑：设置 enabled=true，显示给附近玩家
  - [x] 3.4 实现禁用逻辑：设置 enabled=false，隐藏所有观看者
  - [x] 3.5 添加消息到 zh-CN.yml
  - [x] 3.6 测试启用/禁用功能

## 阶段二：页面管理命令

- [x] Task 4: 实现添加页面命令 `/wh addpage <名称> [内容]`
  - [x] 4.1 在 HologramCommand.java 中添加 AddPageCommand 内部类
  - [x] 4.2 调用 Hologram.addPage() 方法
  - [x] 4.3 可选添加初始内容
  - [x] 4.4 添加消息到 zh-CN.yml
  - [x] 4.5 测试添加页面功能

- [x] Task 5: 实现删除页面命令 `/wh deletepage <名称> <页码>`
  - [x] 5.1 在 HologramCommand.java 中添加 DeletePageCommand 内部类
  - [x] 5.2 检查页码有效性
  - [x] 5.3 调用 Hologram.removePage() 方法
  - [x] 5.4 添加 Tab 补全：页码补全
  - [x] 5.5 添加消息到 zh-CN.yml
  - [x] 5.6 测试删除页面功能

## 阶段三：属性设置命令

- [x] Task 6: 实现设置显示范围命令 `/wh setrange <名称> <范围>`
  - [x] 6.1 在 HologramCommand.java 中添加 SetRangeCommand 内部类
  - [x] 6.2 调用 Hologram.setDisplayRange() 方法
  - [x] 6.3 刷新观看者列表
  - [x] 6.4 添加消息到 zh-CN.yml
  - [x] 6.5 测试设置范围功能

- [x] Task 7: 实现设置更新间隔命令 `/wh setinterval <名称> <tick>`
  - [x] 7.1 在 HologramCommand.java 中添加 SetIntervalCommand 内部类
  - [x] 7.2 调用 Hologram.setUpdateInterval() 方法
  - [x] 7.3 添加消息到 zh-CN.yml
  - [x] 7.4 测试设置间隔功能

- [x] Task 8: 实现设置权限命令 `/wh setpermission <名称> [权限]`
  - [x] 8.1 在 HologramCommand.java 中添加 SetPermissionCommand 内部类
  - [x] 8.2 调用 Hologram.setPermission() 方法
  - [x] 8.3 空权限表示取消权限要求
  - [x] 8.4 添加消息到 zh-CN.yml
  - [x] 8.5 测试设置权限功能

## 阶段四：点击动作管理

- [x] Task 9: 实现添加点击动作命令 `/wh addaction <名称> <页码> <点击类型> <动作类型> [值]`
  - [x] 9.1 在 HologramCommand.java 中添加 AddActionCommand 内部类
  - [x] 9.2 解析点击类型：left, right, shift_left, shift_right
  - [x] 9.3 解析动作类型：message, command, console, sound, teleport, connect, next_page, prev_page, page
  - [x] 9.4 创建 ActionData 并添加到页面
  - [x] 9.5 添加 Tab 补全
  - [x] 9.6 添加消息到 zh-CN.yml
  - [x] 9.7 测试添加动作功能

- [x] Task 10: 实现删除点击动作命令 `/wh deleteaction <名称> <页码> <点击类型> <索引>`
  - [x] 10.1 在 HologramCommand.java 中添加 DeleteActionCommand 内部类
  - [x] 10.2 检查索引有效性
  - [x] 10.3 从页面删除动作
  - [x] 10.4 添加 Tab 补全
  - [x] 10.5 添加消息到 zh-CN.yml
  - [x] 10.6 测试删除动作功能

- [x] Task 11: 实现列出点击动作命令 `/wh actions <名称> <页码> <点击类型>`
  - [x] 11.1 在 HologramCommand.java 中添加 ActionsCommand 内部类
  - [x] 11.2 列出页面的所有点击动作
  - [x] 11.3 添加 Tab 补全
  - [x] 11.4 添加消息到 zh-CN.yml
  - [x] 11.5 测试列出动作功能

## 阶段五：行属性设置

- [x] Task 12: 实现设置行偏移命令 `/wh offset <名称> <行号> <x> <y> <z>`
  - [x] 12.1 在 HologramCommand.java 中添加 OffsetCommand 内部类
  - [x] 12.2 调用 HologramLine.setOffset() 方法
  - [x] 12.3 添加 Tab 补全
  - [x] 12.4 添加消息到 zh-CN.yml
  - [x] 12.5 测试设置偏移功能

- [x] Task 13: 实现设置行高度命令 `/wh height <名称> <行号> <高度>`
  - [x] 13.1 在 HologramCommand.java 中添加 HeightCommand 内部类
  - [x] 13.2 调用 HologramLine.setHeight() 方法
  - [x] 13.3 添加 Tab 补全
  - [x] 13.4 添加消息到 zh-CN.yml
  - [x] 13.5 测试设置高度功能

## 阶段六：更新帮助信息

- [x] Task 14: 更新帮助信息和命令列表
  - [x] 14.1 更新 sendHelp() 方法
  - [x] 14.2 更新 plugin.yml 中的命令描述
  - [x] 14.3 确保所有新命令都有正确的权限检查

---

# Task Dependencies
- Task 4, 5 依赖 Hologram 类的页面管理方法
- Task 9, 10, 11 依赖 HologramPage 类的动作管理方法
- Task 12, 13 依赖 HologramLine 类的属性设置方法
- Task 1, 2, 3, 6, 7, 8 可以并行执行
