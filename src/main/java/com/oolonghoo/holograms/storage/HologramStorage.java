package com.oolonghoo.holograms.storage;

import java.util.Map;

import com.oolonghoo.holograms.hologram.Hologram;

/**
 * 全息图存储接口
 * 定义全息图持久化存储的基本操作
 * 
 * 
 */
public interface HologramStorage {

    /**
     * 加载所有全息图
     * 从存储中读取所有全息图数据并创建 Hologram 对象
     * 
     * @return 加载的全息图映射（名称 -> 全息图）
     */
    Map<String, Hologram> loadAll();

    /**
     * 加载单个全息图
     * 
     * @param id 全息图 ID
     * @return 全息图，如果不存在返回 null
     */
    Hologram load(String id);

    /**
     * 保存单个全息图
     * 将全息图数据持久化到存储中
     * 
     * @param hologram 要保存的全息图
     * @return 是否保存成功
     */
    boolean save(Hologram hologram);

    /**
     * 异步保存单个全息图
     * 使用 Bukkit 调度器在异步线程中保存
     * 
     * @param hologram 要保存的全息图
     */
    void saveAsync(Hologram hologram);

    /**
     * 保存所有全息图
     * 将所有已修改的全息图持久化到存储中
     */
    void saveAll();

    /**
     * 异步保存所有全息图
     * 使用 Bukkit 调度器在异步线程中保存所有全息图
     */
    void saveAllAsync();

    /**
     * 删除全息图文件
     * 从存储中移除指定名称的全息图数据
     * 
     * @param id 全息图 ID
     * @return 是否删除成功
     */
    boolean delete(String id);

    /**
     * 检查全息图是否存在
     * 判断存储中是否存在指定名称的全息图数据
     * 
     * @param id 全息图 ID
     * @return 是否存在
     */
    boolean exists(String id);

    /**
     * 获取存储的全息图数量
     * 
     * @return 存储中的全息图数量
     */
    int count();

    /**
     * 重载存储
     * 重新初始化存储系统
     */
    void reload();
}
