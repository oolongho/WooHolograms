package com.oolonghoo.holograms.action;

import org.bukkit.entity.Player;

/**
 * 动作类
 * 参考 DecentHolograms 的 Action 实现
 * 表示一个可执行的动作
 * 
 * @author oolongho
 */
public class Action {

    /**
     * 动作类型
     */
    private final ActionType type;

    /**
     * 动作数据
     */
    private String data;

    /**
     * 点击类型（可选）
     */
    private ClickType clickType;

    /**
     * 从字符串创建动作
     * 格式: TYPE:DATA 或 TYPE
     * 
     * @param string 动作字符串
     * @throws IllegalArgumentException 如果动作类型无效
     */
    public Action(String string) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("动作字符串不能为空");
        }

        if (string.contains(":")) {
            String[] spl = string.split(":", 2);
            this.type = ActionType.getByName(spl[0]);
            this.data = spl.length > 1 ? spl[1] : "";
        } else {
            this.type = ActionType.getByName(string);
            this.data = null;
        }

        if (this.type == null) {
            throw new IllegalArgumentException("无效的动作类型: " + string);
        }

        this.clickType = ClickType.ANY;
    }

    /**
     * 创建动作
     * 
     * @param type 动作类型
     * @param data 动作数据
     */
    public Action(ActionType type, String data) {
        this.type = type;
        this.data = data;
        this.clickType = ClickType.ANY;
    }

    /**
     * 创建动作（带点击类型）
     * 
     * @param type 动作类型
     * @param data 动作数据
     * @param clickType 点击类型
     */
    public Action(ActionType type, String data, ClickType clickType) {
        this.type = type;
        this.data = data;
        this.clickType = clickType != null ? clickType : ClickType.ANY;
    }

    /**
     * 执行动作
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean execute(Player player) {
        if (type == null || player == null) {
            return false;
        }
        return type.execute(player, data);
    }

    /**
     * 获取动作类型
     * 
     * @return 动作类型
     */
    public ActionType getType() {
        return type;
    }

    /**
     * 获取动作数据
     * 
     * @return 动作数据
     */
    public String getData() {
        return data;
    }

    /**
     * 设置动作数据
     * 
     * @param data 动作数据
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * 获取点击类型
     * 
     * @return 点击类型
     */
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * 设置点击类型
     * 
     * @param clickType 点击类型
     */
    public void setClickType(ClickType clickType) {
        this.clickType = clickType;
    }

    @Override
    public String toString() {
        if (data == null || data.isEmpty()) {
            return type.getName();
        }
        return type.getName() + ":" + data;
    }

    /**
     * 从字符串解析动作
     * 
     * @param string 动作字符串
     * @return 动作实例，如果解析失败返回 null
     */
    public static Action fromString(String string) {
        try {
            return new Action(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
