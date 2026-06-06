package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 科普内容收藏类型枚举
 * 对应类型：0=图文 1=视频
 *
 * @author su
 */
@Getter
public enum FavoriteTypeEnum {

    /** 图文 */
    ARTICLE(0, "ARTICLE", "图文"),
    /** 视频 */
    VIDEO(1, "VIDEO", "视频");

    /** 类型ID（数据库存储的数字值） */
    private final Integer id;
    /** 类型编码（字符串标识，用于前端/接口传输） */
    private final String code;
    /** 类型中文描述（用于页面展示） */
    private final String typeDesc;

    /**
     * 构造方法
     * @param id 类型ID
     * @param code 类型编码
     * @param typeDesc 中文描述
     */
    FavoriteTypeEnum(Integer id, String code, String typeDesc) {
        this.id = id;
        this.code = code;
        this.typeDesc = typeDesc;
    }

    /**
     * 序列化时返回code（前端接收的是字符串编码，如"ARTICLE"）
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 反序列化时根据code解析枚举（前端传code可自动转换为枚举）
     */
    @JsonCreator
    public static FavoriteTypeEnum fromCode(String code) {
        for (FavoriteTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据类型ID（数字）获取枚举（核心：替代原有switch逻辑）
     */
    public static FavoriteTypeEnum getById(Integer id) {
        for (FavoriteTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}