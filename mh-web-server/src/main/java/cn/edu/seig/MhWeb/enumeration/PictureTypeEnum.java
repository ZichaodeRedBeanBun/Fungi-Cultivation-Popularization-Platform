package cn.edu.seig.MhWeb.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 菌种图片类型枚举（映射 mh_picture 表的 type 枚举字段）
 *
 * @author su
 */
@Getter
public enum PictureTypeEnum {

    /** 菌种基础详情图（无科普内容关联） */
    DETAIL("详情图"),

    /** 科普图文内容关联图 */
    ARTICLE_DETAIL("图文详情图"),

    /** 菌种封面图（核心展示图） */
    COVER("菌种封面图"),

    /** 科普视频封面图 */
    VIDEO_COVER("视频封面"),

    /** 栽培技术示意图 */
    CULTIVATION_SCHEMATIC("栽培示意图"),
    DISEASE_PEST_COVER("病虫害封面图");

    /**
     * 枚举值（映射数据库type字段的字符串值，@EnumValue让MyBatis-Plus自动适配）
     */
    @EnumValue
    private final String pictureType;

    /**
     * 构造方法
     * @param pictureType 数据库存储的类型字符串
     */
    PictureTypeEnum(String pictureType) {
        this.pictureType = pictureType;
    }

    /**
     * 根据数据库存储的字符串值反向获取枚举（便于业务层转换）
     */
    public static PictureTypeEnum getByPictureType(String pictureType) {
        for (PictureTypeEnum type : values()) {
            if (type.getPictureType().equals(pictureType)) {
                return type;
            }
        }
        return null;
    }
}