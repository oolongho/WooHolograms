# WooHolograms 功能完善 Spec

## Why
WooHolograms 目前只实现了基础命令，缺少 DecentHolograms 的高级功能，如克隆、附近搜索、页面管理、点击动作管理等。需要参照 DH 的功能逐步完善。

## What Changes
- 新增克隆全息图命令 `/wh copy`
- 新增附近全息图命令 `/wh near`
- 新增启用/禁用命令 `/wh enable|disable`
- 新增页面管理命令 `/wh addpage|deletepage`
- 新增属性设置命令 `/wh setrange|setinterval|setpermission`
- 新增点击动作管理命令 `/wh addaction|deleteaction|actions`
- 新增行属性命令 `/wh offset|height`

## Impact
- Affected code: `HologramCommand.java`, `Hologram.java`, `HologramPage.java`, `messages/zh-CN.yml`

---

## ADDED Requirements

### Requirement: Clone Hologram
系统应提供克隆全息图功能。

#### Scenario: 成功克隆
- **WHEN** 用户执行 `/wh copy <源名称> <目标名称>`
- **THEN** 创建一个新的全息图，复制源全息图的所有页面、行、动作

#### Scenario: 源不存在
- **WHEN** 源全息图不存在
- **THEN** 提示错误信息

#### Scenario: 目标已存在
- **WHEN** 目标名称已存在
- **THEN** 提示错误信息

---

### Requirement: Near Holograms
系统应提供查找附近全息图功能。

#### Scenario: 成功查找
- **WHEN** 用户执行 `/wh near [范围]`
- **THEN** 列出玩家附近指定范围内的所有全息图，按距离排序

#### Scenario: 默认范围
- **WHEN** 用户执行 `/wh near` 不指定范围
- **THEN** 使用默认范围 50 格

---

### Requirement: Enable/Disable Hologram
系统应提供启用/禁用全息图功能。

#### Scenario: 禁用全息图
- **WHEN** 用户执行 `/wh disable <名称>`
- **THEN** 全息图停止显示，隐藏所有观看者

#### Scenario: 启用全息图
- **WHEN** 用户执行 `/wh enable <名称>`
- **THEN** 全息图恢复显示，显示给附近玩家

---

### Requirement: Page Management
系统应提供页面管理功能。

#### Scenario: 添加页面
- **WHEN** 用户执行 `/wh addpage <名称> [内容]`
- **THEN** 在全息图末尾添加新页面，可选添加初始内容

#### Scenario: 删除页面
- **WHEN** 用户执行 `/wh deletepage <名称> <页码>`
- **THEN** 删除指定页面，剩余页面重新编号

---

### Requirement: Property Settings
系统应提供属性设置功能。

#### Scenario: 设置显示范围
- **WHEN** 用户执行 `/wh setrange <名称> <范围>`
- **THEN** 更新全息图的显示范围，刷新观看者

#### Scenario: 设置更新间隔
- **WHEN** 用户执行 `/wh setinterval <名称> <tick>`
- **THEN** 更新全息图的更新间隔

#### Scenario: 设置权限
- **WHEN** 用户执行 `/wh setpermission <名称> [权限]`
- **THEN** 设置查看权限，空权限表示取消权限要求

---

### Requirement: Click Action Management
系统应提供点击动作管理功能。

#### Scenario: 添加动作
- **WHEN** 用户执行 `/wh addaction <名称> <页码> <点击类型> <动作类型> [值]`
- **THEN** 在指定页面添加点击动作

#### Scenario: 删除动作
- **WHEN** 用户执行 `/wh deleteaction <名称> <页码> <点击类型> <索引>`
- **THEN** 删除指定的点击动作

#### Scenario: 列出动作
- **WHEN** 用户执行 `/wh actions <名称> <页码> <点击类型>`
- **THEN** 列出页面的所有点击动作

---

### Requirement: Line Properties
系统应提供行属性设置功能。

#### Scenario: 设置行偏移
- **WHEN** 用户执行 `/wh offset <名称> <行号> <x> <y> <z>`
- **THEN** 设置行的位置偏移

#### Scenario: 设置行高度
- **WHEN** 用户执行 `/wh height <名称> <行号> <高度>`
- **THEN** 设置行的高度
