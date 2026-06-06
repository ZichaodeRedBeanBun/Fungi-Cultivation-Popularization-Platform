package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 种植生长状态枚举
 * 对应状态：1=优 2=良 3=差
 *
 * @author su
 */
@Getter
public enum PlantGrowthStatusEnum {

    /** 优 */
    EXCELLENT(1, "EXCELLENT", "优"),
    /** 良 */
    GOOD(2, "GOOD", "良"),
    /** 差 */
    POOR(3, "POOR", "差");

    /** 状态ID（数据库存储的数字值） */
    private final Integer id;
    /** 状态编码（字符串标识，用于前端/接口传输） */
    private final String code;
    /** 状态中文描述（用于页面展示） */
    private final String statusDesc;

    /**
     * 构造方法（与PlantPlanStatusEnum格式完全对齐）
     * @param id 状态ID
     * @param code 状态编码
     * @param statusDesc 中文描述
     */
    PlantGrowthStatusEnum(Integer id, String code, String statusDesc) {
        this.id = id;
        this.code = code;
        this.statusDesc = statusDesc;
    }

    /**
     * 序列化时返回code（前端接收的是字符串编码，如"EXCELLENT"）
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 反序列化时根据code解析枚举（前端传code可自动转换为枚举）
     */
    @JsonCreator
    public static PlantGrowthStatusEnum fromCode(String code) {
        for (PlantGrowthStatusEnum status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态ID（数字）获取枚举（替代switch逻辑）
     */
    public static PlantGrowthStatusEnum getById(Integer id) {
        for (PlantGrowthStatusEnum status : values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取生长状态中文描述（无匹配时返回"未知"）
     */
    public static String getStatusDescById(Integer id) {
        PlantGrowthStatusEnum status = getById(id);
        return status != null ? status.getStatusDesc() : "未知";
    }
}