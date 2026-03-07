# WooHolograms

🍵一款多功能、有GUI的 Minecraft 全息文字插件

## 特色

### 🎨 丰富的显示类型
- **文本显示**：支持颜色代码、PlaceholderAPI、动画效果
- **物品图标**：展示任意物品的全息图
- **玩家头颅**：支持 Base64 材质、玩家名称、HeadDatabase
- **自定义实体**：展示任意实体类型的全息图
- **翻页按钮**：内置 #NEXT/#PREV 快速翻页功能

### 🌟 高级视觉效果
- **亮度控制**：自定义天空光和方块光亮度（0-15）
- **文本对齐**：支持左对齐、居中、右对齐
- **Billboard 模式**：固定、垂直、水平、中心四种朝向模式
- **渐变动画**：流畅的颜色渐变效果
- **双面显示**：文本可双面渲染

### 🎭 动画系统
- **波浪动画**：`<#ANIM:wave>文本</#ANIM>`，支持自定义颜色参数
- **打字机动画**：`<#ANIM:typewriter>文本</#ANIM>`，逐字显示效果
- **闪烁动画**：`<#ANIM:blink>文本</#ANIM>`，支持速度参数
- **滚动动画**：`<#ANIM:scroll>文本</#ANIM>`，支持宽度参数
- **渐变动画**：`<#ANIM:gradient:red,blue>文本</#ANIM>`，多色渐变效果
- **自定义动画**：通过配置文件创建个性化动画

### 🖱️ 交互功能
- **点击动作**：左键、右键、Shift+左键、Shift+右键
- **行级别动作**：每行可独立设置点击动作
- **页面动作**：全息图级别的点击动作
- **动作类型**：命令、消息、音效、传送、翻页等

### ⚙️ 行级别自定义
- **独立朝向**：每行可设置独立的 yaw/pitch，支持不同朝向
- **独立偏移**：每行可设置独立的 X/Y/Z 轴偏移
- **独立高度**：每行可设置独立的显示高度
- **独立亮度**：每行可设置独立的天空光和方块光亮度
- **独立权限**：每行可设置独立的查看权限

### 🔧 技术特性
- **NMS 原生**：1.21+ 原生支持，性能优异
- **智能更新**：仅更新变更的数据
- **TAB 补全**：完善的命令补全支持
- **GUI 管理**：可视化编辑界面，使用 lime 玻璃板填充，界面清晰美观

## 环境

- Minecraft 1.21+
- Java 21+
- PlaceholderAPI（可选）
- HeadDatabase（可选，用于头颅材质）

## 命令

### 基础命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh create <名称>` | 创建全息图 | `wooholograms.admin` |
| `/wh delete <名称>` | 删除全息图 | `wooholograms.admin` |
| `/wh copy <名称> <新名称>` | 复制全息图 | `wooholograms.admin` |
| `/wh list [页码]` | 列出所有全息图 | `wooholograms.admin` |
| `/wh info <名称>` | 查看全息图详情 | `wooholograms.admin` |
| `/wh gui [名称]` | 打开 GUI 管理界面 | `wooholograms.admin` |
| `/wh near [范围]` | 显示附近全息图 | `wooholograms.admin` |
| `/wh reload` | 重载配置 | `wooholograms.admin` |

### 位置命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh movehere <名称>` | 移动到当前位置 | `wooholograms.admin` |
| `/wh moveto <名称> <x> <y> <z> [世界]` | 移动到指定坐标 | `wooholograms.admin` |
| `/wh teleport <名称>` | 传送到全息图位置 | `wooholograms.admin` |

### 行管理命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh addline <名称> <内容>` | 添加行 | `wooholograms.admin` |
| `/wh setline <名称> <行号> <内容>` | 设置行内容 | `wooholograms.admin` |
| `/wh deleteline <名称> <行号>` | 删除行 | `wooholograms.admin` |
| `/wh insertline <名称> <行号> <内容>` | 插入行 | `wooholograms.admin` |
| `/wh offset <名称> <行号> <偏移>` | 设置行偏移 | `wooholograms.admin` |
| `/wh height <名称> <行号> <高度>` | 设置行高度 | `wooholograms.admin` |

### 页面管理命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh addpage <名称>` | 添加页面 | `wooholograms.admin` |
| `/wh deletepage <名称> <页码>` | 删除页面 | `wooholograms.admin` |
| `/wh swappage <名称> <页码1> <页码2>` | 交换两个页面 | `wooholograms.admin` |

### 属性设置命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh setrange <名称> <范围>` | 设置显示范围 | `wooholograms.admin` |
| `/wh setinterval <名称> <间隔>` | 设置更新间隔 | `wooholograms.admin` |
| `/wh setpermission <名称> [权限]` | 设置查看权限 | `wooholograms.admin` |
| `/wh setfacing <名称> <模式> [角度]` | 设置全息图朝向 | `wooholograms.admin` |
| `/wh setdoublesided <名称> <true|false>` | 设置双面显示 | `wooholograms.admin` |
| `/wh enable <名称>` | 启用全息图 | `wooholograms.admin` |
| `/wh disable <名称>` | 禁用全息图 | `wooholograms.admin` |

### 动作管理命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wh actions <名称>` | 查看动作列表 | `wooholograms.admin` |
| `/wh addaction <名称> <行号> <点击类型> <动作>` | 添加点击动作 | `wooholograms.admin` |
| `/wh deleteaction <名称> <行号> <动作索引>` | 删除动作 | `wooholograms.admin` |

## 行类型格式

| 格式 | 描述 | 示例 |
|------|------|------|
| 普通文本 | 显示文本内容 | `&a欢迎来到服务器！` |
| 多行文本 | 使用 `/n` 或 `\n` 换行 | `&a第一行/n&b第二行` |
| `#ICON:<物品>` | 显示物品图标 | `#ICON:DIAMOND` |
| `#HEAD:<类型>:<值>` | 显示玩家头颅 | `#HEAD:PLAYER:Notch` |
| `#HEAD:URL:<Base64>` | 显示自定义皮肤头颅 | `#HEAD:URL:eyJ0ZXh0...` |
| `#HEAD:HDB:<ID>` | HeadDatabase 头颅 | `#HEAD:HDB:12345` |
| `#SMALLHEAD:...` | 小型头颅显示 | `#SMALLHEAD:PLAYER:Notch` |
| `#ENTITY:<实体类型>` | 显示实体 | `#ENTITY:ZOMBIE` |
| `#NEXT` | 下一页按钮 | `#NEXT 下一页` |
| `#PREV` | 上一页按钮 | `#PREV 上一页` |

### 物品参数

在 `#ICON` 后可添加参数：

```
#ICON:DIAMOND_SWORD custom-model-data:10000 name:&6传说之剑 glow
```

| 参数 | 描述 | 示例 |
|------|------|------|
| `custom-model-data:<值>` | 自定义模型数据 | `custom-model-data:10000` |
| `cmd:<值>` | 自定义模型数据简写 | `cmd:10000` |
| `color:<RGB>` | 皮革颜色 | `color:FF0000` |
| `name:<名称>` | 自定义名称 | `name:&6传说之剑` |
| `lore:<描述>` | 物品描述 | `lore:&7这是描述` |
| `glow` | 发光效果 | `glow` |
| `unbreakable` | 无法破坏 | `unbreakable` |

## 动画格式

### 基本语法

支持两种格式：`<#ANIM:名称>文本</#ANIM>` 或 `{#ANIM:名称}文本{/#ANIM}`

### 如何设置动画颜色

**方法一：在动画标签外添加颜色代码**
```
&a<#ANIM:typewriter>欢迎来到服务器</#ANIM>
&c<#ANIM:blink>重要公告</#ANIM>
&b<#ANIM:scroll:15>这是一条很长的滚动公告内容</#ANIM>
```

**方法二：使用动画参数（仅部分动画支持）**
```
<#ANIM:wave:&c,&e>波浪文字</#ANIM>     <!-- 波浪动画支持颜色参数 -->
<#ANIM:gradient:red,blue>渐变</#ANIM>  <!-- 渐变动画支持颜色参数 -->
```

### 内置动画

| 动画 | 格式 | 参数说明 | 示例 |
|------|------|----------|------|
| 波浪 | `<#ANIM:wave:主色,副色>文本</#ANIM>` | 主色、副色（颜色代码） | `<#ANIM:wave:&e,&f>Hello</#ANIM>` |
| 打字机 | `<#ANIM:typewriter>文本</#ANIM>` | 无参数，在标签外加颜色 | `&a<#ANIM:typewriter>欢迎</#ANIM>` |
| 闪烁 | `<#ANIM:blink:速度>文本</#ANIM>` | 速度（数字，默认10） | `&c<#ANIM:blink:5>重要</#ANIM>` |
| 滚动 | `<#ANIM:scroll:宽度>文本</#ANIM>` | 宽度（数字，默认20） | `&b<#ANIM:scroll:15>公告</#ANIM>` |
| 渐变 | `<#ANIM:gradient:颜色1,颜色2,...>文本</#ANIM>` | 颜色（颜色名或HEX） | `<#ANIM:gradient:red,blue>渐变</#ANIM>` |

### 渐变动画颜色支持

渐变动画支持以下颜色格式：
- **颜色名称**：`red`、`blue`、`green`、`yellow`、`cyan`、`magenta`、`white`、`black`、`orange`、`purple`、`pink`、`gold`、`gray`、`aqua`、`lime` 等
- **HEX 格式**：`#FF0000`、`#00FF00`、`#0000FF` 等

示例：
```
<#ANIM:gradient:red,blue>红蓝渐变</#ANIM>
<#ANIM:gradient:#FF0000,#00FF00,#0000FF>三色渐变</#ANIM>
<#ANIM:gradient:gold,orange,red>火焰效果</#ANIM>
```

## Billboard 模式

| 模式 | 描述 |
|------|------|
| `fixed` | 固定朝向 | 固定朝向指定角度 |
| `horizontal` | 水平跟随 | 水平方向跟随玩家视角，垂直方向固定 |
| `vertical` | 垂直跟随 | 垂直方向跟随玩家视角，水平方向固定 |
| `all` | 完全跟随 | 完全跟随玩家视角（默认） |

## 动作类型

| 类型 | 描述 | 示例 |
|------|------|------|
| `COMMAND` | 以玩家身份执行命令 | `COMMAND:spawn` |
| `CONSOLE` | 以控制台身份执行命令 | `CONSOLE:give {player} diamond 1` |
| `MESSAGE` | 发送消息 | `MESSAGE:&a你好 {player}！` |
| `SOUND` | 播放音效 | `SOUND:ENTITY_PLAYER_LEVELUP` |
| `TELEPORT` | 传送玩家 | `TELEPORT:world,100,64,200` |
| `SERVER` | 连接到其他服务器（BungeeCord） | `SERVER:lobby` |
| `NEXT_PAGE` | 下一页 | `NEXT_PAGE` |
| `PREV_PAGE` | 上一页 | `PREV_PAGE` |
| `PAGE` | 跳转到指定页 | `PAGE:3` |

### 点击类型

| 类型 | 描述 |
|------|------|
| `ANY` | 任意点击 |
| `LEFT` | 左键点击 |
| `RIGHT` | 右键点击 |
| `SHIFT_LEFT` | Shift+左键 |
| `SHIFT_RIGHT` | Shift+右键 |

## 内置变量

| 变量 | 描述 |
|------|------|
| `{player}` | 玩家名称 |
| `{player_uuid}` | 玩家 UUID |
| `{player_displayname}` | 玩家显示名称 |
| `{player_x}` | 玩家 X 坐标 |
| `{player_y}` | 玩家 Y 坐标 |
| `{player_z}` | 玩家 Z 坐标 |
| `{player_world}` | 玩家所在世界 |
| `{player_health}` | 玩家生命值 |
| `{player_level}` | 玩家等级 |

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
holo.getPage(0).addLine("#ICON:DIAMOND");
holo.getPage(0).addLine("#NEXT 下一页");

// 设置行独立朝向
HologramLine line = holo.getPage(0).getLine(0);
line.setCustomYaw(90);
line.setCustomPitch(0);

// 添加行级别动作
Action action = new Action(ActionType.COMMAND, "spawn");
line.addAction(ClickType.LEFT, action);

// 添加页面级别动作
holo.getPage(0).addAction(ClickType.RIGHT, new Action(ActionType.MESSAGE, "&a点击了！"));

// 显示给玩家
holo.show(player);

// 监听点击事件
@EventHandler
public void onHologramClick(HologramClickEvent event) {
    Player player = event.getPlayer();
    Hologram hologram = event.getHologram();
    ClickType clickType = event.getClickType();
    // 你的逻辑
}
```

## 权限

| 权限 | 描述 |
|------|------|
| `wooholograms.admin` | 管理员权限（所有命令） |
| `wooholograms.view` | 查看全息图权限 |
| `wooholograms.create` | 创建全息图权限 |
| `wooholograms.delete` | 删除全息图权限 |
| `wooholograms.edit` | 编辑全息图权限 |
| `wooholograms.reload` | 重载配置权限 |

---

❤️ 主包是开发新手，如果有做得不好的地方，欢迎指正。希望能和大家一起交流！

⭐ 觉得有用请给个 Star 爱你哟
