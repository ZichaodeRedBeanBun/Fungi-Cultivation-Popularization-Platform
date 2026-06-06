package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 病虫害类型枚举
 * 适配entity中itemType字段：1=病害，2=虫害
 *
 * @author su
 */
@Getter
public enum DiseasePestTypeEnum {

    /**
     * 病害类型
     */
    DISEASE(1, "disease", "病害"),

    /**
     * 虫害类型
     */
    PEST(2, "pest", "虫害");

    /**
     * 状态数字ID（适配数据库存储/前端传参，对应itemType字段值）
     */
    private final Integer id;

    /**
     * 类型编码（前端/业务层统一标识，如：disease/pest）
     */
    private final String code;

    /**
     * 类型名称（前端展示用，如：病害/虫害）
     */
    private final String name;

    DiseasePestTypeEnum(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static DiseasePestTypeEnum fromCode(String code) {
        for (DiseasePestTypeEnum status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    public static DiseasePestTypeEnum getById(Integer id) {
        for (DiseasePestTypeEnum status : values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}