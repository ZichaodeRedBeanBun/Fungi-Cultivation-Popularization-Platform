package cn.edu.seig.MhWeb.model.entity;

import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum; // 替换为你的枚举实际包路径
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 食用菌种图片链接表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_picture")
public class Picture implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图片主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 图片链接URL
     */
    @TableField("pic_url")
    private String picUrl;

    /**
     * 关联的菌种ID
     */
    @TableField("mushroom_id")
    private Long mushroomId;
    /**
     * 关联的病虫害ID
     */
    @TableField("disease_id")
    private Long diseaseId;

    /**
     * 关联科普内容表主键ID（NULL表示菌种基础详情图）
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 图片类型（枚举：详情图/图文详情图/封面图/视频封面/栽培示意图）
     */
    @TableField("type")
    private PictureTypeEnum type; // 核心修改：String → PictureTypeEnum

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

}