package com.oolongho.holograms.hologram;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.HologramManager;
import com.oolongho.holograms.nms.NmsHologramRenderer;
import com.oolongho.holograms.nms.NmsHologramRendererFactory;
import com.oolongho.holograms.nms.HologramRendererPool;
import com.oolongho.holograms.util.ColorUtil;
import com.oolongho.holograms.util.PlaceholderUtil;
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
 */
public class HologramLine {

    // 动画匹配模式 - 与 AnimationManager 保持一致
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("[<{]#?ANIM:(\\w+)(:\\S+)?[}>](.*?)[<{]/#?ANIM[}>]");

    // 缓存的空实体ID数组
    private static final int[] EMPTY_IDS = new int[0];

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
    private org.bukkit.Material blockMaterial = org.bukkit.Material.STONE;

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

    // Billboard 模式
    private Billboard billboard;

    // Display Entity 属性（null 表示继承全息图级别值）
    private Float scaleX = null;
    private Float scaleY = null;
    private Float scaleZ = null;
    private Double translationX = null;
    private Double translationY = null;
    private Double translationZ = null;
    private Float shadowRadius = null;  // null 表示继承
    private Float shadowStrength = null; // null 表示继承
    private Integer glowColor = null;    // null 表示不覆盖

    // Chroma 彩虹色（null 表示继承全息图级别值）
    private Boolean chromaBackground = null;
    private Boolean chromaGlow = null;

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
            } else if (upperContent.startsWith("#BLOCK:")) {
                this.type = HologramType.BLOCK;
                if (prevType != this.type) {
                    this.height = HologramType.BLOCK.getDefaultHeight();
                    this.previousRenderer = this.renderer;
                    this.renderer = null;
                }
                parseBlockType(content);
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
                removeAutoPageActions(ClickType.ANY, ActionType.NEXT_PAGE);
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
                removeAutoPageActions(ClickType.ANY, ActionType.PREV_PAGE);
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

    private void registerEntityIds() {
        if (renderer == null) return;
        Hologram hologram = getHologram();
        if (hologram == null) return;
        HologramManager manager = WooHolograms.getInstance().getHologramManager();
        for (int id : renderer.getEntityIds()) {
            manager.registerEntityId(id, hologram);
        }
    }

    private void unregisterEntityIds(NmsHologramRenderer r) {
        if (r == null) return;
        HologramManager manager = WooHolograms.getInstance().getHologramManager();
        for (int id : r.getEntityIds()) {
            manager.unregisterEntityId(id);
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

    private void removeAutoPageActions(ClickType clickType, ActionType pageActionType) {
        List<Action> actionList = actions.get(clickType);
        if (actionList != null) {
            actionList.removeIf(a -> a.getType() == pageActionType);
            if (actionList.isEmpty()) {
                actions.remove(clickType);
            }
        }
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
        if (text.contains("%")) {
            return true;
        }
        return text.contains("{player}") || text.contains("{page}") || text.contains("{pages}");
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

    /**
     * 解析方块类型
     * @param content 内容
     */
    private void parseBlockType(String content) {
        if (content == null || content.isEmpty()) {
            this.blockMaterial = org.bukkit.Material.STONE;
            return;
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#BLOCK:")) {
            String materialName = content.substring(7).trim();
            org.bukkit.Material material = org.bukkit.Material.matchMaterial(materialName);
            this.blockMaterial = material != null && material.isBlock() ? material : org.bukkit.Material.STONE;
        } else {
            this.blockMaterial = org.bukkit.Material.STONE;
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

            // TEXT 行由 PageTextRendererImpl 统一管理显示
            if (type == HologramType.TEXT) {
                // 如果从其他类型变为TEXT，需要清理旧渲染器
                hidePreviousIfNecessary();
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

        // TEXT 行由 PageTextRendererImpl 统一渲染，不需要独立渲染器
        if (type == HologramType.TEXT) {
            this.renderer = null;
            return;
        }

        WooHolograms plugin = WooHolograms.getInstance();

        HologramRendererPool pool = plugin.getRendererPool();
        renderer = pool.obtain(type);
        if (renderer != null) {
            registerEntityIds();
            return;
        }

        NmsHologramRendererFactory factory = plugin.getRendererFactory();

        renderer = switch (type) {
            case TEXT -> factory.createTextRenderer();
            case ICON -> factory.createIconRenderer();
            case HEAD -> factory.createHeadRenderer();
            case SMALLHEAD -> factory.createSmallHeadRenderer();
            case BLOCK -> factory.createBlockRenderer();
            case ENTITY -> factory.createEntityRenderer();
            default -> factory.createTextRenderer();
        };
        registerEntityIds();
    }

    /**
     * 从指定玩家隐藏
     * 
     * @param players 玩家数组
     */
    public void hide(Player... players) {
        synchronized (renderMutex) {
            // TEXT 行由 PageTextRendererImpl 统一管理隐藏
            if (type == HologramType.TEXT) {
                return;
            }

            hidePreviousIfNecessary();

            if (players != null && players.length > 0) {
                for (Player player : players) {
                    if (renderer != null) {
                        renderer.destroy(player);
                    }
                    viewers.remove(player.getUniqueId());
                    playerTextCache.remove(player.getUniqueId());
                    lastTextCache.remove(player.getUniqueId());
                }
            } else {
                for (UUID uuid : viewers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline() && renderer != null) {
                        renderer.destroy(player);
                    }
                }
                viewers.clear();
                playerTextCache.clear();
                lastTextCache.clear();
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

            // TEXT 行由 PageTextRendererImpl 统一管理更新
            if (type == HologramType.TEXT) {
                // 如果从其他类型变为TEXT，需要清理旧渲染器
                hidePreviousIfNecessary();
                return;
            }

            hidePreviousIfNecessary();

            if (players != null && players.length > 0) {
                for (Player player : players) {
                    if (renderer != null && (containsPlaceholders || force)) {
                        updateTextIfNecessary(player, true);
                    }
                }
            } else {
                for (UUID uuid : viewers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline() && renderer != null && (containsPlaceholders || force)) {
                        updateTextIfNecessary(player, true);
                    }
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
            if (!enabled || hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
                return;
            }

            // TEXT 行动画由 PageTextRendererImpl 统一管理
            if (type == HologramType.TEXT) {
                return;
            }

            hidePreviousIfNecessary();

            if (players != null && players.length > 0) {
                for (Player player : players) {
                    updateTextIfNecessary(player, false);
                }
            } else {
                for (UUID uuid : viewers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        updateTextIfNecessary(player, false);
                    }
                }
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

            // TEXT 行位置由 PageTextRendererImpl 统一管理
            if (type == HologramType.TEXT) {
                return;
            }

            hidePreviousIfNecessary();

            if (players != null && players.length > 0) {
                for (Player player : players) {
                    if (renderer != null) {
                        renderer.teleport(player, getLocation());
                    }
                }
            } else {
                for (UUID uuid : viewers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline() && renderer != null) {
                        renderer.teleport(player, getLocation());
                    }
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

            List<Player> viewerPlayers = new ArrayList<>();
            for (UUID uuid : viewers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    viewerPlayers.add(p);
                }
            }
            previousRenderer.destroy(viewerPlayers);
            unregisterEntityIds(previousRenderer);
            WooHolograms plugin = WooHolograms.getInstance();
            HologramRendererPool pool = plugin.getRendererPool();
            pool.release(previousRenderer);
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
        String updatedText;
        if (type == HologramType.TEXT) {
            updatedText = getText(player, updatePlaceholders);
        } else {
            updatedText = content == null ? "" : content;
            if (updatePlaceholders && !hasFlag(EnumFlag.DISABLE_PLACEHOLDERS)) {
                updatedText = parsePlaceholders(updatedText, player);
            }
        }

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

        text = text.replace("{page}", String.valueOf(parent != null ? parent.getIndex() + 1 : 1));
        text = text.replace("{pages}", String.valueOf(parent != null && parent.getParent() != null ? parent.getParent().size() : 1));

        text = PlaceholderUtil.replace(text, player);

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
        return getText(player, true);
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
        // 继承页面级标志
        if (parent != null) {
            if (parent.hasFlag(flag)) {
                return true;
            }
            // 继承全息图级标志
            Hologram holo = parent.getParent();
            if (holo != null && holo.hasFlag(flag)) {
                return true;
            }
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

        if (offsetY != 0.0) {
            map.put("offsetY", offsetY);
        }

        if (offsetZ != 0.0) {
            map.put("offsetZ", offsetZ);
        }

        if (parent == null || parent.getParent() == null || facing != parent.getParent().getFacing()) {
            map.put("facing", facing);
        }

        if (customYaw != null) {
            map.put("custom-yaw", customYaw);
        }

        if (customPitch != null) {
            map.put("custom-pitch", customPitch);
        }

        if (brightness != null) {
            map.put("brightness", brightness.getSkyLight() + "," + brightness.getBlockLight());
        }

        if (billboard != null) {
            map.put("billboard", billboard.getId());
        }

        // Display Entity 属性
        if (scaleX != null) map.put("scale-x", scaleX);
        if (scaleY != null) map.put("scale-y", scaleY);
        if (scaleZ != null) map.put("scale-z", scaleZ);

        if (translationX != null) map.put("translation-x", translationX);
        if (translationY != null) map.put("translation-y", translationY);
        if (translationZ != null) map.put("translation-z", translationZ);

        if (shadowRadius != null) {
            map.put("shadow-radius", shadowRadius);
        }

        if (shadowStrength != null) {
            map.put("shadow-strength", shadowStrength);
        }

        if (glowColor != null) {
            map.put("glow-color", glowColor);
        }

        if (chromaBackground != null) {
            map.put("chroma-background", chromaBackground);
        }

        if (chromaGlow != null) {
            map.put("chroma-glow", chromaGlow);
        }

        if (hasActions()) {
            Map<String, List<String>> actionsMap = new LinkedHashMap<>();
            for (Map.Entry<ClickType, List<Action>> entry : actions.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    actionsMap.put(entry.getKey().name(),
                            entry.getValue().stream().map(Action::toString).collect(Collectors.toList()));
                }
            }
            map.put("actions", actionsMap);
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

        if (map.containsKey("offsetY") && map.get("offsetY") instanceof Number offsetY) {
            line.setOffsetY(offsetY.doubleValue());
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

        if (map.containsKey("billboard") && map.get("billboard") instanceof String billboardObj) {
            line.setBillboard(Billboard.fromId(billboardObj));
        } else {
            line.setBillboard(null);
        }

        // Display Entity 属性（支持扁平格式和嵌套 Map 格式）
        if (map.containsKey("scale") && map.get("scale") instanceof Map<?, ?> scaleMap) {
            // 旧格式：嵌套 Map
            Float sx = scaleMap.containsKey("x") && scaleMap.get("x") instanceof Number n ? n.floatValue() : null;
            Float sy = scaleMap.containsKey("y") && scaleMap.get("y") instanceof Number n ? n.floatValue() : null;
            Float sz = scaleMap.containsKey("z") && scaleMap.get("z") instanceof Number n ? n.floatValue() : null;
            line.setScale(sx, sy, sz);
        } else {
            // 新格式：扁平键
            Float sx = map.containsKey("scale-x") && map.get("scale-x") instanceof Number n ? n.floatValue() : null;
            Float sy = map.containsKey("scale-y") && map.get("scale-y") instanceof Number n ? n.floatValue() : null;
            Float sz = map.containsKey("scale-z") && map.get("scale-z") instanceof Number n ? n.floatValue() : null;
            if (sx != null || sy != null || sz != null) {
                line.setScale(sx, sy, sz);
            }
        }

        if (map.containsKey("translation") && map.get("translation") instanceof Map<?, ?> translationMap) {
            // 旧格式：嵌套 Map
            Double tx = translationMap.containsKey("x") && translationMap.get("x") instanceof Number n ? n.doubleValue() : null;
            Double ty = translationMap.containsKey("y") && translationMap.get("y") instanceof Number n ? n.doubleValue() : null;
            Double tz = translationMap.containsKey("z") && translationMap.get("z") instanceof Number n ? n.doubleValue() : null;
            line.setTranslation(tx, ty, tz);
        } else {
            // 新格式：扁平键
            Double tx = map.containsKey("translation-x") && map.get("translation-x") instanceof Number n ? n.doubleValue() : null;
            Double ty = map.containsKey("translation-y") && map.get("translation-y") instanceof Number n ? n.doubleValue() : null;
            Double tz = map.containsKey("translation-z") && map.get("translation-z") instanceof Number n ? n.doubleValue() : null;
            if (tx != null || ty != null || tz != null) {
                line.setTranslation(tx, ty, tz);
            }
        }

        if (map.containsKey("shadow-radius") && map.get("shadow-radius") instanceof Number shadowRadius) {
            line.setShadowRadius(shadowRadius.floatValue());
        }

        if (map.containsKey("shadow-strength") && map.get("shadow-strength") instanceof Number shadowStrength) {
            line.setShadowStrength(shadowStrength.floatValue());
        }

        if (map.containsKey("glow-color") && map.get("glow-color") instanceof Number glowColor) {
            line.setGlowColor(glowColor.intValue());
        }

        if (map.containsKey("chroma-background") && map.get("chroma-background") instanceof Boolean chromaBg) {
            line.setChromaBackground(chromaBg);
        }

        if (map.containsKey("chroma-glow") && map.get("chroma-glow") instanceof Boolean chromaGl) {
            line.setChromaGlow(chromaGl);
        }

        if (map.containsKey("custom-yaw") && map.get("custom-yaw") instanceof Number customYaw) {
            line.setCustomYaw(customYaw.floatValue());
        }

        if (map.containsKey("custom-pitch") && map.get("custom-pitch") instanceof Number customPitch) {
            line.setCustomPitch(customPitch.floatValue());
        }

        if (map.containsKey("actions") && map.get("actions") instanceof Map<?, ?> actionsMap) {
            for (Map.Entry<?, ?> entry : actionsMap.entrySet()) {
                if (entry.getKey() instanceof String clickTypeName && entry.getValue() instanceof List<?> actionList) {
                    try {
                        ClickType clickType = ClickType.valueOf(clickTypeName);
                        for (Object actionObj : actionList) {
                            if (actionObj instanceof String actionStr) {
                                Action action = Action.fromString(actionStr);
                                if (action != null) {
                                    line.addAction(clickType, action);
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // ignore unknown click type
                    }
                }
            }
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
        line.setBillboard(this.billboard);
        line.setCustomYaw(this.customYaw);
        line.setCustomPitch(this.customPitch);
        line.setScale(this.scaleX, this.scaleY, this.scaleZ);
        line.setTranslation(this.translationX, this.translationY, this.translationZ);
        line.setShadowRadius(this.shadowRadius);
        line.setShadowStrength(this.shadowStrength);
        line.setGlowColor(this.glowColor);
        line.setChromaBackground(this.chromaBackground);
        line.setChromaGlow(this.chromaGlow);
        line.addFlags(this.flags.toArray(EnumFlag[]::new));
        for (Map.Entry<ClickType, List<Action>> entry : this.actions.entrySet()) {
            for (Action action : entry.getValue()) {
                line.addAction(entry.getKey(), action);
            }
        }
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
        synchronized (renderMutex) {
            // TEXT 行的渲染器由 PageTextRendererImpl 管理，此处只清理缓存
            if (type == HologramType.TEXT) {
                // 清理 previousRenderer（从其他类型变为TEXT时遗留的旧渲染器）
                if (previousRenderer != null) {
                    unregisterEntityIds(previousRenderer);
                    previousRenderer.destroy(getViewerPlayers());
                    WooHolograms.getInstance().getRendererPool().release(previousRenderer);
                    previousRenderer = null;
                }
                viewers.clear();
                playerTextCache.clear();
                lastTextCache.clear();
                return;
            }

            if (renderer != null) {
                unregisterEntityIds(renderer);
                List<Player> viewerPlayers = new ArrayList<>();
                for (UUID uuid : viewers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        viewerPlayers.add(player);
                    }
                }
                renderer.destroy(viewerPlayers);
                WooHolograms plugin = WooHolograms.getInstance();
                HologramRendererPool pool = plugin.getRendererPool();
                pool.release(renderer);
                renderer = null;
            }
            if (previousRenderer != null) {
                unregisterEntityIds(previousRenderer);
                List<Player> viewerPlayers = new ArrayList<>();
                for (UUID uuid : viewers) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        viewerPlayers.add(p);
                    }
                }
                previousRenderer.destroy(viewerPlayers);
                WooHolograms plugin = WooHolograms.getInstance();
                HologramRendererPool pool = plugin.getRendererPool();
                pool.release(previousRenderer);
                previousRenderer = null;
            }
            viewers.clear();
            playerTextCache.clear();
            lastTextCache.clear();
        }
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

            // TEXT 行位置由 PageTextRendererImpl 管理
            if (type == HologramType.TEXT) {
                return;
            }

            if (renderer != null && oldLocation != null && !oldLocation.equals(location)) {
                for (UUID uuid : viewers) {
                    Player viewer = Bukkit.getPlayer(uuid);
                    if (viewer != null && viewer.isOnline()) {
                        renderer.destroy(viewer);
                        renderer.render(viewer, this.location, this);
                    }
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

    public org.bukkit.Material getBlockMaterial() {
        return blockMaterial;
    }

    public double getHeight() {
        // TEXT 行使用 hologram 的 lineHeight（由 PageTextRendererImpl 统一管理合并渲染）
        if (type == HologramType.TEXT) {
            if (parent != null && parent.getParent() != null) {
                return parent.getParent().getLineHeight();
            }
            return DEFAULT_HEIGHT_TEXT;
        }
        return height;
    }

    public double getBaseHeight() {
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
        List<Action> list = actions.get(clickType);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
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

    public void setAction(ClickType clickType, int index, Action action) {
        List<Action> actionList = actions.get(clickType);
        if (actionList != null && index >= 0 && index < actionList.size()) {
            actionList.set(index, action);
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

    public Billboard getBillboard() {
        return billboard;
    }

    public void setBillboard(Billboard billboard) {
        this.billboard = billboard;
    }

    // ==================== Display Entity 属性方法 ====================

    /**
     * 获取 X 轴缩放（null 表示继承全息图级别值）
     */
    public Float getScaleX() {
        return scaleX;
    }

    /**
     * 获取 Y 轴缩放（null 表示继承全息图级别值）
     */
    public Float getScaleY() {
        return scaleY;
    }

    /**
     * 获取 Z 轴缩放（null 表示继承全息图级别值）
     */
    public Float getScaleZ() {
        return scaleZ;
    }

    /**
     * 设置缩放（null 表示继承全息图级别值）
     */
    public void setScale(Float x, Float y, Float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }

    /**
     * 获取 X 轴平移（null 表示继承全息图级别值）
     */
    public Double getTranslationX() {
        return translationX;
    }

    /**
     * 获取 Y 轴平移（null 表示继承全息图级别值）
     */
    public Double getTranslationY() {
        return translationY;
    }

    /**
     * 获取 Z 轴平移（null 表示继承全息图级别值）
     */
    public Double getTranslationZ() {
        return translationZ;
    }

    /**
     * 设置平移（null 表示继承全息图级别值）
     */
    public void setTranslation(Double x, Double y, Double z) {
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
    }

    /**
     * 获取阴影半径（null 表示继承全息图级别值）
     */
    public Float getShadowRadius() {
        return shadowRadius;
    }

    /**
     * 设置阴影半径（null 表示继承全息图级别值）
     */
    public void setShadowRadius(Float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }

    /**
     * 获取阴影强度（null 表示继承全息图级别值）
     */
    public Float getShadowStrength() {
        return shadowStrength;
    }

    /**
     * 设置阴影强度（null 表示继承全息图级别值）
     */
    public void setShadowStrength(Float shadowStrength) {
        this.shadowStrength = shadowStrength;
    }

    /**
     * 获取发光颜色覆盖（null 表示不覆盖）
     */
    public Integer getGlowColor() {
        return glowColor;
    }

    /**
     * 设置发光颜色覆盖（null 表示不覆盖）
     */
    public void setGlowColor(Integer glowColor) {
        this.glowColor = glowColor;
    }

    /**
     * 获取 Chroma 背景色状态（null 表示继承全息图级别值）
     */
    public Boolean getChromaBackground() {
        return chromaBackground;
    }

    /**
     * 获取有效的 Chroma 背景色状态（继承全息图级别值）
     */
    public boolean isChromaBackground() {
        if (chromaBackground != null) {
            return chromaBackground;
        }
        Hologram holo = getHologram();
        return holo != null && holo.isChromaBackground();
    }

    /**
     * 设置 Chroma 背景色（null 表示继承全息图级别值）
     */
    public void setChromaBackground(Boolean chromaBackground) {
        this.chromaBackground = chromaBackground;
    }

    /**
     * 获取 Chroma 发光色状态（null 表示继承全息图级别值）
     */
    public Boolean getChromaGlow() {
        return chromaGlow;
    }

    /**
     * 获取有效的 Chroma 发光色状态（继承全息图级别值）
     */
    public boolean isChromaGlow() {
        if (chromaGlow != null) {
            return chromaGlow;
        }
        Hologram holo = getHologram();
        return holo != null && holo.isChromaGlow();
    }

    /**
     * 设置 Chroma 发光色（null 表示继承全息图级别值）
     */
    public void setChromaGlow(Boolean chromaGlow) {
        this.chromaGlow = chromaGlow;
    }

    public int[] getEntityIds() {
        // TEXT 行的实体ID由 PageTextRendererImpl 管理
        if (type == HologramType.TEXT) {
            return EMPTY_IDS;
        }
        if (renderer == null) {
            return EMPTY_IDS;
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
