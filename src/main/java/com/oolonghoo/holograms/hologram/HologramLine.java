package com.oolonghoo.holograms.hologram;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
import com.oolonghoo.holograms.nms.HologramRendererPool;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 全息图行
 * 表示全息图的单行内容
 * 参考 DecentHolograms 的 HologramLine 实现
 * 
 * @author oolongho
 */
public class HologramLine {

    // 动画匹配模式 - 与 AnimationManager 保持一致
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("[<{]#?ANIM:(\\w+)(:\\S+)?[}>](.*?)[<{]/#?ANIM[}>]");

    // 默认配置值
    private static final double DEFAULT_HEIGHT_TEXT = 0.25;
    private static final double DEFAULT_HEIGHT_ICON = 0.5;
    private static final double DEFAULT_HEIGHT_HEAD = 0.6;
    private static final double DEFAULT_HEIGHT_SMALLHEAD = 0.4;
    private static final double DEFAULT_HEIGHT_ENTITY = 0.7;

    /*
     * 字段
     */

    private final HologramPage parent;
    private Location location;

    // 内容相关
    private String content;
    private HologramType type = HologramType.UNKNOWN;
    private double height;
    private HeadTexture headTexture;
    private org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.ZOMBIE;

    // 偏移
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    // 朝向
    private float facing;
    
    // 自定义朝向（null 表示跟随整体）
    private Float customYaw = null;
    private Float customPitch = null;

    // 亮度
    private Brightness brightness;

    // 文本对齐
    private TextAlignment alignment = TextAlignment.LEFT;

    // Billboard 模式
    private Billboard billboard = Billboard.CENTER;

    // 权限
    private String permission;

    // 标志
    private final Set<EnumFlag> flags;
    
    // 行级别动作
    private final Map<ClickType, List<Action>> actions;

    // 渲染器
    private NmsHologramRenderer renderer;
    private NmsHologramRenderer previousRenderer;

    // 观看者
    private final Set<UUID> viewers;

    // 玩家文本缓存（用于占位符）
    private final Map<UUID, String> playerTextCache;
    private final Map<UUID, String> lastTextCache;

    // 状态
    private boolean enabled;
    private boolean containsAnimations;
    private boolean containsPlaceholders;

    // 渲染锁
    private final Object renderMutex = new Object();

    /*
     * 构造函数
     */

    /**
     * 创建全息图行
     * 
     * @param parent 父页面
     * @param location 位置
     * @param content 内容
     */
    public HologramLine(HologramPage parent, Location location, String content) {
        this.parent = parent;
        this.location = location != null ? location.clone() : null;
        this.content = content != null ? content : "";
        this.height = DEFAULT_HEIGHT_TEXT;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.offsetZ = 0.0;
        this.facing = 0.0f;
        this.permission = null;
        this.flags = ConcurrentHashMap.newKeySet();
        this.actions = new EnumMap<>(ClickType.class);
        this.viewers = ConcurrentHashMap.newKeySet();
        this.playerTextCache = new ConcurrentHashMap<>();
        this.lastTextCache = new ConcurrentHashMap<>();
        this.enabled = true;

        // 解析内容
        parseContent();
    }

    /*
     * 内容解析方法
     */

    /**
     * 解析内容，确定类型和高度
     */
    public final void parseContent() {
        synchronized (renderMutex) {
            HologramType prevType = this.type;
            String upperContent = content.toUpperCase(Locale.ROOT);

            if (upperContent.startsWith("#ICON:")) {
                this.type = HologramType.ICON;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_ICON;
                    this.previousRenderer = this.renderer;
                    this.renderer = null; // 将由渲染器工厂创建
                }
            } else if (upperContent.startsWith("#SMALLHEAD:")) {
                this.type = HologramType.SMALLHEAD;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_SMALLHEAD;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                this.headTexture = HeadTexture.parse(content);
            } else if (upperContent.startsWith("#HEAD:")) {
                this.type = HologramType.HEAD;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_HEAD;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                this.headTexture = HeadTexture.parse(content);
            } else if (upperContent.startsWith("#ENTITY:")) {
                this.type = HologramType.ENTITY;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_ENTITY;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                parseEntityType(content);
            } else if (upperContent.equals("#NEXT") || upperContent.startsWith("#NEXT ")) {
                this.type = HologramType.NEXT;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_TEXT;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                // 自动添加 NEXT_PAGE 动作
                Hologram hologram = getHologram();
                if (hologram != null) {
                    this.actions.computeIfAbsent(ClickType.ANY, k -> new ArrayList<>())
                            .add(new Action(ActionType.NEXT_PAGE, hologram.getName()));
                }
            } else if (upperContent.equals("#PREV") || upperContent.startsWith("#PREV ")) {
                this.type = HologramType.PREV;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_TEXT;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                // 自动添加 PREV_PAGE 动作
                Hologram hologram = getHologram();
                if (hologram != null) {
                    this.actions.computeIfAbsent(ClickType.ANY, k -> new ArrayList<>())
                            .add(new Action(ActionType.PREV_PAGE, hologram.getName()));
                }
            } else {
                this.type = HologramType.TEXT;
                if (prevType != this.type) {
                    this.height = DEFAULT_HEIGHT_TEXT;
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
            }

            // 检查是否包含动画和占位符
            this.containsAnimations = checkContainsAnimations(content);
            this.containsPlaceholders = checkContainsPlaceholders(content);
        }
    }

    /**
     * 检查内容是否包含动画
     * @param text 文本
     * @return 是否包含动画
     */
    private boolean checkContainsAnimations(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return ANIMATION_PATTERN.matcher(text).find();
    }

    /**
     * 检查内容是否包含占位符
     * @param text 文本
     * @return 是否包含占位符
     */
    private boolean checkContainsPlaceholders(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // 检查 PlaceholderAPI 占位符
        return text.contains("%");
    }
    
    /**
     * 解析实体类型
     * @param content 内容
     */
    private void parseEntityType(String content) {
        if (content == null || content.isEmpty()) {
            this.entityType = org.bukkit.entity.EntityType.ZOMBIE;
            return;
        }
        
        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#ENTITY:")) {
            String entityName = content.substring(8).trim().toUpperCase(Locale.ROOT);
            try {
                this.entityType = org.bukkit.entity.EntityType.valueOf(entityName);
            } catch (IllegalArgumentException e) {
                this.entityType = org.bukkit.entity.EntityType.ZOMBIE;
            }
        } else {
            this.entityType = org.bukkit.entity.EntityType.ZOMBIE;
        }
    }

    /*
     * 显示/隐藏方法
     */

    /**
     * 显示给指定玩家
     * 
     * @param players 玩家数组
     */
    public void show(Player... players) {
        synchronized (renderMutex) {
            if (!enabled) {
                return;
            }

            hidePreviousIfNecessary();

            List<Player> playerList = (players != null && players.length > 0) 
                    ? Arrays.asList(players) 
                    : new ArrayList<>(Bukkit.getOnlinePlayers());

            for (Player player : playerList) {
                if (player == null || !player.isOnline()) {
                    continue;
                }

                if (parent != null && parent.getParent() != null && parent.getParent().isHideState(player)) {
                    continue;
                }
                
                if (canShow(player) && isInDisplayRange(player)) {
                    if (isVisible(player)) {
                        updateToPlayer(player);
                    } else {
                        displayToPlayer(player);
                        viewers.add(player.getUniqueId());
                    }
                }
            }
        }
    }

    private void updateToPlayer(Player player) {
        if (renderer != null) {
            renderer.updateText(player, this);
        }
    }

    private void displayToPlayer(Player player) {
        if (location == null) {
            return;
        }
        
        if (renderer == null) {
            createRenderer();
        }
        
        if (renderer != null) {
            renderer.render(player, location, this);
        }
    }

    private void createRenderer() {
        if (parent == null || parent.getParent() == null) {
            return;
        }
        
        WooHolograms plugin = WooHolograms.getInstance();
        
        HologramRendererPool pool = plugin.getRendererPool();
        renderer = pool.obtain(type);
        if (renderer != null) {
            return;
        }
        
        NmsHologramRendererFactory factory = plugin.getRendererFactory();
        
        renderer = switch (type) {
            case TEXT -> factory.createTextRenderer();
            case ICON -> factory.createIconRenderer();
            case HEAD -> factory.createHeadRenderer();
            case SMALLHEAD -> factory.createSmallHeadRenderer();
            case ENTITY -> factory.createEntityRenderer();
            default -> factory.createTextRenderer();
        };
    }

    /**
     * 从指定玩家隐藏
     * 
     * @param players 玩家数组
     */
    public void hide(Player... players) {
        synchronized (renderMutex) {
            hidePreviousIfNecessary();

            List<Player> playerList = (players != null && players.length > 0) 
                    ? Arrays.asList(players) 
                    : new ArrayList<>(getViewerPlayers());

            for (Player player : playerList) {
                if (renderer != null) {
                    renderer.destroy(player);
                }
                viewers.remove(player.getUniqueId());
                playerTextCache.remove(player.getUniqueId());
                lastTextCache.remove(player.getUniqueId());
            }
        }
    }

    /**
     * 更新内容给指定玩家
     * 
     * @param players 玩家数组
     */
    public void update(Player... players) {
        update(false, players);
    }

    /**
     * 更新内容给指定玩家
     * 
     * @param force 是否强制更新
     * @param players 玩家数组
     */
    public void update(boolean force, Player... players) {
        synchronized (renderMutex) {
            if (!enabled || hasFlag(EnumFlag.DISABLE_UPDATING)) {
                return;
            }

            hidePreviousIfNecessary();

            List<Player> playerList = (players != null && players.length > 0) 
                    ? Arrays.asList(players) 
                    : new ArrayList<>(getViewerPlayers());

            for (Player player : playerList) {
                if (renderer != null && (containsPlaceholders || force)) {
                    // 更新文本
                    updateTextIfNecessary(player, true);
                }
            }
        }
    }

    /**
     * 更新动画
     * 
     * @param players 玩家数组
     */
    public void updateAnimations(Player... players) {
        synchronized (renderMutex) {
            if (!enabled || type != HologramType.TEXT || hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
                return;
            }

            hidePreviousIfNecessary();

            List<Player> playerList = (players != null && players.length > 0) 
                    ? Arrays.asList(players) 
                    : new ArrayList<>(getViewerPlayers());

            for (Player player : playerList) {
                updateTextIfNecessary(player, false);
            }
        }
    }

    /**
     * 更新位置
     * 
     * @param updateRotation 是否更新旋转
     * @param players 玩家数组
     */
    public void updateLocation(boolean updateRotation, Player... players) {
        synchronized (renderMutex) {
            if (!enabled) {
                return;
            }

            hidePreviousIfNecessary();

            List<Player> playerList = (players != null && players.length > 0) 
                    ? Arrays.asList(players) 
                    : new ArrayList<>(getViewerPlayers());

            for (Player player : playerList) {
                if (renderer != null) {
                    renderer.teleport(player, getLocation());
                }
            }
        }
    }

    /**
     * 如果需要，隐藏之前的渲染器
     */
    private void hidePreviousIfNecessary() {
        synchronized (renderMutex) {
            if (previousRenderer == null) {
                return;
            }

            getViewerPlayers().forEach(previousRenderer::destroy);
            previousRenderer = null;
        }
    }

    /**
     * 更新文本（如果需要）
     * 
     * @param player 玩家
     * @param updatePlaceholders 是否更新占位符
     */
    private void updateTextIfNecessary(Player player, boolean updatePlaceholders) {
        UUID uuid = player.getUniqueId();
        String lastText = lastTextCache.get(uuid);
        String updatedText = getText(player, updatePlaceholders);

        if (!updatedText.equals(lastText)) {
            lastTextCache.put(uuid, updatedText);
            if (renderer != null) {
                renderer.updateText(player, this);
            }
        }
    }

    /**
     * 获取玩家的文本
     * 
     * @param player 玩家
     * @param update 是否更新缓存
     * @return 处理后的文本
     */
    private String getText(Player player, boolean update) {
        if (type != HologramType.TEXT) {
            return "";
        }

        UUID uuid = player.getUniqueId();
        String baseText = playerTextCache.get(uuid);

        // 更新缓存
        if (update || baseText == null) {
            baseText = content == null ? "" : content;

            // 解析占位符
            if (!hasFlag(EnumFlag.DISABLE_PLACEHOLDERS)) {
                baseText = parsePlaceholders(baseText, player);
            }

            playerTextCache.put(uuid, baseText);
        }

        // 动画每次都要重新解析（不使用缓存）
        String result = baseText;
        if (containsAnimations && !hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
            result = parseAnimations(baseText);
        }

        return ColorUtil.colorize(result);
    }

    /**
     * 解析占位符
     * 
     * @param text 文本
     * @param player 玩家
     * @return 解析后的文本
     */
    private String parsePlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 替换内部占位符
        text = text.replace("{player}", player.getName());
        text = text.replace("{page}", String.valueOf(parent != null ? parent.getIndex() + 1 : 1));
        text = text.replace("{pages}", String.valueOf(parent != null ? parent.getParent().size() : 1));

        // PlaceholderAPI 占位符由 hook 处理
        if (WooHolograms.getInstance().getPlaceholderHook() != null) {
            text = WooHolograms.getInstance().getPlaceholderHook().setPlaceholders(player, text);
        }

        return text;
    }

    /**
     * 解析动画
     * 
     * @param text 文本
     * @return 解析后的文本
     */
    private String parseAnimations(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return WooHolograms.getInstance().getAnimationManager().parseTextAnimations(text);
    }
    
    /**
     * 获取处理后的显示文本（包含动画解析）
     * 用于渲染器更新文本时使用
     * 
     * @param player 玩家
     * @return 处理后的文本
     */
    public String getDisplayText(Player player) {
        if (type != HologramType.TEXT) {
            return "";
        }
        
        String text = content == null ? "" : content;
        
        // 解析占位符
        if (!hasFlag(EnumFlag.DISABLE_PLACEHOLDERS)) {
            text = parsePlaceholders(text, player);
        }
        
        // 解析动画
        if (containsAnimations && !hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
            text = parseAnimations(text);
        }
        
        return ColorUtil.colorize(text);
    }

    /*
     * 权限和范围检查
     */

    /**
     * 检查玩家是否有权限查看此行
     * 
     * @param player 玩家
     * @return 是否有权限
     */
    public boolean hasPermission(Player player) {
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    /**
     * 检查是否可以显示给玩家
     * 
     * @param player 玩家
     * @return 是否可以显示
     */
    public boolean canShow(Player player) {
        // 检查权限
        if (!hasPermission(player)) {
            return false;
        }

        // 检查父级权限
        if (parent != null && parent.getParent() != null) {
            return parent.getParent().canShow(player);
        }

        return true;
    }

    /**
     * 检查玩家是否在显示范围内
     * 
     * @param player 玩家
     * @return 是否在范围内
     */
    public boolean isInDisplayRange(Player player) {
        if (parent == null || parent.getParent() == null) {
            return true;
        }
        return parent.getParent().isInDisplayRange(player);
    }

    /**
     * 检查玩家是否在更新范围内
     * 
     * @param player 玩家
     * @return 是否在范围内
     */
    public boolean isInUpdateRange(Player player) {
        if (parent == null || parent.getParent() == null) {
            return true;
        }
        return parent.getParent().isInUpdateRange(player);
    }

    /**
     * 更新可见性
     * 
     * @param player 玩家
     */
    public void updateVisibility(Player player) {
        synchronized (renderMutex) {
            if (isVisible(player) && !(hasPermission(player) && isInDisplayRange(player))) {
                hide(player);
            } else if (!isVisible(player) && hasPermission(player) && isInDisplayRange(player)) {
                show(player);
            }
        }
    }

    /*
     * 标志方法
     */

    /**
     * 检查是否有指定标志
     * 
     * @param flag 标志
     * @return 是否有该标志
     */
    public boolean hasFlag(EnumFlag flag) {
        if (flags.contains(flag)) {
            return true;
        }
        // 检查父级标志
        if (parent != null && parent.getParent() != null) {
            return parent.getParent().hasFlag(flag);
        }
        return false;
    }

    /**
     * 添加标志
     * 
     * @param flags 标志数组
     */
    public void addFlags(EnumFlag... flags) {
        if (flags != null) {
            for (EnumFlag flag : flags) {
                if (flag != null) {
                    this.flags.add(flag);
                }
            }
        }
    }

    /**
     * 移除标志
     * 
     * @param flag 标志
     */
    public void removeFlag(EnumFlag flag) {
        this.flags.remove(flag);
    }

    /**
     * 清除所有标志
     */
    public void clearFlags() {
        this.flags.clear();
    }

    /*
     * 观看者方法
     */

    /**
     * 检查玩家是否正在查看
     * 
     * @param player 玩家
     * @return 是否正在查看
     */
    public boolean isVisible(Player player) {
        return viewers.contains(player.getUniqueId());
    }

    /**
     * 获取所有观看者玩家
     * 
     * @return 观看者玩家列表
     */
    public List<Player> getViewerPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    /*
     * 序列化方法
     */

    /**
     * 序列化为 Map
     * 
     * @return 序列化后的 Map
     */
    public Map<String, Object> serializeToMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("content", content);
        map.put("height", height);

        if (!flags.isEmpty()) {
            map.put("flags", flags.stream().map(EnumFlag::name).collect(Collectors.toList()));
        }

        if (permission != null && !permission.trim().isEmpty()) {
            map.put("permission", permission);
        }

        if (offsetX != 0.0) {
            map.put("offsetX", offsetX);
        }

        if (offsetZ != 0.0) {
            map.put("offsetZ", offsetZ);
        }

        if (parent == null || facing != parent.getParent().getFacing()) {
            map.put("facing", facing);
        }

        if (brightness != null) {
            map.put("brightness", brightness.getSkyLight() + "," + brightness.getBlockLight());
        }

        if (alignment != TextAlignment.LEFT) {
            map.put("alignment", alignment.getId());
        }

        if (billboard != Billboard.CENTER) {
            map.put("billboard", billboard.getId());
        }

        return map;
    }

    /**
     * 从 Map 创建 HologramLine
     * 
     * @param map Map 数据
     * @param parent 父页面
     * @param location 位置
     * @return HologramLine 实例
     */
    @SuppressWarnings("unchecked")
    public static HologramLine fromMap(Map<String, Object> map, HologramPage parent, Location location) {
        String content = (String) map.getOrDefault("content", "");
        HologramLine line = new HologramLine(parent, location, content);

        if (map.containsKey("height") && map.get("height") instanceof Number height) {
            line.setHeight(height.doubleValue());
        }

        if (map.containsKey("flags") && map.get("flags") instanceof List<?> flagsList) {
            try {
                for (Object flagObj : flagsList) {
                    if (flagObj instanceof String flagStr) {
                        EnumFlag flag = EnumFlag.fromId(flagStr);
                        if (flag != null) {
                            line.addFlags(flag);
                        }
                    }
                }
            } catch (Exception e) {
                if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                    WooHolograms.getInstance().getLogger().warning(() -> "Failed to parse flags: " + e.getMessage());
                }
            }
        }

        if (map.containsKey("permission")) {
            line.setPermission((String) map.get("permission"));
        }

        if (map.containsKey("offsetX") && map.get("offsetX") instanceof Number offsetX) {
            line.setOffsetX(offsetX.doubleValue());
        }

        if (map.containsKey("offsetZ") && map.get("offsetZ") instanceof Number offsetZ) {
            line.setOffsetZ(offsetZ.doubleValue());
        }

        if (map.containsKey("facing") && map.get("facing") instanceof Number facing) {
            line.setFacing(facing.floatValue());
        }

        if (map.containsKey("brightness") && map.get("brightness") instanceof String brightnessObj) {
            String[] parts = brightnessObj.split(",");
            if (parts.length == 2) {
                try {
                    int sky = Integer.parseInt(parts[0].trim());
                    int block = Integer.parseInt(parts[1].trim());
                    line.setBrightness(Brightness.of(sky, block));
                } catch (NumberFormatException e) {
                    if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                        WooHolograms.getInstance().getLogger().warning(() -> "Failed to parse brightness: " + brightnessObj);
                    }
                }
            }
        }

        if (map.containsKey("alignment") && map.get("alignment") instanceof String alignmentObj) {
            line.setAlignment(TextAlignment.fromId(alignmentObj));
        }

        if (map.containsKey("billboard") && map.get("billboard") instanceof String billboardObj) {
            line.setBillboard(Billboard.fromId(billboardObj));
        }

        return line;
    }

    /**
     * 克隆此行
     * 
     * @param parent 新父页面
     * @param location 新位置
     * @return 克隆的行
     */
    public HologramLine clone(HologramPage parent, Location location) {
        HologramLine line = new HologramLine(parent, location, this.content);
        line.setHeight(this.height);
        line.setOffsetX(this.offsetX);
        line.setOffsetY(this.offsetY);
        line.setOffsetZ(this.offsetZ);
        line.setFacing(this.facing);
        line.setPermission(this.permission);
        line.setBrightness(this.brightness);
        line.setAlignment(this.alignment);
        line.setBillboard(this.billboard);
        line.addFlags(this.flags.toArray(EnumFlag[]::new));
        return line;
    }

    /*
     * 启用/禁用方法
     */

    /**
     * 启用此行
     */
    public void enable() {
        this.enabled = true;
        show();
    }

    /**
     * 禁用此行
     */
    public void disable() {
        this.enabled = false;
        hide();
    }

    /**
     * 销毁此行
     */
    public void destroy() {
        hide();
        if (renderer != null) {
            renderer.destroy(getViewerPlayers());
            WooHolograms plugin = WooHolograms.getInstance();
            HologramRendererPool pool = plugin.getRendererPool();
            pool.release(renderer);
            renderer = null;
        }
        if (previousRenderer != null) {
            previousRenderer.destroy(getViewerPlayers());
            previousRenderer = null;
        }
        viewers.clear();
        playerTextCache.clear();
        lastTextCache.clear();
    }

    /*
     * Getter 和 Setter
     */

    public HologramPage getParent() {
        return parent;
    }

    public Hologram getHologram() {
        return parent != null ? parent.getParent() : null;
    }

    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    public void setLocation(Location location) {
        synchronized (renderMutex) {
            Location oldLocation = this.location;
            this.location = location != null ? location.clone() : null;
            
            if (renderer != null && oldLocation != null && !oldLocation.equals(location)) {
                for (Player viewer : getViewerPlayers()) {
                    renderer.destroy(viewer);
                    renderer.render(viewer, this.location, this);
                }
            }
        }
    }

    public String getContent() {
        return content;
    }

    /**
     * 设置内容
     * 
     * @param content 新内容
     */
    public void setContent(String content) {
        synchronized (renderMutex) {
            this.content = content != null ? content : "";
            parseContent();
            update(true);
        }
    }

    public HologramType getType() {
        synchronized (renderMutex) {
            return type;
        }
    }

    public HeadTexture getHeadTexture() {
        return headTexture;
    }
    
    public org.bukkit.entity.EntityType getEntityType() {
        return entityType;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public float getFacing() {
        return facing;
    }

    public void setFacing(float facing) {
        this.facing = facing;
    }

    public Float getCustomYaw() {
        return customYaw;
    }

    public void setCustomYaw(Float customYaw) {
        synchronized (renderMutex) {
            this.customYaw = customYaw;
            update(true);
        }
    }

    public Float getCustomPitch() {
        return customPitch;
    }

    public void setCustomPitch(Float customPitch) {
        synchronized (renderMutex) {
            this.customPitch = customPitch;
            update(true);
        }
    }

    public boolean hasCustomFacing() {
        return customYaw != null || customPitch != null;
    }

    public void clearCustomFacing() {
        this.customYaw = null;
        this.customPitch = null;
    }

    // ==================== 行级别动作方法 ====================

    public boolean hasActions() {
        return actions.values().stream().anyMatch(list -> list != null && !list.isEmpty());
    }

    public void addAction(ClickType clickType, Action action) {
        actions.computeIfAbsent(clickType, k -> new ArrayList<>()).add(action);
    }

    public List<Action> getActions(ClickType clickType) {
        return actions.getOrDefault(clickType, new ArrayList<>());
    }

    public Map<ClickType, List<Action>> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    public void clearActions(ClickType clickType) {
        actions.remove(clickType);
    }

    public void clearAllActions() {
        actions.clear();
    }

    public void removeAction(ClickType clickType, int index) {
        List<Action> actionList = actions.get(clickType);
        if (actionList != null && index >= 0 && index < actionList.size()) {
            actionList.remove(index);
        }
    }

    public void executeActions(Player player, ClickType clickType) {
        List<Action> actionsToExecute = new ArrayList<>();
        
        if (actions.containsKey(clickType)) {
            List<Action> actionList = actions.get(clickType);
            if (actionList != null) {
                actionsToExecute.addAll(actionList);
            }
        }
        
        if (clickType != ClickType.ANY && actions.containsKey(ClickType.ANY)) {
            List<Action> anyActions = actions.get(ClickType.ANY);
            if (anyActions != null) {
                actionsToExecute.addAll(anyActions);
            }
        }
        
        if (actionsToExecute.isEmpty()) {
            return;
        }
        
        for (Action action : actionsToExecute) {
            if (!action.execute(player)) {
                break;
            }
        }
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Set<EnumFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public NmsHologramRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(NmsHologramRenderer renderer) {
        this.renderer = renderer;
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Brightness getBrightness() {
        return brightness;
    }

    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TextAlignment alignment) {
        this.alignment = alignment != null ? alignment : TextAlignment.LEFT;
    }

    public Billboard getBillboard() {
        return billboard;
    }

    public void setBillboard(Billboard billboard) {
        this.billboard = billboard != null ? billboard : Billboard.CENTER;
    }

    public int[] getEntityIds() {
        if (renderer == null) {
            return new int[0];
        }
        return renderer.getEntityIds().stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public String toString() {
        return "HologramLine{" +
                "content='" + content + '\'' +
                ", type=" + type +
                ", height=" + height +
                ", enabled=" + enabled +
                '}';
    }
}
