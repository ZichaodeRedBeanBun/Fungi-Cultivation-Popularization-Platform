package cn.edu.seig.MhWeb.model.dto.comment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普内容评论入参DTO
 *
 * @author su
 */
@Data
public class CommentPopSciDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科普内容ID（图文/视频）
     */
    private Long popsciId;

    /**
     * 评论内容
     */
    private String content;

}