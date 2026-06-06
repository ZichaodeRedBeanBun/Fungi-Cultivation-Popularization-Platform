package cn.edu.seig.MhWeb.model.entity;


import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科普中国菇类栽培/种植内容表实体（图文+视频合并）
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_popsci_content")
public class PopSciContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科普内容主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联菌种ID（核心：绑定到具体菇类）
     */
    @TableField("mushroom_id")
    private Long mushroomId;

    /**
     * 内容类型（图文/视频）：article-图文，video-视频
     */
    @TableField("content_type")
    private PopSciContentTypeEnum contentType;

    /**
     * 标题（如[科普中国]-富硒香菇的栽培方法）
     */
    @TableField("title")
    private String title;

    /**
     * 关联科普号ID（mh_user.user_id，仅user_type=3的已认证科普号）
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 发布日期（如2021-12-31）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("publish_date")
    private LocalDate publishDate;

    /**
     * 点赞数
     */
    @TableField("praise_count")
    private Integer praiseCount;

    /**
     * 评论数
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 收藏数
     */
    @TableField("collect_count")
    private Integer collectCount;
    /**
     * 图文正文（结构化文本）
     */
    @TableField("content")
    private String content;

    /**
     * 视频播放链接
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 内容描述
     */
    @TableField("description")
    private String description;

    /**
     * 原文/原视频URL
     */
    @TableField("source_url")
    private String sourceUrl;

    /**
     * 内容状态：1=待审核，2=审核通过（可展示），3=审核驳回
     */
    @TableField("content_status")
    private PopSciContentStatusEnum contentStatus;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 驳回原因（仅content_status=3时填写）
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;


}