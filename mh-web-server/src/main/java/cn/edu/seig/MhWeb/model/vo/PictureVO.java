package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片通用VO（适配mh_picture表，可复用在所有图片展示场景）
 * 适用：菌种详情图、科普图文详情图、视频封面、栽培示意图等所有图片类型
 * @author su
 */
@Data
public class PictureVO {
    /**
     * 图片主键ID
     */
    private Long id;

    /**
     * 图片链接URL
     */
    private String picUrl;

    /**
     * 关联的菌种ID
     */
    private Long mushroomId;

    /**
     * 关联科普内容表主键ID（mh_popsci_content.id）
     * NULL = 菌种基础详情图；非NULL = 科普内容关联图
     */
    private Long contentId;

    /**
     * 图片类型：详情图/图文详情图/菌种封面图/视频封面/栽培示意图
     */
    private PictureTypeEnum type;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ========== 扩展字段（非数据库字段，按需使用） ==========
    /**
     * 图片类型中文描述（冗余字段，方便前端展示）
     */
    private String typeDesc;

    /**
     * 关联的菌种名称（冗余字段，方便前端展示）
     */
    private String mushroomName;
}