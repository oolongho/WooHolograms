# WooHolograms

🍵 一款多功能、有GUI的 Minecraft 全息文字插件

## 特色

### 🎨 显示类型

- **文本**：支持颜色代码、PlaceholderAPI、动画效果
- **物品**：基于 ItemDisplay 实体渲染，支持附魔光效
- **方块**：基于 BlockDisplay 实体渲染，展示任意方块
- **玩家头颅**：支持 Base64 材质、玩家名称、HeadDatabase
- **自定义实体**：展示任意实体类型的全息图
- **翻页按钮**：内置 #NEXT/#PREV 快速翻页功能

### 🌟 视觉效果

- **亮度控制**：自定义天空光和方块光亮度（0-15）
- **文本对齐**：支持左对齐、居中、右对齐，多行文本自动对齐形成统一矩形背景
- **背景透明度**：自定义文本背景透明度（0=透明 \~ 255=不透明）
- **背景颜色**：支持颜色名称和十六进制（#FF0000）自定义背景颜色
- **朝向模式**：固定、垂直、水平、中心四种朝向模式
- **渐变动画**：流畅的颜色渐变效果
- **双面显示**：文本双面渲染
- **背景渐变**：背景色和发光色沿 HSL 色轮动态渐变
- **Display 属性**：缩放（X/Y/Z）、阴影、发光颜色、亮度，全息图级统一设置（可单独覆盖）

### 🎭 动画系统

- **波浪动画**：`<#ANIM:wave>文本</#ANIM>`，支持自定义颜色参数
- **打字机动画**：`<#ANIM:typewriter>文本</#ANIM>`，逐字显示效果
- **闪烁动画**：`<#ANIM:blink>文本</#ANIM>`，支持速度参数
- **滚动动画**：`<#ANIM:scroll>文本</#ANIM>`，支持宽度参数
- **渐变动画**：`<#ANIM:gradient:red,blue>文本</#ANIM>`，多色渐变效果
- **自定义动画**：通过配置文件创建个性化动画
- **动画预编译**：注册时预计算帧数组，运行时零解析开销

### 🖱️ 交互功能

- **点击动作**：左键、右键、Shift+左键、Shift+右键、任意点击
- **行级别动作**：每行可独立设置点击动作
- **页面动作**：全息图级别的点击动作
- **动作类型**：命令、消息、音效、传送、翻页等
- **点击冷却**：防止快速点击刷动作，可配置冷却时间

### 🔧 更多特性

- **Display Entity**：基于 1.21+ 的 TextDisplay/ItemDisplay/BlockDisplay 实体，性能优异
- **Folia 支持**：完整兼容 Folia 区域化多线程调度
- **增量渲染**：仅更新变更的数据，减少网络包发送
- **对象池**：渲染器对象复用，减少 GC 压力
- **动画预编译**：帧数据预计算缓存，运行时零解析
- **轻量分析器**：内置 Profiler，按需启用，定位性能瓶颈
- **数据转换**：支持从 HolographicDisplays 一键导入
- **DH 兼容**：自动读取和迁移 DecentHolograms 配置，内置 API 兼容层
- **细粒度权限**：每个命令独立权限节点
- **TAB 补全**：完善的命令补全支持
- **GUI 管理**：可视化编辑界面，所有功能均可通过 GUI 操作

## 命令

### 基础命令

| 命令                    | 描述          | 权限                            |
| --------------------- | ----------- | ----------------------------- |
| `/wh create <名称>`     | 创建全息图       | `wooholograms.command.create` |
| `/wh delete <名称>`     | 删除全息图       | `wooholograms.command.delete` |
| `/wh copy <名称> <新名称>` | 复制全息图       | `wooholograms.command.copy`   |
| `/wh list [页码]`       | 列出所有全息图     | `wooholograms.command.list`   |
| `/wh info <名称>`       | 查看全息图详情     | `wooholograms.command.info`   |
| `/wh gui [名称]`        | 打开 GUI 管理界面 | `wooholograms.command.gui`    |
| `/wh near [范围]`       | 显示附近全息图     | `wooholograms.command.near`   |
| `/wh reload`          | 重载配置        | `wooholograms.command.reload` |

### 位置命令

| 命令                                 | 描述       | 权限                              |
| ---------------------------------- | -------- | ------------------------------- |
| `/wh movehere <名称>`                | 移动到当前位置  | `wooholograms.command.movehere` |
| `/wh moveto <名称> <x> <y> <z> [世界]` | 移动到指定坐标  | `wooholograms.command.moveto`   |
| `/wh teleport <名称>`                | 传送到全息图位置 | `wooholograms.command.teleport` |

### 行管理命令

| 命令                              | 描述    | 权限                                |
| ------------------------------- | ----- | --------------------------------- |
| `/wh addline <名称> <内容>`         | 添加行   | `wooholograms.command.addline`    |
| `/wh setline <名称> <行号> <内容>`    | 设置行内容 | `wooholograms.command.setline`    |
| `/wh deleteline <名称> <行号>`      | 删除行   | `wooholograms.command.removeline` |
| `/wh insertline <名称> <行号> <内容>` | 插入行   | `wooholograms.command.insertline` |
| `/wh offset <名称> <行号> <偏移>`     | 设置行偏移 | `wooholograms.command.offset`     |
| `/wh height <名称> <行号> <高度>`     | 设置行高度 | `wooholograms.command.height`     |

### 页面管理命令

| 命令                              | 描述     | 权限                                |
| ------------------------------- | ------ | --------------------------------- |
| `/wh addpage <名称>`              | 添加页面   | `wooholograms.command.addpage`    |
| `/wh deletepage <名称> <页码>`      | 删除页面   | `wooholograms.command.removepage` |
| `/wh swappage <名称> <页码1> <页码2>` | 交换两个页面 | `wooholograms.command.swappage`   |

### 属性设置命令

| 命令                                                       | 描述      | 权限                                    |
| -------------------------------------------------------- | ------- | ------------------------------------- |
| `/wh setrange <名称> <范围>`                                 | 设置显示范围  | `wooholograms.command.setrange`       |
| `/wh setinterval <名称> <间隔>`                              | 设置更新间隔  | `wooholograms.command.setinterval`    |
| `/wh setpermission <名称> [权限]`                            | 设置查看权限  | `wooholograms.command.setpermission`  |
| `/wh setfacing <名称> <模式> [角度]`                           | 设置全息图朝向 | `wooholograms.command.setfacing`      |
| `/wh setdoublesided <名称> <true\|false>`                  | 设置双面显示  | `wooholograms.command.setdoublesided` |
| `/wh setscale <名称> [行号] <x> <y> <z>`                     | 设置缩放    | `wooholograms.command.setscale`       |
| `/wh setshadow <名称> [行号] <半径> <强度>`                      | 设置阴影    | `wooholograms.command.setshadow`      |
| `/wh setglowcolor <名称> [行号] <颜色\|#RRGGBB\|reset>`        | 设置发光颜色  | `wooholograms.command.setglowcolor`   |
| `/wh setchroma <名称> [行号] background\|glow <true\|false>` | 设置彩虹渐变  | `wooholograms.command.setchroma`      |
| `/wh enable <名称>`                                        | 启用全息图   | `wooholograms.command.enable`         |
| `/wh disable <名称>`                                       | 禁用全息图   | `wooholograms.command.disable`        |

### 动作管理命令

| 命令                                    | 描述     | 权限                                  |
| ------------------------------------- | ------ | ----------------------------------- |
| `/wh actions <名称>`                    | 查看动作列表 | `wooholograms.command.actions`      |
| `/wh addaction <名称> <行号> <点击类型> <动作>` | 添加点击动作 | `wooholograms.command.addaction`    |
| `/wh deleteaction <名称> <行号> <动作索引>`   | 删除动作   | `wooholograms.command.deleteaction` |

### 工具命令

| 命令                                | 描述                         | 权限                              |
| --------------------------------- | -------------------------- | ------------------------------- |
| `/wh convert holographicdisplays` | 从 HolographicDisplays 导入数据 | `wooholograms.command.convert`  |
| `/wh profiler [on\|off\|reset]`   | 查看/控制性能分析器                 | `wooholograms.command.profiler` |

## 行类型格式

| 格式                   | 描述                  | 示例                        |
| -------------------- | ------------------- | ------------------------- |
| 普通文本                 | 显示文本内容              | `&a欢迎来到服务器！`              |
| 多行文本                 | 使用 `\n` 换行          | `&a第一行\n&b第二行`            |
| `#ICON:<物品>`         | 显示物品图标（ItemDisplay） | `#ICON:DIAMOND`           |
| `#BLOCK:<方块>`        | 显示方块（BlockDisplay）  | `#BLOCK:STONE`            |
| `#HEAD:<类型>:<值>`     | 显示玩家头颅              | `#HEAD:PLAYER:Notch`      |
| `#HEAD:URL:<Base64>` | 显示自定义皮肤头颅           | `#HEAD:URL:eyJ0ZXh0...`   |
| `#HEAD:HDB:<ID>`     | HeadDatabase 头颅     | `#HEAD:HDB:12345`         |
| `#SMALLHEAD:...`     | 小型头颅显示              | `#SMALLHEAD:PLAYER:Notch` |
| `#ENTITY:<实体类型>`     | 显示实体                | `#ENTITY:ZOMBIE`          |
| `#NEXT`              | 下一页按钮               | `#NEXT 下一页`               |
| `#PREV`              | 上一页按钮               | `#PREV 上一页`               |

### 物品参数

在 `#ICON` 后可添加参数：

```
#ICON:DIAMOND_SWORD custom-model-data:10000 name:&6传说之剑 glow
```

| 参数                      | 描述        | 示例                        |
| ----------------------- | --------- | ------------------------- |
| `custom-model-data:<值>` | 自定义模型数据   | `custom-model-data:10000` |
| `cmd:<值>`               | 自定义模型数据简写 | `cmd:10000`               |
| `color:<RGB>`           | 皮革颜色      | `color:FF0000`            |
| `name:<名称>`             | 自定义名称     | `name:&6传说之剑`             |
| `lore:<描述>`             | 物品描述      | `lore:&7这是描述`             |
| `glow`                  | 发光效果      | `glow`                    |
| `unbreakable`           | 无法破坏      | `unbreakable`             |

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
<#ANIM:wave:&c,&e>波浪文字</#ANIM>
<#ANIM:gradient:red,blue>渐变</#ANIM>
```

### 内置动画

| 动画  | 格式                                       | 参数说明        | 示例                                    |
| --- | ---------------------------------------- | ----------- | ------------------------------------- |
| 波浪  | `<#ANIM:wave:主色,副色>文本</#ANIM>`           | 主色、副色（颜色代码） | `<#ANIM:wave:&e,&f>Hello</#ANIM>`     |
| 打字机 | `<#ANIM:typewriter>文本</#ANIM>`           | 无参数，在标签外加颜色 | `&a<#ANIM:typewriter>欢迎</#ANIM>`      |
| 闪烁  | `<#ANIM:blink:速度>文本</#ANIM>`             | 速度（数字，默认10） | `&c<#ANIM:blink:5>重要</#ANIM>`         |
| 滚动  | `<#ANIM:scroll:宽度>文本</#ANIM>`            | 宽度（数字，默认20） | `&b<#ANIM:scroll:15>公告</#ANIM>`       |
| 渐变  | `<#ANIM:gradient:颜色1,颜色2,...>文本</#ANIM>` | 颜色（颜色名或HEX） | `<#ANIM:gradient:red,blue>渐变</#ANIM>` |

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

## 朝向模式

| 模式           | 描述                |
| ------------ | ----------------- |
| `fixed`      | 固定朝向指定角度          |
| `horizontal` | 水平方向跟随玩家视角，垂直方向固定 |
| `vertical`   | 垂直方向跟随玩家视角，水平方向固定 |
| `all`        | 完全跟随玩家视角（默认）      |

## 文本对齐

全息图级别的文本对齐设置，所有文本行自动对齐形成统一矩形背景：

| 对齐方式     | 描述      | 效果       |
| -------- | ------- | -------- |
| `LEFT`   | 左对齐（默认） | 短行右侧填充空格 |
| `CENTER` | 居中对齐    | 短行两侧填充空格 |
| `RIGHT`  | 右对齐     | 短行左侧填充空格 |

## 背景设置

### 背景透明度

控制文本背景的透明度，范围为 0-255：

- `0` = 完全透明（不可见背景）
- `128` = 半透明（默认）
- `255` = 完全不透明

### 背景颜色

支持颜色名称和十六进制格式：

| 颜色名称                  | 值       | 颜色名称        | 值       |
| --------------------- | ------- | ----------- | ------- |
| black                 | #000000 | white       | #FFFFFF |
| red                   | #FF0000 | green       | #00FF00 |
| blue                  | #0000FF | yellow      | #FFFF00 |
| aqua / cyan           | #00FFFF | gray / grey | #808080 |
| dark\_red             | #AA0000 | dark\_green | #00AA00 |
| dark\_blue            | #0000AA | dark\_aqua  | #00AAAA |
| dark\_purple / purple | #AA00AA | dark\_gray  | #404040 |
| gold / orange         | #FFAA00 | <br />      | <br />  |

也支持十六进制格式：`#FF0000`、`#00FF00` 等

### 彩虹渐变

背景色和发光色沿 HSL 色轮动态渐变，每帧更新：

```
/wh setchroma <名称> background true     # 全息图级：背景色彩虹渐变
/wh setchroma <名称> glow true           # 全息图级：发光色彩虹渐变
/wh setchroma <名称> 2 background true   # 行级：第2行背景色彩虹渐变
/wh setchroma <名称> 2 glow true         # 行级：第2行发光色彩虹渐变
```

## 动作类型

| 类型          | 描述                   | 示例                                |
| ----------- | -------------------- | --------------------------------- |
| `COMMAND`   | 以玩家身份执行命令            | `COMMAND:spawn`                   |
| `CONSOLE`   | 以控制台身份执行命令           | `CONSOLE:give {player} diamond 1` |
| `MESSAGE`   | 发送消息                 | `MESSAGE:&a你好 {player}！`          |
| `SOUND`     | 播放音效                 | `SOUND:ENTITY_PLAYER_LEVELUP`     |
| `TELEPORT`  | 传送玩家                 | `TELEPORT:world,100,64,200`       |
| `SERVER`    | 连接到其他服务器（BungeeCord） | `SERVER:lobby`                    |
| `NEXT_PAGE` | 下一页                  | `NEXT_PAGE`                       |
| `PREV_PAGE` | 上一页                  | `PREV_PAGE`                       |
| `PAGE`      | 跳转到指定页               | `PAGE:3`                          |

### 点击类型

| 类型            | 描述       |
| ------------- | -------- |
| `ANY`         | 任意点击     |
| `LEFT`        | 左键点击     |
| `RIGHT`       | 右键点击     |
| `SHIFT_LEFT`  | Shift+左键 |
| `SHIFT_RIGHT` | Shift+右键 |

## 内置变量

| 变量                     | 描述      |
| ---------------------- | ------- |
| `{player}`             | 玩家名称    |
| `{player_uuid}`        | 玩家 UUID |
| `{player_displayname}` | 玩家显示名称  |
| `{player_x}`           | 玩家 X 坐标 |
| `{player_y}`           | 玩家 Y 坐标 |
| `{player_z}`           | 玩家 Z 坐标 |
| `{player_world}`       | 玩家所在世界  |
| `{player_health}`      | 玩家生命值   |
| `{player_level}`       | 玩家等级    |

## PlaceholderAPI 变量

| 变量                           | 描述        |
| ---------------------------- | --------- |
| `%wooholograms_count%`       | 全息图总数     |
| `%wooholograms_player_page%` | 玩家当前查看的页面 |

## API 使用示例

```java
import com.oolonghoo.holograms.api.WooHologramsAPI;
import com.oolonghoo.holograms.api.event.HologramClickEvent;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;

// 检查 API 是否可用
if (!WooHologramsAPI.isLoaded()) {
    return;
}

// 创建全息图（返回 Optional）
Optional<Hologram> opt = WooHologramsAPI.createHologram("test", player.getLocation());
if (opt.isEmpty()) {
    player.sendMessage("创建失败，名称可能已存在");
    return;
}
Hologram holo = opt.get();

// 添加行
HologramPage page = holo.getPage(0);
page.addLine("&a欢迎！");
page.addLine("#ICON:DIAMOND");
page.addLine("#BLOCK:GOLD_BLOCK");
page.addLine("#NEXT 下一页");

// 设置全息图级别 Display 属性
holo.setScale(1.5f, 1.5f, 1.0f);           // 缩放
holo.setGlowColor(0xFF0000);                 // 发光颜色（纯 RGB）
holo.setBrightness(new Brightness(15, 15));  // 亮度（天空光, 方块光）
holo.setChromaBackground(true);              // 彩虹渐变背景
holo.setChromaGlow(true);                    // 彩虹渐变发光

// 非 TEXT 行可单独覆盖
HologramLine iconLine = page.getLine(1);
iconLine.setScale(2.0f, 2.0f, 2.0f);
iconLine.setShadowRadius(0.5f);
iconLine.setBillboard(Billboard.FIXED_ANGLE);
iconLine.setCustomYaw(90f);
iconLine.setCustomPitch(0f);

// 添加动作
iconLine.addAction(ClickType.LEFT, new Action(ActionType.COMMAND, "spawn"));
page.addAction(ClickType.RIGHT, new Action(ActionType.MESSAGE, "&a点击了！"));

// 显示给玩家
holo.show(player);

// 获取已有全息图
Optional<Hologram> existing = WooHologramsAPI.getHologram("test");
existing.ifPresent(h -> h.show(player));

// 监听点击事件
@EventHandler
public void onHologramClick(HologramClickEvent event) {
    Player player = event.getPlayer();
    Hologram hologram = event.getHologram();
    HologramPage page = event.getPage();
    ClickType clickType = event.getClickType();
}
```

## 权限

### 管理员权限

| 权限                       | 描述             | 默认 |
| ------------------------ | -------------- | -- |
| `wooholograms.admin`     | 管理员权限（包含所有子权限） | OP |
| `wooholograms.command.*` | 所有命令权限         | OP |

### 命令权限

| 权限                                    | 描述      |
| ------------------------------------- | ------- |
| `wooholograms.command.create`         | 创建全息图   |
| `wooholograms.command.delete`         | 删除全息图   |
| `wooholograms.command.copy`           | 复制全息图   |
| `wooholograms.command.near`           | 显示附近全息图 |
| `wooholograms.command.enable`         | 启用全息图   |
| `wooholograms.command.disable`        | 禁用全息图   |
| `wooholograms.command.list`           | 列出全息图   |
| `wooholograms.command.info`           | 查看详情    |
| `wooholograms.command.teleport`       | 传送到全息图  |
| `wooholograms.command.movehere`       | 移动到当前位置 |
| `wooholograms.command.moveto`         | 移动到指定坐标 |
| `wooholograms.command.addline`        | 添加行     |
| `wooholograms.command.removeline`     | 删除行     |
| `wooholograms.command.setline`        | 设置行内容   |
| `wooholograms.command.insertline`     | 插入行     |
| `wooholograms.command.addpage`        | 添加页面    |
| `wooholograms.command.removepage`     | 删除页面    |
| `wooholograms.command.swappage`       | 交换页面    |
| `wooholograms.command.setrange`       | 设置显示范围  |
| `wooholograms.command.setinterval`    | 设置更新间隔  |
| `wooholograms.command.setpermission`  | 设置查看权限  |
| `wooholograms.command.setfacing`      | 设置朝向    |
| `wooholograms.command.setdoublesided` | 设置双面显示  |
| `wooholograms.command.addaction`      | 添加动作    |
| `wooholograms.command.deleteaction`   | 删除动作    |
| `wooholograms.command.actions`        | 查看动作    |
| `wooholograms.command.offset`         | 设置偏移    |
| `wooholograms.command.height`         | 设置高度    |
| `wooholograms.command.reload`         | 重载配置    |
| `wooholograms.command.setpage`        | 设置页面    |
| `wooholograms.command.gui`            | 打开GUI   |
| `wooholograms.command.help`           | 查看帮助    |
| `wooholograms.command.convert`        | 导入数据    |
| `wooholograms.command.profiler`       | 性能分析器   |
| `wooholograms.command.setscale`       | 设置缩放    |
| `wooholograms.command.setshadow`      | 设置阴影    |
| `wooholograms.command.setglowcolor`   | 设置发光颜色  |
| `wooholograms.command.setchroma`      | 设置彩虹渐变  |

## DecentHolograms 配置兼容

WooHolograms 支持读取 DecentHolograms 的配置文件格式，方便从 DH 迁移：

- 首次启动时自动检测 `plugins/DecentHolograms/holograms/` 目录下的 `.yml` 文件
- 自动将 DH 格式数据迁移到 `plugins/WooHolograms/holograms/` 目录
- 迁移后使用 WooHolograms 原生格式存储，后续编辑自动保存为新格式
- 支持读取 DH 的 kebab-case 键名（如 `facing-direction`）和 WH 的 camelCase 键名（如 `facingDirection`）

## DecentHolograms API 兼容

WooHolograms 内置了 DecentHolograms API 兼容层，通过 `provides: DecentHolograms` 声明和同包名同类名委托模式实现。

**当服务器未安装 DecentHolograms 时**，依赖 DH API 的插件会自动使用 WooHolograms 作为后端，无需修改任何代码。

覆盖的 DHAPI 方法：

- 创建/删除全息图
- 页面操作（添加/插入/删除/获取）
- 行操作（添加/插入/设置/删除/获取）
- 传送全息图
- 显示/隐藏全息图

## 数据导入

### HolographicDisplays

使用 `/wh convert holographicdisplays`（别名 `hd`）导入：

- 扫描 `plugins/HolographicDisplays/` 目录下的 `.yml` 文件
- 自动解析 HD 格式的全息图位置和文本内容
- 名称冲突时自动跳过并提示
- 不支持 HD 3.x 数据库文件（会提示）

### CMI

使用 `/wh convert cmi` 导入：

- 读取 `plugins/CMI/Saves/holograms.yml`（回退 `plugins/CMI/holograms.yml`）
- 自动跳过 CMI 生成的翻页按钮全息图（`#>` / `#<` 结尾）
- `!nextpage!` 分页符自动分割为多页，每页添加翻页动作
- `ICON:` 行自动转换为 `#ICON:` 格式

## 性能分析器

内置轻量 Profiler，默认禁用，按需开启：

```
/wh profiler on       # 启用
/wh profiler          # 查看报告
/wh profiler reset    # 重置数据
/wh profiler off      # 禁用
```

报告按总耗时降序显示各模块的平均耗时和调用次数，帮助定位性能瓶颈。

### 配置文件格式

每个全息图一个 `.yml` 文件，存储在 `plugins/WooHolograms/holograms/` 目录下：

```yaml
location: world,100.0,64.0,200.0,0.0,0.0
enabled: true
display-range: 48
update-range: 48
update-interval: 20
facing: 0.0
down-origin: false
billboard: CENTER
double-sided: false
line-height: 0.3
permission: null
scale-x: 1.0
scale-y: 1.0
scale-z: 1.0
shadow-radius: 0
shadow-strength: 1.0
glow-color: -1
chroma-background: false
chroma-glow: false
pages:
  '1':
    lines:
      '1':
        content: '&a欢迎来到服务器！'
        height: 0.3
        offsetX: 0.0
        offsetY: 0.0
        offsetZ: 0.0
      '2':
        content: '#ICON:DIAMOND'
        height: 0.6
      '3':
        content: '#BLOCK:GOLD_BLOCK'
        height: 0.5
      '4':
        content: '#NEXT 下一页'
        height: 0.3
    actions:
      ANY:
        - 'MESSAGE:&a你点击了全息图！'
```

***

❤️ 主包是开发新手，如果有做得不好的地方，欢迎指正。希望能和大家一起交流！

⭐ 觉得有用请给个 Star 爱你哟
