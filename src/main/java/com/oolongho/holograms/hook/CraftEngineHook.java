package com.oolongho.holograms.hook;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * CraftEngine 钩子
 *
 * <p>负责检测 CraftEngine（CE）是否在线，并提供 CE 自定义物品/方块的解析能力。
 * 所有 CE 类引用封闭在本类内部，调用方必须先以 {@link #isReady()} 守卫，
 * 确保 CE 离线时不会触发 CE 类加载（避免 NoClassDefFoundError）。</p>
 *
 * <p>ID 格式：{@code namespace:path}（如 {@code simmarket:cash_register}）。
 * CE 离线或 ID 未注册时解析方法返回 null，由调用方走原版兜底逻辑。</p>
 *
 * @author oolongho
 */
public class CraftEngineHook {

    private static final String CE_PLUGIN_NAME = "CraftEngine";

    private volatile boolean ready = false;

    /**
     * 检测 CraftEngine 是否在线并记录日志。
     *
     * @return 在线返回 true，离线返回 false
     */
    public boolean init() {
        ready = Bukkit.getPluginManager().getPlugin(CE_PLUGIN_NAME) != null;
        return ready;
    }

    /**
     * CraftEngine 是否可用。
     *
     * @return 可用返回 true
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 解析 CE 自定义物品。
     *
     * <p>路径：{@link CraftEngineItems#byId(Key)} → buildBukkitItem()。</p>
     *
     * @param id CE 物品 ID（namespace:path）
     * @return 物品；CE 离线、ID 未注册或构建失败返回 null
     */
    @Nullable
    public ItemStack resolveItem(String id) {
        if (!ready || id == null || id.isEmpty()) {
            return null;
        }
        try {
            var definition = CraftEngineItems.byId(Key.of(id));
            return definition != null ? definition.buildBukkitItem() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 CE 自定义方块的 Bukkit BlockData。
     *
     * <p>路径：{@link CraftEngineBlocks#byId(Key)} → defaultState() →
     * {@link CraftEngineBlocks#getBukkitBlockData}。</p>
     *
     * @param id CE 方块 ID（namespace:path）
     * @return BlockData；CE 离线、ID 未注册或解析失败返回 null
     */
    @Nullable
    public BlockData resolveBlockData(String id) {
        if (!ready || id == null || id.isEmpty()) {
            return null;
        }
        try {
            var definition = CraftEngineBlocks.byId(Key.of(id));
            if (definition == null) {
                return null;
            }
            var state = definition.defaultState();
            return state != null ? CraftEngineBlocks.getBukkitBlockData(state) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 列出 CE 已注册的自定义物品 ID（用于 Tab 补全）。
     *
     * @return ID 列表；CE 离线返回空列表
     */
    public List<String> listItemIds() {
        if (!ready) {
            return List.of();
        }
        try {
            List<String> ids = new ArrayList<>();
            for (Key key : CraftEngineItems.loadedItems().keySet()) {
                ids.add(key.asString());
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 列出 CE 已注册的自定义方块 ID（用于 Tab 补全）。
     *
     * @return ID 列表；CE 离线返回空列表
     */
    public List<String> listBlockIds() {
        if (!ready) {
            return List.of();
        }
        try {
            List<String> ids = new ArrayList<>();
            for (Key key : CraftEngineBlocks.loadedBlocks().keySet()) {
                ids.add(key.asString());
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }
}
