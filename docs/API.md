# WooHolograms API 文档（v2）

适用于 WooHolograms **1.2.0+**。API 形状版本 `2.0`：同一主版本内保证二进制向后兼容（只新增，不修改/删除既有签名）。

## 引入依赖

### Gradle（JitPack）

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.oolongho:wooholograms-api:1.2.0")
}
```

### 插件依赖

在 `plugin.yml` 中声明依赖（保证加载顺序）：

```yaml
depend: [WooHolograms]
```

## 获取 API 实例

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("WooHolograms") == null) {
            return;
        }
        // 延迟一 tick，确保 WooHolograms 已完成加载
        getServer().getScheduler().runTask(this, () -> {
            HologramApi api = WooHologramsApiProvider.getOrThrow();
            getLogger().info("WooHolograms API " + api.getApiVersion() + " 就绪");
        });
    }
}
```

## 线程约定

所有 API 方法必须在**服务器主线程**（Folia 下为全息图所在区域的线程）调用。
建议在自己的事件监听/任务中直接调用；跨线程场景请使用 Bukkit 调度器回主线程。

事件处理器中调用同一全息图的其他 API 方法是安全的（内部状态锁可重入），
但请避免在事件处理器中执行重量级操作。

## 创建全息图（Builder）

```java
Hologram holo = api.holograms().builder("shop", location)
        .line(HologramLines.text("&6&l★ 商店 ★"))
        .line(HologramLines.icon(Material.EMERALD))
        .offsetLine(HologramLines.text("&e限时"), 1.2, 0.5, 0)  // 偏移行（悬浮）
        .page()                                                  // 开启第二页
        .line("&a第二页内容")
        .lineHeight(0.3)
        .billboard(Billboard.CENTER)
        .backgroundAlpha(64)
        .permission("shop.use")
        .create();   // 名称非法/已存在/创建事件被取消时抛 IllegalStateException

// 克隆已有全息图（正确入口是注册表，而非 Hologram 上的方法）
Hologram copy = api.holograms().cloneHologram("shop", "shop2", otherLocation, false);
```

### 临时全息图

```java
// 不写入文件，重启消失
Hologram temp = api.holograms().builder("preview", loc)
        .line("预览内容")
        .temporary()
        .create();

// 定时销毁（20 tick = 1 秒），适合副本特效/商店预览
Hologram effect = api.holograms().builder("fx", loc)
        .line(HologramLines.animation("wave", "&a技能特效"))
        .expireAfter(100)
        .create();
```

## 行内容工厂

用类型化工厂替代手写 `#ICON:` 等魔法前缀（`api.content.HologramLines`）：

| 工厂 | 说明 |
|------|------|
| `text(String)` | 普通文本（`&` 颜色代码、PAPI 占位符、`{player}`/`{page}`/`{pages}`） |
| `icon(Material)` / `iconCustom(String)` | 物品图标（原版 / CraftEngine 自定义） |
| `head(String)` / `smallHead(String)` | 玩家头颅（玩家名、Base64、`HDB:<id>`） |
| `block(Material)` / `blockCustom(String)` | 方块（原版 / CraftEngine 自定义） |
| `entity(EntityType)` | 实体 |
| `animation(name, content, args...)` | 包裹动画 |

## 查询与编辑

```java
HologramRegistry registry = api.holograms();

// 查询
Hologram holo = registry.getHologram("shop");
registry.getAllHolograms().forEach(h -> h.getName());
registry.getHologramsInWorld("world").size();

// 页面与行操作
HologramPage page = holo.getPage(0);
page.addLine(HologramLines.text("&a新行"));
page.setLine(0, HologramLines.text("&c新标题"));
holo.nextPage(player);

// 批量属性修改（一次性提交）
holo.edit()
        .billboard(Billboard.CENTER)
        .lineHeight(0.3)
        .backgroundAlpha(32)
        .apply();

// 附近查询
for (Hologram near : registry.getHologramsNear(location, 16)) {
    near.getName();
}

// 动作操作（行级/页面级，格式与配置文件一致）
page.addAction(ClickType.LEFT, "MESSAGE:你好 {player}");
page.addAction(ClickType.RIGHT, "CONSOLE:say 有人点击了商店");
page.addAction(ClickType.RIGHT, "SHOP_OPEN:shop1");   // 自定义注册的动作类型
page.getActionData(ClickType.RIGHT);   // 读取（可回写 addAction）
page.clearActions(ClickType.LEFT);
holo.executeActions(player, ClickType.RIGHT);   // 程序化触发当前页动作

// 玩家会话
holo.show(player);
holo.hide(player);
holo.setHidePlayer(player);   // 强制隐藏（无视距离自动显示）
holo.switchPage(player, 2);

// 删除
registry.deleteHologram("shop");
```

## 事件

| 事件 | 触发时机 | 可取消 |
|------|----------|--------|
| `HologramClickEvent` | 玩家点击全息图（含 `getHitLine()` 命中行） | ✅ |
| `HologramActionExecuteEvent` | 点击动作即将执行（携带路由结果） | ✅ |
| `HologramPageSwitchEvent` | 玩家翻页 | ✅ |
| `HologramMoveEvent` | 全息图移动 | ✅ |
| `HologramCreateEvent` | 全息图创建 | ✅ |
| `HologramDeleteEvent` | 全息图删除 | ❌ |
| `HologramEditEvent` | 批量编辑器提交后 | ❌ |
| `HologramsLoadedEvent` | 全息图加载完成 | ❌ |

```java
@EventHandler
public void onClick(HologramClickEvent event) {
    HologramLine line = event.getHitLine();
    if (line != null && event.getClickType() == ClickType.LEFT) {
        event.getPlayer().sendMessage("你点击了: " + line.getContent());
    }
}
```

## 注册自定义动作

```java
api.actions().register("SHOP_OPEN", (player, args) -> {
    player.sendMessage("打开商店: " + String.join(" ", args));
    return true;   // false 中断后续动作
});
// 注册后即可在动作配置中使用,也可通过 API 挂载:
page.addAction(ClickType.LEFT, "SHOP_OPEN:shop1");
```

内置类型（`NONE`/`MESSAGE`/`COMMAND`/`CONSOLE`/`SOUND`/`TELEPORT`/`SERVER`/`NEXT_PAGE`/`PREV_PAGE`/`PAGE`）不可覆盖；同名自定义动作重复注册视为更新。

## 注册自定义动画

```java
public class ReverseAnimation implements TextAnimation {
    @Override public String getName() { return "reverse"; }
    @Override public int getSpeed() { return 4; }
    @Override public int getPause() { return 0; }
    @Override public List<String> getAliases() { return List.of(); }
    @Override public String animate(String text, long step, String... args) {
        return new StringBuilder(text).reverse().toString();
    }
}

api.animations().register(new ReverseAnimation());
// 行内容中使用: <#ANIM:reverse>&aHello</#ANIM>
```

> 注意：插件重载（`/wh reload`）会重建动画表。第三方插件应监听 `HologramsLoadedEvent` 并在其中重新注册。

## 兼容层

- **DecentHolograms API 兼容层**：`provides: DecentHolograms` + DHAPI 兼容包继续可用，与 v2 API 相互独立。
- **v1 静态门面**（`WooHologramsAPI`）：已标记 `@Deprecated`，保留转发，不再新增功能。
