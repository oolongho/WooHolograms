package com.oolonghoo.holograms.hologram;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.api.event.HologramClickEvent;
import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import com.oolonghoo.holograms.storage.HologramStorage;
import com.oolonghoo.holograms.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 全息图核心类
 * 表示一个完整的全息图对象，包含多页、多行数据
 * 参考 DecentHolograms 的 Hologram 实现
 * 
 * @author oolongho
 */
public class Hologram {

    /*
     * 字段
     */

    // 基本信息
    private final String name;
    private Location location;
    private boolean enabled;
    private boolean saveToFile;

    // 显示设置
    private double displayRange;
    private double updateRange;
    private int updateInterval;
    private Billboard billboard = Billboard.CENTER;
    private float facing;
    private boolean downOrigin;
    private boolean doubleSided = false;
    
    // 全息图类型和显示属性
    private HologramType type;
    private boolean visible;
    private boolean persistent;
    private double lineHeight;

    // 权限
    private String permission;

    // 标志
    private final Set<EnumFlag> flags;

    // 页面
    private final List<HologramPage> pages;

    // 观看者
    private final Set<UUID> viewers;
    private final Map<UUID, Integer> viewerPages;

    // 显示/隐藏状态
    private final Set<UUID> hidePlayers;
    private final Set<UUID> showPlayers;
    private boolean defaultVisibleState;

    // 可点击实体渲染器
    private final List<NmsHologramRenderer> clickableRenderers;

    // 锁
    protected final ReentrantLock saveLock = new ReentrantLock();
    protected final Object visibilityMutex = new Object();

    // 存储器
    private HologramStorage storage;

    /*
     * 构造函数
     */

    /**
     * 创建全息图（保存到文件）
     * 
     * @param name 名称
     * @param location 位置
     */
    public Hologram(String name, Location location) {
        this(name, location, true);
    }

    /**
     * 创建全息图
     * 
     * @param name 名称
     * @param location 位置
     * @param saveToFile 是否保存到文件
     */
    public Hologram(String name, Location location, boolean saveToFile) {
        this.name = name;
        this.location = location != null ? location.clone() : null;
        this.saveToFile = saveToFile;
        this.enabled = true;
        
        WooHolograms plugin = WooHolograms.getInstance();
        if (plugin != null && plugin.getConfigManager() != null) {
            this.displayRange = plugin.getConfigManager().getDefaultDisplayRange();
            this.updateRange = plugin.getConfigManager().getDefaultUpdateRange();
            this.updateInterval = plugin.getConfigManager().getDefaultUpdateInterval();
            this.downOrigin = plugin.getConfigManager().isDefaultDownOrigin();
            this.lineHeight = plugin.getConfigManager().getDefaultLineHeight();
        } else {
            this.displayRange = 48.0;
            this.updateRange = 48.0;
            this.updateInterval = 3;
            this.downOrigin = true;
            this.lineHeight = 0.25;
        }
        
        this.facing = 0.0f;
        this.type = HologramType.TEXT;
        this.visible = true;
        this.persistent = saveToFile;
        this.permission = null;
        this.flags = ConcurrentHashMap.newKeySet();
        this.pages = new ArrayList<>();
        this.viewers = ConcurrentHashMap.newKeySet();
        this.viewerPages = new ConcurrentHashMap<>();
        this.hidePlayers = ConcurrentHashMap.newKeySet();
        this.showPlayers = ConcurrentHashMap.newKeySet();
        this.defaultVisibleState = true;
        this.clickableRenderers = new ArrayList<>();

        // 添加默认页
        addPage();
    }

    /*
     * 基本方法
     */

    /**
     * 获取名称
     * 
     * @return 名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取 ID（别名方法）
     * 
     * @return ID
     */
    public String getId() {
        return name;
    }

    /**
     * 获取位置
     * 
     * @return 位置
     */
    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    /**
     * 设置位置
     * 
     * @param location 新位置
     */
    public void setLocation(Location location) {
        this.location = location != null ? location.clone() : null;
        realignLines();
        teleportClickableEntitiesAll();
    }

    /**
     * 是否启用
     * 
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     * 
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 是否保存到文件
     * 
     * @return 是否保存到文件
     */
    public boolean isSaveToFile() {
        return saveToFile;
    }

    /**
     * 设置是否保存到文件
     * 
     * @param saveToFile 是否保存到文件
     */
    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }

    /*
     * 显示设置方法
     */

    /**
     * 获取显示范围
     * 
     * @return 显示范围
     */
    public double getDisplayRange() {
        return displayRange;
    }

    /**
     * 设置显示范围
     * 
     * @param displayRange 显示范围
     */
    public void setDisplayRange(double displayRange) {
        this.displayRange = displayRange;
    }

    /**
     * 获取更新范围
     * 
     * @return 更新范围
     */
    public double getUpdateRange() {
        return updateRange;
    }

    /**
     * 设置更新范围
     * 
     * @param updateRange 更新范围
     */
    public void setUpdateRange(double updateRange) {
        this.updateRange = updateRange;
    }

    /**
     * 获取更新间隔
     * 
     * @return 更新间隔（tick）
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * 设置更新间隔
     * 
     * @param updateInterval 更新间隔（tick）
     */
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    /**
     * 获取朝向
     * 
     * @return 朝向
     */
    public float getFacing() {
        return facing;
    }

    /**
     * 设置朝向
     * 
     * @param facing 朝向
     */
    public void setFacing(float facing) {
        this.facing = facing;
        refreshAllViewers();
    }

    /**
     * 获取 Billboard 模式
     * 
     * @return Billboard 模式
     */
    public Billboard getBillboard() {
        return billboard;
    }

    /**
     * 设置 Billboard 模式
     * 
     * @param billboard Billboard 模式
     */
    public void setBillboard(Billboard billboard) {
        this.billboard = billboard;
        refreshAllViewers();
    }

    /**
     * 是否双面显示
     * 
     * @return 是否双面显示
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    /**
     * 设置双面显示
     * 
     * @param doubleSided 是否双面显示
     */
    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        refreshAllViewers();
    }

    /**
     * 是否从下往上
     * 
     * @return 是否从下往上
     */
    public boolean isDownOrigin() {
        return downOrigin;
    }

    /**
     * 设置是否从下往上
     * 
     * @param downOrigin 是否从下往上
     */
    public void setDownOrigin(boolean downOrigin) {
        this.downOrigin = downOrigin;
        refreshAllViewers();
    }
    
    /**
     * 刷新所有观看者的全息图显示
     * 重新渲染所有行和可点击实体
     */
    public void refreshAllViewers() {
        synchronized (visibilityMutex) {
            if (!enabled) {
                return;
            }
            
            for (Player player : getViewerPlayers()) {
                int pageIndex = getPlayerPage(player);
                HologramPage page = getPage(pageIndex);
                if (page != null) {
                    for (HologramLine line : page.getLines()) {
                        line.hide(player);
                        line.show(player);
                    }
                }
            }
            
            hideClickableEntitiesAll();
            showClickableEntitiesAll();
        }
    }

    /*
     * 权限方法
     */

    /**
     * 获取权限
     * 
     * @return 权限
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置权限
     * 
     * @param permission 权限
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * 检查玩家是否有权限查看
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
        return hasPermission(player);
    }
    
    /*
     * 类型和显示属性方法
     */
    
    /**
     * 获取全息图类型
     * 
     * @return 全息图类型
     */
    public HologramType getType() {
        return type != null ? type : HologramType.TEXT;
    }
    
    /**
     * 设置全息图类型
     * 
     * @param type 全息图类型
     */
    public void setType(HologramType type) {
        this.type = type != null ? type : HologramType.TEXT;
    }
    
    /**
     * 是否可见
     * 
     * @return 是否可见
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * 设置可见性
     * 
     * @param visible 是否可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            showAll();
        } else {
            hideAll();
        }
    }
    
    /**
     * 是否持久化
     * 
     * @return 是否持久化
     */
    public boolean isPersistent() {
        return persistent;
    }
    
    /**
     * 设置持久化
     * 
     * @param persistent 是否持久化
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
    
    /**
     * 获取行高
     * 
     * @return 行高
     */
    public double getLineHeight() {
        return lineHeight;
    }
    
    /**
     * 设置行高
     * 
     * @param lineHeight 行高
     */
    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
        realignLines();
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
        return flags.contains(flag);
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

    /**
     * 获取所有标志
     * 
     * @return 标志集合
     */
    public Set<EnumFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /*
     * 页面方法
     */

    /**
     * 获取页数
     * 
     * @return 页数
     */
    public int size() {
        return pages.size();
    }

    /**
     * 获取页数（别名方法）
     * 
     * @return 页数
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * 获取所有页面
     * 
     * @return 页面列表
     */
    public List<HologramPage> getPages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * 获取指定页面
     * 
     * @param index 页索引
     * @return 页面，如果不存在返回 null
     */
    public HologramPage getPage(int index) {
        if (index < 0 || index >= pages.size()) {
            return null;
        }
        return pages.get(index);
    }

    /**
     * 获取玩家当前查看的页面
     * 
     * @param player 玩家
     * @return 页面
     */
    public HologramPage getPage(Player player) {
        if (isVisible(player)) {
            return getPage(getPlayerPage(player));
        }
        return null;
    }
    
    /**
     * 获取当前页面（默认第一页）
     * 
     * @return 当前页面
     */
    public HologramPage getCurrentPage() {
        return getPage(0);
    }

    /**
     * 添加新页面
     * 
     * @return 新添加的页面
     */
    public HologramPage addPage() {
        synchronized (visibilityMutex) {
            HologramPage page = new HologramPage(this, pages.size());
            pages.add(page);
            return page;
        }
    }

    /**
     * 添加新页面（带内容）
     * 
     * @param lines 行内容列表
     * @return 新添加的页面
     */
    public HologramPage addPage(List<String> lines) {
        synchronized (visibilityMutex) {
            HologramPage page = new HologramPage(this, pages.size());
            
            // 添加行
            if (lines != null) {
                for (String line : lines) {
                    if (line != null) {
                        page.addLine(line);
                    }
                }
            }
            
            pages.add(page);
            return page;
        }
    }

    /**
     * 在指定位置插入页面
     * 
     * @param index 索引
     * @return 新插入的页面
     */
    public HologramPage insertPage(int index) {
        synchronized (visibilityMutex) {
            // 1. 验证索引
            if (index < 0 || index > size()) {
                return null;
            }

            // 2. 创建新页面
            HologramPage page = new HologramPage(this, index);
            pages.add(index, page);

            // 3. 更新其他页面的索引
            for (int i = index + 1; i < pages.size(); i++) {
                pages.get(i).setIndex(i);
            }

            // 4. 更新观看者的页码
            viewerPages.replaceAll((uuid, pageIndex) -> {
                if (pageIndex >= index) {
                    return pageIndex + 1;
                }
                return pageIndex;
            });

            return page;
        }
    }

    /**
     * 在指定位置插入页面（带内容）
     * 
     * @param index 索引
     * @param lines 行内容列表
     * @return 新插入的页面
     */
    public HologramPage insertPage(int index, List<String> lines) {
        synchronized (visibilityMutex) {
            // 1. 验证索引
            if (index < 0 || index > size()) {
                return null;
            }

            // 2. 创建新页面
            HologramPage page = new HologramPage(this, index);
            
            // 3. 添加行
            if (lines != null) {
                for (String line : lines) {
                    if (line != null) {
                        page.addLine(line);
                    }
                }
            }
            
            pages.add(index, page);

            // 4. 更新其他页面的索引
            for (int i = index + 1; i < pages.size(); i++) {
                pages.get(i).setIndex(i);
            }

            // 5. 更新观看者的页码
            viewerPages.replaceAll((uuid, pageIndex) -> {
                if (pageIndex >= index) {
                    return pageIndex + 1;
                }
                return pageIndex;
            });

            return page;
        }
    }

    /**
     * 移除页面
     * 
     * @param index 页索引
     * @return 被移除的页面
     */
    public HologramPage removePage(int index) {
        synchronized (visibilityMutex) {
            // 1. 验证索引
            if (index < 0 || index >= size()) {
                return null;
            }

            // 2. 移除页面
            HologramPage page = pages.remove(index);
            if (page == null) {
                return null;
            }

            // 3. 销毁页面
            page.destroy();

            // 4. 更新其他页面的索引
            for (int i = 0; i < pages.size(); i++) {
                pages.get(i).setIndex(i);
            }

            // 5. 更新观看者的页码
            if (!pages.isEmpty()) {
                for (Map.Entry<UUID, Integer> entry : viewerPages.entrySet()) {
                    UUID uuid = entry.getKey();
                    int currentPage = entry.getValue();
                    Player player = Bukkit.getPlayer(uuid);

                    if (currentPage == index) {
                        // 当前观看的页面被删除，切换到第一页
                        if (player != null && player.isOnline()) {
                            show(player, 0);
                        } else {
                            viewerPages.put(uuid, 0);
                        }
                    } else if (currentPage > index) {
                        // 当前观看的页面索引需要减一
                        viewerPages.put(uuid, currentPage - 1);
                    }
                }
            } else {
                // 没有页面了，清空观看者
                viewerPages.clear();
                viewers.clear();
            }

            return page;
        }
    }

    /**
     * 清空所有页面
     */
    public void clearPages() {
        synchronized (visibilityMutex) {
            // 1. 隐藏所有观看者
            hideAll();

            // 2. 销毁所有页面
            for (HologramPage page : pages) {
                page.destroy();
            }
            pages.clear();

            // 3. 清空观看者状态
            viewerPages.clear();
            viewers.clear();
        }
    }

    /**
     * 交换两个页面
     * 
     * @param index1 第一页索引
     * @param index2 第二页索引
     * @return 是否成功
     */
    public boolean swapPages(int index1, int index2) {
        synchronized (visibilityMutex) {
            // 1. 验证索引
            if (index1 == index2 || index1 < 0 || index1 >= size() || index2 < 0 || index2 >= size()) {
                return false;
            }

            // 2. 交换页面
            Collections.swap(pages, index1, index2);

            // 3. 交换索引
            HologramPage page1 = getPage(index1);
            HologramPage page2 = getPage(index2);
            int temp = page1.getIndex();
            page1.setIndex(page2.getIndex());
            page2.setIndex(temp);

            // 4. 交换观看者
            Set<Player> viewers1 = getViewerPlayers(index1);
            Set<Player> viewers2 = getViewerPlayers(index2);
            viewers1.forEach(player -> show(player, index2));
            viewers2.forEach(player -> show(player, index1));

            return true;
        }
    }

    /*
     * 页面切换方法
     */

    /**
     * 切换到下一页
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean nextPage(Player player) {
        synchronized (visibilityMutex) {
            int currentPage = getPlayerPage(player);
            int nextPageIndex = currentPage + 1;
            
            // 检查是否有下一页
            if (nextPageIndex >= size()) {
                // 如果只有一页或已经是最后一页，则返回第一页（循环）
                if (size() <= 1) {
                    return false;
                }
                nextPageIndex = 0;
            }
            
            return show(player, nextPageIndex);
        }
    }

    /**
     * 切换到上一页
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean previousPage(Player player) {
        synchronized (visibilityMutex) {
            int currentPage = getPlayerPage(player);
            int prevPageIndex = currentPage - 1;
            
            // 检查是否有上一页
            if (prevPageIndex < 0) {
                // 如果只有一页或已经是第一页，则返回最后一页（循环）
                if (size() <= 1) {
                    return false;
                }
                prevPageIndex = size() - 1;
            }
            
            return show(player, prevPageIndex);
        }
    }

    /**
     * 切换到指定页面
     * 
     * @param player 玩家
     * @param pageIndex 页索引
     * @return 是否成功
     */
    public boolean switchPage(Player player, int pageIndex) {
        synchronized (visibilityMutex) {
            // 验证页面索引
            if (pageIndex < 0 || pageIndex >= size()) {
                return false;
            }
            
            // 如果已经是当前页，则不需要切换
            if (getPlayerPage(player) == pageIndex) {
                return true;
            }
            
            return show(player, pageIndex);
        }
    }

    /**
     * 切换到第一页
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean firstPage(Player player) {
        return switchPage(player, 0);
    }

    /**
     * 切换到最后一页
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean lastPage(Player player) {
        if (size() == 0) {
            return false;
        }
        return switchPage(player, size() - 1);
    }

    /**
     * 检查是否有下一页
     * 
     * @param player 玩家
     * @return 是否有下一页
     */
    public boolean hasNextPage(Player player) {
        int currentPage = getPlayerPage(player);
        return currentPage < size() - 1;
    }

    /**
     * 检查是否有上一页
     * 
     * @param player 玩家
     * @return 是否有上一页
     */
    public boolean hasPreviousPage(Player player) {
        int currentPage = getPlayerPage(player);
        return currentPage > 0;
    }

    /**
     * 获取总页数
     * 
     * @return 总页数
     */
    public int getTotalPages() {
        return size();
    }

    /*
     * 显示/隐藏方法
     */

    /**
     * 显示给玩家（使用玩家当前页码）
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean show(Player player) {
        return show(player, getPlayerPage(player));
    }

    /**
     * 显示给玩家
     * 
     * @param player 玩家
     * @param pageIndex 页索引
     * @return 是否成功
     */
    public boolean show(Player player, int pageIndex) {
        synchronized (visibilityMutex) {
            if (!enabled) {
                return false;
            }

            if (isHideState(player)) {
                return false;
            }

            if (!defaultVisibleState && !isShowState(player)) {
                return false;
            }

            HologramPage page = getPage(pageIndex);
            if (page == null || page.size() == 0) {
                return false;
            }

            if (!canShow(player)) {
                return false;
            }

            if (!isInDisplayRange(player)) {
                return false;
            }

            HologramPage currentPage = getPage(player);
            if (currentPage != null && currentPage != page) {
                hidePageFrom(player, currentPage);
            }

            showPageTo(player, page, pageIndex);
            return true;
        }
    }

    /**
     * 显示页面给玩家
     */
    private void showPageTo(Player player, HologramPage page, int pageIndex) {
        page.getLines().forEach(line -> line.show(player));
        viewerPages.put(player.getUniqueId(), pageIndex);
        viewers.add(player.getUniqueId());
        showClickableEntities(player);
    }

    /**
     * 从玩家隐藏页面
     */
    private void hidePageFrom(Player player, HologramPage page) {
        page.getLines().forEach(line -> line.hide(player));
        hideClickableEntities(player);
    }

    /**
     * 显示给所有玩家
     */
    public void showAll() {
        synchronized (visibilityMutex) {
            if (enabled) {
                Bukkit.getOnlinePlayers().forEach(player -> show(player, getPlayerPage(player)));
            }
        }
    }

    /**
     * 从玩家隐藏
     * 
     * @param player 玩家
     */
    public void hide(Player player) {
        synchronized (visibilityMutex) {
            // 1. 检查玩家是否正在查看
            if (!isVisible(player)) {
                return;
            }

            // 2. 获取当前页面
            HologramPage page = getPage(player);
            if (page != null) {
                // 3. 调用页面的 hide 方法
                hidePageFrom(player, page);
            }

            // 4. 从观看者列表移除
            viewers.remove(player.getUniqueId());

            // 5. 清理玩家页面状态
            viewerPages.remove(player.getUniqueId());
        }
    }

    /**
     * 从玩家隐藏（保留页面状态）
     * 
     * @param player 玩家
     * @param keepPageState 是否保留页面状态
     */
    public void hide(Player player, boolean keepPageState) {
        synchronized (visibilityMutex) {
            // 1. 检查玩家是否正在查看
            if (!isVisible(player)) {
                return;
            }

            // 2. 获取当前页面
            HologramPage page = getPage(player);
            if (page != null) {
                // 3. 调用页面的 hide 方法
                hidePageFrom(player, page);
            }

            // 4. 从观看者列表移除
            viewers.remove(player.getUniqueId());

            // 5. 根据参数决定是否清理页面状态
            if (!keepPageState) {
                viewerPages.remove(player.getUniqueId());
            }
        }
    }

    /**
     * 从所有玩家隐藏
     */
    public void hideAll() {
        synchronized (visibilityMutex) {
            if (enabled) {
                getViewerPlayers().forEach(this::hide);
            }
        }
    }

    /*
     * 更新方法
     */

    /**
     * 更新
     * 
     * @param player 玩家
     */
    public void update(Player player) {
        update(false, player);
    }

    /**
     * 更新
     * 
     * @param force 是否强制更新
     * @param player 玩家
     */
    public void update(boolean force, Player player) {
        synchronized (visibilityMutex) {
            // 1. 检查是否禁用更新
            if (hasFlag(EnumFlag.DISABLE_UPDATING)) {
                return;
            }

            // 2. 执行更新
            performUpdate(force, player);
        }
    }

    /**
     * 更新所有
     */
    public void updateAll() {
        updateAll(false);
    }

    /**
     * 更新所有
     * 
     * @param force 是否强制更新
     */
    public void updateAll(boolean force) {
        synchronized (visibilityMutex) {
            // 1. 检查是否启用
            if (!enabled) {
                return;
            }

            // 2. 检查是否禁用更新
            if (hasFlag(EnumFlag.DISABLE_UPDATING)) {
                return;
            }

            // 3. 更新所有观看者
            getViewerPlayers().forEach(player -> performUpdate(force, player));
        }
    }

    /**
     * 执行更新
     * 
     * @param force 是否强制更新
     * @param player 玩家
     */
    private void performUpdate(boolean force, Player player) {
        // 1. 检查玩家是否正在查看
        if (!isVisible(player)) {
            return;
        }

        // 2. 检查玩家是否在更新范围内
        if (!isInUpdateRange(player)) {
            return;
        }

        // 3. 检查玩家是否在隐藏列表中
        if (isHideState(player)) {
            return;
        }

        // 4. 获取当前页面
        HologramPage page = getPage(player);
        if (page == null) {
            return;
        }

        // 5. 更新文本内容
        for (HologramLine line : page.getLines()) {
            line.update(force, player);
        }
    }

    /**
     * 更新动画
     * 
     * @param player 玩家
     */
    public void updateAnimations(Player player) {
        synchronized (visibilityMutex) {
            // 1. 检查是否禁用动画
            if (hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
                return;
            }

            // 2. 执行动画更新
            performUpdateAnimations(player);
        }
    }
    
    /**
     * 更新文本内容
     */
    public void updateText() {
        synchronized (visibilityMutex) {
            for (HologramPage page : pages) {
                for (HologramLine line : page.getLines()) {
                    line.update(true);
                }
            }
        }
    }
    
    /**
     * 执行动作
     * 
     * @param player 玩家
     * @param clickType 点击类型
     */
    public void executeActions(Player player, ClickType clickType) {
        HologramPage page = getPage(player);
        if (page != null) {
            page.executeActions(player, clickType);
        }
    }

    /**
     * 更新所有动画
     */
    public void updateAnimationsAll() {
        synchronized (visibilityMutex) {
            // 1. 检查是否启用
            if (!enabled) {
                return;
            }

            // 2. 检查是否禁用动画
            if (hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
                return;
            }

            // 3. 更新所有观看者的动画
            getViewerPlayers().forEach(this::performUpdateAnimations);
        }
    }

    /**
     * 执行动画更新
     * 
     * @param player 玩家
     */
    private void performUpdateAnimations(Player player) {
        // 1. 检查玩家是否正在查看
        if (!isVisible(player)) {
            return;
        }

        // 2. 检查玩家是否在更新范围内
        if (!isInUpdateRange(player)) {
            return;
        }

        // 3. 检查玩家是否在隐藏列表中
        if (isHideState(player)) {
            return;
        }

        // 4. 获取当前页面
        HologramPage page = getPage(player);
        if (page == null) {
            return;
        }

        // 5. 更新动画
        for (HologramLine line : page.getLines()) {
            line.updateAnimations(player);
        }
    }

    /**
     * 重新对齐所有行
     */
    public void realignLines() {
        for (HologramPage page : pages) {
            page.realignLines();
        }
    }

    /*
     * 传送方法
     */

    /**
     * 传送全息图到新位置
     * 
     * @param location 新位置
     */
    public void teleport(Location location) {
        teleport(location, true);
    }

    /**
     * 传送全息图到新位置
     * 
     * @param location 新位置
     * @param updateViewers 是否更新观看者
     */
    public void teleport(Location location, boolean updateViewers) {
        synchronized (visibilityMutex) {
            // 1. 更新位置
            this.location = location != null ? location.clone() : null;

            // 2. 重新对齐所有行
            realignLines();

            // 3. 更新观看者
            if (updateViewers && enabled) {
                // 传送可点击实体
                teleportClickableEntitiesAll();

                // 更新所有行的位置
                for (HologramPage page : pages) {
                    for (HologramLine line : page.getLines()) {
                        line.updateLocation(true);
                    }
                }
            }
        }
    }

    /**
     * 传送全息图到新位置（仅对指定玩家）
     * 
     * @param location 新位置
     * @param player 玩家
     */
    public void teleport(Location location, Player player) {
        synchronized (visibilityMutex) {
            // 1. 检查玩家是否正在查看
            if (!isVisible(player)) {
                return;
            }

            // 2. 获取当前页面
            HologramPage page = getPage(player);
            if (page == null) {
                return;
            }

            // 3. 传送可点击实体
            teleportClickableEntities(player);

            // 4. 更新行的位置
            for (HologramLine line : page.getLines()) {
                line.updateLocation(true, player);
            }
        }
    }

    /**
     * 移动全息图（相对偏移）
     * 
     * @param dx X 轴偏移
     * @param dy Y 轴偏移
     * @param dz Z 轴偏移
     */
    public void move(double dx, double dy, double dz) {
        if (location == null) {
            return;
        }

        Location newLocation = location.clone().add(dx, dy, dz);
        teleport(newLocation);
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
     * 获取玩家当前页码
     * 
     * @param player 玩家
     * @return 页码
     */
    public int getPlayerPage(Player player) {
        return viewerPages.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * 获取所有观看者
     * 
     * @return 观看者 UUID 集合
     */
    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
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

    /**
     * 获取指定页面的观看者
     * 
     * @param pageIndex 页索引
     * @return 观看者玩家集合
     */
    public Set<Player> getViewerPlayers(int pageIndex) {
        Set<Player> players = new HashSet<>();
        viewerPages.forEach((uuid, page) -> {
            if (page == pageIndex) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    players.add(player);
                }
            }
        });
        return players;
    }

    /**
     * 设置玩家页面状态
     * 
     * @param player 玩家
     * @param pageIndex 页索引
     */
    public void setPlayerPage(Player player, int pageIndex) {
        viewerPages.put(player.getUniqueId(), pageIndex);
    }

    /**
     * 设置玩家页面状态（批量）
     * 
     * @param pageIndex 页索引
     */
    public void setPlayerPages(int pageIndex) {
        synchronized (visibilityMutex) {
            for (UUID uuid : viewerPages.keySet()) {
                viewerPages.put(uuid, pageIndex);
            }
        }
    }

    /**
     * 清除玩家页面状态
     * 
     * @param player 玩家
     */
    public void clearPlayerPage(Player player) {
        viewerPages.remove(player.getUniqueId());
    }

    /**
     * 批量设置玩家页面状态
     * 
     * @param playerStates 玩家页面状态映射（UUID -> 页码)
     */
    public void setPlayerPages(Map<UUID, Integer> playerStates) {
        synchronized (visibilityMutex) {
            viewerPages.clear();
            viewerPages.putAll(playerStates);
        }
    }

    /**
     * 获取所有玩家页面状态
     * 
     * @return 玩家页面状态映射
     */
    public Map<UUID, Integer> getAllPlayerPageStates() {
        return Collections.unmodifiableMap(viewerPages);
    }

    /**
     * 清除所有玩家页面状态
     */
    public void clearAllPlayerPageStates() {
        viewerPages.clear();
    }

    /*
     * 显示/隐藏状态方法
     */

    /**
     * 获取默认可见状态
     * 
     * @return 默认可见状态
     */
    public boolean isDefaultVisibleState() {
        return defaultVisibleState;
    }

    /**
     * 设置默认可见状态
     * 
     * @param defaultVisibleState 默认可见状态
     */
    public void setDefaultVisibleState(boolean defaultVisibleState) {
        this.defaultVisibleState = defaultVisibleState;
    }

    /**
     * 设置玩家隐藏状态
     * 
     * @param player 玩家
     */
    public void setHidePlayer(Player player) {
        hidePlayers.add(player.getUniqueId());
    }

    /**
     * 移除玩家隐藏状态
     * 
     * @param player 玩家
     */
    public void removeHidePlayer(Player player) {
        hidePlayers.remove(player.getUniqueId());
    }

    /**
     * 检查玩家是否隐藏
     * 
     * @param player 玩家
     * @return 是否隐藏
     */
    public boolean isHideState(Player player) {
        return hidePlayers.contains(player.getUniqueId());
    }

    /**
     * 设置玩家显示状态
     * 
     * @param player 玩家
     */
    public void setShowPlayer(Player player) {
        showPlayers.add(player.getUniqueId());
    }

    /**
     * 移除玩家显示状态
     * 
     * @param player 玩家
     */
    public void removeShowPlayer(Player player) {
        showPlayers.remove(player.getUniqueId());
    }

    /**
     * 检查玩家是否显示
     * 
     * @param player 玩家
     * @return 是否显示
     */
    public boolean isShowState(Player player) {
        return showPlayers.contains(player.getUniqueId());
    }

    /*
     * 范围检查方法
     */

    /**
     * 检查玩家是否在显示范围内
     * 
     * @param player 玩家
     * @return 是否在范围内
     */
    public boolean isInDisplayRange(Player player) {
        return isInRange(player, displayRange);
    }

    /**
     * 检查玩家是否在更新范围内
     * 
     * @param player 玩家
     * @return 是否在范围内
     */
    public boolean isInUpdateRange(Player player) {
        return isInRange(player, updateRange);
    }

    /**
     * 检查玩家是否在范围内
     */
    private boolean isInRange(Player player, double range) {
        try {
            if (player.getWorld().equals(location.getWorld())) {
                return player.getLocation().distanceSquared(location) <= range * range;
            }
        } catch (Exception e) {
            if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                WooHolograms.getInstance().getLogger().warning("isInRange check failed: " + e.getMessage());
            }
        }
        return false;
    }

    /*
     * 可点击实体方法
     */

    /**
     * 显示可点击实体
     * 
     * @param player 玩家
     */
    public void showClickableEntities(Player player) {
        HologramPage page = getPage(player);
        if (page == null || !page.isClickable()) {
            return;
        }

        int amount = (int) (page.getHeight() / 2) + 1;
        Location loc = location.clone();
        loc.setY((int) (loc.getY() - (downOrigin ? 0 : page.getHeight())) + 0.5);

        for (int i = 0; i < amount; i++) {
            NmsHologramRenderer renderer = getClickableRenderer(i);
            // renderer.display(player, loc);
            loc.add(0, 1.8, 0);
        }
    }

    /**
     * 显示所有可点击实体
     */
    public void showClickableEntitiesAll() {
        if (enabled) {
            getViewerPlayers().forEach(this::showClickableEntities);
        }
    }

    /**
     * 隐藏可点击实体
     * 
     * @param player 玩家
     */
    public void hideClickableEntities(Player player) {
        HologramPage page = getPage(player);
        if (page == null) {
            return;
        }

        for (NmsHologramRenderer renderer : clickableRenderers) {
            renderer.destroy(player);
        }
    }

    /**
     * 隐藏所有可点击实体
     */
    public void hideClickableEntitiesAll() {
        if (enabled) {
            getViewerPlayers().forEach(this::hideClickableEntities);
        }
    }

    /**
     * 传送可点击实体
     * 
     * @param player 玩家
     */
    public void teleportClickableEntities(Player player) {
        HologramPage page = getPage(player);
        if (page == null || !page.isClickable()) {
            return;
        }

        int amount = (int) (page.getHeight() / 2) + 1;
        Location loc = location.clone();
        loc.setY((int) (loc.getY() - (downOrigin ? 0 : page.getHeight())) + 0.5);

        for (int i = 0; i < amount; i++) {
            NmsHologramRenderer renderer = getClickableRenderer(i);
            // renderer.move(player, loc);
            loc.add(0, 1.8, 0);
        }
    }

    /**
     * 传送所有可点击实体
     */
    public void teleportClickableEntitiesAll() {
        if (enabled) {
            getViewerPlayers().forEach(this::teleportClickableEntities);
        }
    }

    /**
     * 获取可点击渲染器
     * 
     * @param index 索引
     * @return 渲染器
     */
    public NmsHologramRenderer getClickableRenderer(int index) {
        if (index >= clickableRenderers.size()) {
            NmsHologramRenderer renderer = WooHolograms.getInstance()
                    .getRendererFactory()
                    .createClickableRenderer();
            clickableRenderers.add(renderer);
        }
        return clickableRenderers.get(index);
    }

    /*
     * 点击处理方法
     */

    /**
     * 处理点击
     * 
     * @param player 玩家
     * @param entityId 实体 ID
     * @param clickType 点击类型
     * @return 是否处理成功
     */
    public boolean onClick(Player player, int entityId, ClickType clickType) {
        // 1. 检查全息图是否启用
        if (!enabled) {
            return false;
        }

        // 2. 检查玩家是否正在查看
        if (!isVisible(player)) {
            return false;
        }

        // 3. 获取玩家当前页面
        HologramPage page = getPage(player);
        if (page == null) {
            return false;
        }

        // 4. 检查 entityId 是否属于此全息图
        if (!page.hasEntity(entityId)) {
            return false;
        }

        // 5. 触发 HologramClickEvent 事件
        HologramClickEvent event = new HologramClickEvent(this, page, player, clickType, entityId);
        Bukkit.getPluginManager().callEvent(event);

        // 6. 检查事件是否被取消
        if (event.isCancelled()) {
            return false;
        }

        // 7. 检查是否禁用动作
        if (hasFlag(EnumFlag.DISABLE_ACTIONS)) {
            return true;
        }

        // 8. 执行动作
        page.executeActions(player, clickType);

        return true;
    }

    /**
     * 处理点击（带行索引）
     * 
     * @param player 玩家
     * @param entityId 实体 ID
     * @param clickType 点击类型
     * @param lineIndex 行索引（-1 表示点击的是可点击实体而非行）
     * @return 是否处理成功
     */
    public boolean onClick(Player player, int entityId, ClickType clickType, int lineIndex) {
        // 1. 检查全息图是否启用
        if (!enabled) {
            return false;
        }

        // 2. 检查玩家是否正在查看
        if (!isVisible(player)) {
            return false;
        }

        // 3. 获取玩家当前页面
        HologramPage page = getPage(player);
        if (page == null) {
            return false;
        }

        // 4. 检查 entityId 是否属于此全息图
        if (!page.hasEntity(entityId)) {
            return false;
        }

        // 5. 触发 HologramClickEvent 事件
        HologramClickEvent event = new HologramClickEvent(this, page, player, clickType, entityId);
        Bukkit.getPluginManager().callEvent(event);

        // 6. 检查事件是否被取消
        if (event.isCancelled()) {
            return false;
        }

        // 7. 检查是否禁用动作
        if (hasFlag(EnumFlag.DISABLE_ACTIONS)) {
            return true;
        }

        // 8. 执行动作
        page.executeActions(player, clickType);

        return true;
    }

    /**
     * 检查实体 ID 是否属于此全息图
     * 
     * @param entityId 实体 ID
     * @return 是否属于此全息图
     */
    public boolean hasEntity(int entityId) {
        // 检查所有页面
        for (HologramPage page : pages) {
            if (page.hasEntity(entityId)) {
                return true;
            }
        }

        // 检查可点击实体
        for (NmsHologramRenderer renderer : clickableRenderers) {
            if (renderer.getEntityIds().contains(entityId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取实体 ID 所属的页面
     * 
     * @param entityId 实体 ID
     * @return 页面，如果不存在返回 null
     */
    public HologramPage getPageByEntityId(int entityId) {
        for (HologramPage page : pages) {
            if (page.hasEntity(entityId)) {
                return page;
            }
        }
        return null;
    }

    /**
     * 处理玩家退出
     * 
     * @param player 玩家
     */
    public void onQuit(Player player) {
        hide(player);
        removeShowPlayer(player);
        removeHidePlayer(player);
        viewerPages.remove(player.getUniqueId());
    }

    /*
     * 存储方法
     */

    /**
     * 设置存储器
     * 
     * @param storage 存储器
     */
    public void setStorage(HologramStorage storage) {
        this.storage = storage;
    }

    /**
     * 保存
     */
    public void save() {
        if (!saveToFile || storage == null) {
            return;
        }

        storage.save(this);
    }

    /**
     * 删除
     */
    public void delete() {
        destroy();

        if (storage != null) {
            storage.delete(name);
        }
    }

    /**
     * 启用
     */
    public void enable() {
        synchronized (visibilityMutex) {
            enabled = true;
            showAll();
        }
    }

    /**
     * 禁用
     */
    public void disable() {
        synchronized (visibilityMutex) {
            hideAll();
            enabled = false;
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        disable();
        viewerPages.clear();

        for (HologramPage page : pages) {
            page.destroy();
        }
        pages.clear();

        for (NmsHologramRenderer renderer : clickableRenderers) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                renderer.destroy(player);
            }
        }
        clickableRenderers.clear();
    }

    /*
     * 克隆方法
     */

    /**
     * 克隆全息图
     * 
     * @param name 新名称
     * @param location 新位置
     * @param temp 是否为临时全息图
     * @return 克隆的全息图
     */
    public Hologram clone(String name, Location location, boolean temp) {
        Hologram hologram = new Hologram(name, location, !temp);
        hologram.setDownOrigin(this.downOrigin);
        hologram.setPermission(this.permission);
        hologram.setFacing(this.facing);
        hologram.setDisplayRange(this.displayRange);
        hologram.setUpdateRange(this.updateRange);
        hologram.setUpdateInterval(this.updateInterval);
        hologram.addFlags(this.flags.toArray(new EnumFlag[0]));
        hologram.setDefaultVisibleState(this.defaultVisibleState);
        hologram.showPlayers.addAll(this.showPlayers);
        hologram.hidePlayers.addAll(this.hidePlayers);

        for (int i = 0; i < size(); i++) {
            HologramPage page = getPage(i);
            HologramPage clonePage = page.clone(hologram, i);
            if (hologram.pages.size() > i) {
                hologram.pages.set(i, clonePage);
            } else {
                hologram.pages.add(clonePage);
            }
        }

        return hologram;
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
        map.put("location", LocationUtil.toString(location));
        map.put("enabled", enabled);
        map.put("permission", permission == null || permission.isEmpty() ? null : permission);
        map.put("flags", flags.isEmpty() ? null : flags.stream().map(EnumFlag::name).collect(Collectors.toList()));
        map.put("display-range", displayRange);
        map.put("update-range", updateRange);
        map.put("update-interval", updateInterval);
        map.put("billboard", billboard != Billboard.CENTER ? billboard.getId() : null);
        map.put("facing", facing);
        map.put("down-origin", downOrigin);
        map.put("double-sided", doubleSided);
        map.put("pages", pages.stream().map(HologramPage::serializeToMap).collect(Collectors.toList()));
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hologram hologram = (Hologram) o;
        return Objects.equals(name, hologram.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Hologram{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", pages=" + pages.size() +
                ", viewers=" + viewers.size() +
                '}';
    }

    /**
     * 显示给附近玩家
     */
    public void showToNearby() {
        if (!enabled) {
            return;
        }

        Location loc = getLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        double displayRange = getDisplayRange();
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= displayRange * displayRange) {
                show(player, 0);
            }
        }
    }

    /**
     * 从所有玩家隐藏
     */
    public void hideFromAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
    }
}
