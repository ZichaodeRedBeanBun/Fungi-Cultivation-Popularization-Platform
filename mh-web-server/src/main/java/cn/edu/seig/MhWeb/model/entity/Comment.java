package cn.edu.seig.MhWeb.model.entity;

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
 * 科普内容评论表实体类
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_content_comment") // 匹配数据库表名
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论主键ID
     */
    @TableId(value = "id", type = IdType.AUTO) // 表主键为id，自增策略
    private Long id;

    /**
     * 用户主键ID（关联mh_user.user_id）
     */
    @TableField("user_id")
    private Long userId; // 匹配表的int类型

    /**
     * 科普内容主键ID（关联mh_kepu_content.id）
     */
    @TableField("content_id")
    private Long contentId; // 匹配表的int类型

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论时间（默认当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 序列化格式
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 点赞数量（初始为0）
     */
    @TableField("like_count")
    private Integer likeCount; // 匹配表的int类型（默认0）

}