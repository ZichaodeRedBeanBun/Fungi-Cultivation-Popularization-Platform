package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.model.dto.comment.CommentPopSciDTO;
import cn.edu.seig.MhWeb.model.entity.Comment;
import cn.edu.seig.MhWeb.model.vo.CommentPopSciVO;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 科普内容评论服务接口
 *
 * @author su
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 新增科普内容评论
     * @param commentPopSciDTO 评论入参
     * @return 操作结果
     */
    Result addPopSciComment(CommentPopSciDTO commentPopSciDTO);

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    Result likeComment(Long commentId);

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    Result cancelLikeComment(Long commentId);

    /**
     * 删除评论（仅评论作者可删）
     * @param commentId 评论ID
     * @return 操作结果
     */
    Result deleteComment(Long commentId);

    List<CommentPopSciVO> listPopSciComment(Long popsciId);
}