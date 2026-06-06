package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.ibatis.type.MappedTypes;

/**
 * 科普内容类型枚举
 * 适配entity中contentType字段：1=图文，2=视频
 *
 * @author su
 */
@Getter
public enum PopSciContentTypeEnum {

    /**
     * 图文类型
     */
    ARTICLE(1, "article", "图文"),

    /**
     * 视频类型
     */
    VIDEO(2, "video", "视频");

    /**
     * 状态数字ID（适配数据库存储/前端传参，对应content_type字段值）
     */
    private final Integer id;

    /**
     * 类型编码（前端/业务层统一标识，如：article/video）
     */
    private final String code;

    /**
     * 类型名称（前端展示用，如：图文/视频）
     */
    private final String name;

    PopSciContentTypeEnum(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static PopSciContentTypeEnum fromCode(String code) {
        for (PopSciContentTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
    /**
     * 根据编码（article/video）获取枚举（常用工具方法）
     */
    public static PopSciContentTypeEnum getByCode(String code) {
        for (PopSciContentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据数字ID（1/2）获取枚举（常用工具方法）
     */
    public static PopSciContentTypeEnum getById(Integer id) {
        for (PopSciContentTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}