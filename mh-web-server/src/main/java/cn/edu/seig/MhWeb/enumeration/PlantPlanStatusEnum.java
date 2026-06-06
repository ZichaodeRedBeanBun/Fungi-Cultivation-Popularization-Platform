package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 种植计划状态枚举
 * 对应状态：1=执行中 2=已完成 3=暂停 4=作废
 *
 * @author su
 */
@Getter
public enum PlantPlanStatusEnum {

    /** 执行中 */
    EXECUTING(1, "EXECUTING", "执行中"),
    /** 已完成 */
    COMPLETED(2, "COMPLETED", "已完成"),
    /** 暂停 */
    PAUSED(3, "PAUSED", "暂停"),
    /** 作废 */
    INVALID(4, "INVALID", "作废");

    /** 状态ID（数据库存储的数字值） */
    private final Integer id;
    /** 状态编码（字符串标识，用于前端/接口传输） */
    private final String code;
    /** 状态中文描述（用于页面展示） */
    private final String statusDesc;

    /**
     * 构造方法
     * @param id 状态ID
     * @param code 状态编码
     * @param statusDesc 中文描述
     */
    PlantPlanStatusEnum(Integer id, String code, String statusDesc) {
        this.id = id;
        this.code = code;
        this.statusDesc = statusDesc;
    }

    /**
     * 序列化时返回code（前端接收的是字符串编码，如"EXECUTING"）
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 反序列化时根据code解析枚举（前端传code可自动转换为枚举）
     */
    @JsonCreator
    public static PlantPlanStatusEnum fromCode(String code) {
        for (PlantPlanStatusEnum status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态ID（数字）获取枚举（核心：替代原有switch逻辑）
     */
    public static PlantPlanStatusEnum getById(Integer id) {
        for (PlantPlanStatusEnum status : values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取状态中文描述（无匹配时返回"未知"）
     */
    public static String getStatusDescById(Integer id) {
        PlantPlanStatusEnum status = getById(id);
        return status != null ? status.getStatusDesc() : "未知";
    }
}