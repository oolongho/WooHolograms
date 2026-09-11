package com.oolongho.holograms.hook;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CraftEngine 钩子（纯反射实现）
 *
 * <p>负责检测 CraftEngine（CE）是否在线，并提供 CE 自定义物品/方块的解析能力。
 * 全部 CE 类通过反射访问，编译期不依赖 CE jar：
 * 既避免 CE 离线时触发 CE 类加载（NoClassDefFoundError），
 * 也让本插件源码仓库在没有 CE jar 的环境下可直接构建。</p>
 *
 * <p>ID 格式：{@code namespace:path}（如 {@code simmarket:cash_register}）。
 * CE 离线、反射解析失败或 ID 未注册时，解析方法返回 null，由调用方走原版兜底逻辑。</p>
 *
 * @author oolongho
 */
public class CraftEngineHook {

    private static final String CE_PLUGIN_NAME = "CraftEngine";

    private static final String CLASS_KEY = "net.momirealms.craftengine.core.util.Key";
    private static final String CLASS_ITEMS_API = "net.momirealms.craftengine.bukkit.api.CraftEngineItems";
    private static final String CLASS_BLOCKS_API = "net.momirealms.craftengine.bukkit.api.CraftEngineBlocks";

    private volatile boolean ready = false;

    // 反射成员缓存（init 成功后一次性解析）
    private Method keyOf;
    private Method itemsById;
    private Method blocksById;
    private Method itemsLoadedItems;
    private Method blocksLoadedBlocks;
    private Method getBukkitBlockData;
    // 按定义对象的具体类缓存实例方法（CE 内部类型不进编译期签名）
    private final Map<Class<?>, Method> buildItemCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> defaultStateCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> keyAsStringCache = new ConcurrentHashMap<>();

    /**
     * 检测 CraftEngine 是否在线，并预解析反射成员。
     *
     * @return 在线且反射解析成功返回 true，否则 false
     */
    public boolean init() {
        ready = false;
        if (Bukkit.getPluginManager().getPlugin(CE_PLUGIN_NAME) == null) {
            return false;
        }
        try {
            Class<?> keyClass = Class.forName(CLASS_KEY);
            keyOf = keyClass.getMethod("of", String.class);
            Class<?> itemsClass = Class.forName(CLASS_ITEMS_API);
            Class<?> blocksClass = Class.forName(CLASS_BLOCKS_API);
            itemsById = itemsClass.getMethod("byId", keyClass);
            blocksById = blocksClass.getMethod("byId", keyClass);
            itemsLoadedItems = findStatic(itemsClass, "loadedItems", 0, Map.class);
            blocksLoadedBlocks = findStatic(blocksClass, "loadedBlocks", 0, Map.class);
            // getBukkitBlockData 的参数是 CE 内部 BlockState 类型，按方法名+参数个数+返回类型匹配
            getBukkitBlockData = findStatic(blocksClass, "getBukkitBlockData", 1, BlockData.class);
            ready = itemsLoadedItems != null && blocksLoadedBlocks != null && getBukkitBlockData != null;
        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().warning("[WooHolograms] CraftEngine 钩子初始化失败（版本不兼容？）: " + e.getMessage());
            ready = false;
        }
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
     * <p>等价于直接调用：{@code CraftEngineItems.byId(Key.of(id)).buildBukkitItem()}。</p>
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
            Object definition = itemsById.invoke(null, keyOf.invoke(null, id));
            if (definition == null) {
                return null;
            }
            Method build = buildItemCache.computeIfAbsent(definition.getClass(),
                    c -> findInstance(c, "buildBukkitItem", 0, ItemStack.class));
            return build == null ? null : (ItemStack) build.invoke(definition);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 CE 自定义方块的 Bukkit BlockData。
     *
     * <p>等价于直接调用：{@code CraftEngineBlocks.getBukkitBlockData(
     * CraftEngineBlocks.byId(Key.of(id)).defaultState())}。</p>
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
            Object definition = blocksById.invoke(null, keyOf.invoke(null, id));
            if (definition == null) {
                return null;
            }
            Method defaultState = defaultStateCache.computeIfAbsent(definition.getClass(),
                    c -> findInstance(c, "defaultState", 0, null));
            if (defaultState == null) {
                return null;
            }
            Object state = defaultState.invoke(definition);
            return state == null ? null : (BlockData) getBukkitBlockData.invoke(null, state);
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
        return listIds(itemsLoadedItems);
    }

    /**
     * 列出 CE 已注册的自定义方块 ID（用于 Tab 补全）。
     *
     * @return ID 列表；CE 离线返回空列表
     */
    public List<String> listBlockIds() {
        return listIds(blocksLoadedBlocks);
    }

    /**
     * 从 CE 注册表 Map（Key -> 定义）中提取全部 ID 字符串。
     */
    private List<String> listIds(Method loadedMethod) {
        if (!ready || loadedMethod == null) {
            return List.of();
        }
        try {
            Map<?, ?> loaded = (Map<?, ?>) loadedMethod.invoke(null);
            List<String> ids = new ArrayList<>();
            for (Object key : loaded.keySet()) {
                Method asString = keyAsStringCache.computeIfAbsent(key.getClass(),
                        c -> findInstance(c, "asString", 0, String.class));
                if (asString == null) {
                    continue;
                }
                ids.add((String) asString.invoke(key));
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 按名称+参数个数+返回类型查找静态方法（返回类型可为 null 表示不校验）
     */
    private static Method findStatic(Class<?> owner, String name, int params, Class<?> expectedReturn) {
        for (Method m : owner.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params
                    && Modifier.isStatic(m.getModifiers())
                    && (expectedReturn == null || expectedReturn.isAssignableFrom(m.getReturnType()))) {
                return m;
            }
        }
        return null;
    }

    /**
     * 按名称+参数个数+返回类型查找实例方法（返回类型可为 null 表示不校验）
     */
    private static Method findInstance(Class<?> owner, String name, int params, Class<?> expectedReturn) {
        for (Method m : owner.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params
                    && !Modifier.isStatic(m.getModifiers())
                    && (expectedReturn == null || expectedReturn.isAssignableFrom(m.getReturnType()))) {
                return m;
            }
        }
        return null;
    }
}
