package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 科普内容评论VO（前端展示）
 *
 * @author su
 */
@Data
public class CommentPopSciVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long commentId;
    /**
     * 评论ID
     */
    private Long userId;
    /**
     * 评论ID
     */
    private Long contentAuthorId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间（格式化：yyyy-MM-dd）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    /**
     * 点赞数量
     */
    private Long likeCount;


}