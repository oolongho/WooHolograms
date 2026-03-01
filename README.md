# WooHolograms

🍵一款现代化的 Minecraft 全息图插件

## 特色

### 🎨 丰富的显示类型
- **文本显示**：支持颜色代码、PlaceholderAPI、动画效果
- **物品图标**：展示任意物品的全息图
- **玩家头颅**：支持 Base64 材质、玩家名称、HeadDatabase
- **自定义实体**：展示任意实体类型的全息图

### 🌟 高级视觉效果
- **亮度控制**：自定义天空光和方块光亮度（0-15）
- **文本对齐**：支持左对齐、居中、右对齐
- **Billboard 模式**：固定、垂直、水平、中心四种朝向模式
- **渐变动画**：流畅的颜色渐变效果

### 🎭 动画系统
- **内置动画**：波浪、闪烁、打字机、滚动效果
- **渐变动画**：多色渐变，支持自定义速度
- **自定义动画**：通过配置文件创建个性化动画

### 🖱️ 交互功能
- **点击动作**：左键、右键、Shift+左键、Shift+右键
- **动作类型**：命令、消息、音效、传送等
- **权限控制**：支持设置查看权限

### 📱 完整 GUI 系统
- **可视化编辑**：全息图列表、详情、行编辑
- **聊天输入**：便捷的文本输入方式
- **即时反馈**：操作结果实时显示

### ⚡ 性能优化
- **渲染器缓存池**：对象复用，减少 GC 压力
- **Display Entity**：1.21+ 原生支持，性能优异
- **智能更新**：仅更新变更的数据

## 环境

- Minecraft 1.21+
- Java 21+
- PlaceholderAPI（可选）
- HeadDatabase（可选，用于头颅材质）

## 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh create <名称>` | 创建全息图 | `wooholograms.admin` |
| `/wh delete <名称>` | 删除全息图 | `wooholograms.admin` |
| `/wh list [页码]` | 列出所有全息图 | `wooholograms.admin` |
| `/wh info <名称>` | 查看全息图详情 | `wooholograms.admin` |
| `/wh gui [名称]` | 打开 GUI 管理界面 | `wooholograms.admin` |
| `/wh movehere <名称>` | 移动到当前位置 | `wooholograms.admin` |
| `/wh teleport <名称>` | 传送到全息图位置 | `wooholograms.admin` |
| `/wh copy <名称> <新名称>` | 复制全息图 | `wooholograms.admin` |
| `/wh addline <名称> <内容>` | 添加行 | `wooholograms.admin` |
| `/wh setline <名称> <行号> <内容>` | 设置行内容 | `wooholograms.admin` |
| `/wh deleteline <名称> <行号>` | 删除行 | `wooholograms.admin` |
| `/wh insertline <名称> <行号> <内容>` | 插入行 | `wooholograms.admin` |
| `/wh addpage <名称>` | 添加页面 | `wooholograms.admin` |
| `/wh deletepage <名称> <页码>` | 删除页面 | `wooholograms.admin` |
| `/wh setrange <名称> <范围>` | 设置显示范围 | `wooholograms.admin` |
| `/wh setinterval <名称> <间隔>` | 设置更新间隔 | `wooholograms.admin` |
| `/wh setpermission <名称> [权限]` | 设置查看权限 | `wooholograms.admin` |
| `/wh setfacing <名称> <行号> <模式> [角度]` | 设置行朝向 | `wooholograms.admin` |
| `/wh addaction <名称> <行号> <点击类型> <动作>` | 添加点击动作 | `wooholograms.admin` |
| `/wh near [范围]` | 显示附近全息图 | `wooholograms.admin` |
| `/wh reload` | 重载配置 | `wooholograms.admin` |

## 行类型格式

| 格式 | 描述 | 示例 |
|------|------|------|
| 普通文本 | 显示文本内容 | `&a欢迎来到服务器！` |
| `#ICON:<物品>` | 显示物品图标 | `#ICON:DIAMOND` |
| `#HEAD:<类型>:<值>` | 显示玩家头颅 | `#HEAD:PLAYER:Notch` |
| `#HEAD:URL:<Base64>` | 显示自定义皮肤头颅 | `#HEAD:URL:eyJ0ZXh0...` |
| `#HEAD:HDB:<ID>` | HeadDatabase 头颅 | `#HEAD:HDB:12345` |
| `#SMALLHEAD:...` | 小型头颅显示 | `#SMALLHEAD:PLAYER:Notch` |
| `#ENTITY:<实体类型>` | 显示实体 | `#ENTITY:ZOMBIE` |

## 动画格式

| 格式 | 描述 | 示例 |
|------|------|------|
| `<#ANIM:wave>文本</#ANIM>` | 波浪动画 | `<#ANIM:wave>Hello</#ANIM>` |
| `<#ANIM:flash>文本</#ANIM>` | 闪烁动画 | `<#ANIM:flash>重要</#ANIM>` |
| `<#ANIM:typewriter>文本</#ANIM>` | 打字机效果 | `<#ANIM:typewriter>欢迎</#ANIM>` |
| `<#ANIM:scroll>文本</#ANIM>` | 滚动动画 | `<#ANIM:scroll>公告</#ANIM>` |
| `<#ANIM:gradient:色1:色2>文本</#ANIM>` | 渐变动画 | `<#ANIM:gradient:#FF0000:#0000FF>彩虹</#ANIM>` |

## 朝向模式

| 模式 | 描述 | 使用场景 |
|------|------|----------|
| `fixed_angle` | 固定角度 | 全息图固定朝向某个方向，需要指定角度（0-360度） |
| `horizontal` | 水平跟随 | 水平方向跟随玩家视角，垂直方向固定 |
| `vertical` | 垂直跟随 | 垂直方向跟随玩家视角，水平方向固定 |
| `all` | 完全跟随 | 完全跟随玩家视角（默认） |

**命令示例**:
```
/wh setfacing testholo 1 fixed_angle 45   # 固定45度角
/wh setfacing testholo 1 horizontal       # 水平跟随
/wh setfacing testholo 1 all              # 完全跟随（默认）
```

## PlaceholderAPI 变量

| 变量 | 描述 |
|------|------|
| `%wooholograms_count%` | 全息图总数 |
| `%wooholograms_player_page%` | 玩家当前查看的页面 |

## API 使用示例

```java
WooHologramsAPI api = WooHologramsAPI.getInstance();

// 创建全息图
Hologram holo = api.createHologram("test", player.getLocation());

// 添加行
holo.getPage(0).addLine("&a欢迎！");

// 添加点击动作
Action action = new Action(ActionType.COMMAND, "spawn");
holo.getPage(0).addAction(ClickType.LEFT, action);

// 显示给玩家
holo.show(player);

// 监听点击事件
@EventHandler
public void onHologramClick(HologramClickEvent event) {
    Player player = event.getPlayer();
    Hologram hologram = event.getHologram();
    // 你的逻辑
}
```

---

❤️ 主包是开发新手，如果有做得不好的地方，欢迎指正。希望能和大家一起交流！

⭐ 觉得有用请给个 Star 爱你哟
