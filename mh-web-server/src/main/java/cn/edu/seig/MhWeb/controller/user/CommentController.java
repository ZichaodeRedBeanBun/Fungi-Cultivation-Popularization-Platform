package cn.edu.seig.MhWeb.controller.user;


import cn.edu.seig.MhWeb.model.dto.comment.CommentPopSciDTO;
import cn.edu.seig.MhWeb.model.vo.CommentPopSciVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科普内容评论控制器
 *
 * @author su
 */
@RestController
@RequestMapping("/user/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 新增科普内容评论
     * @param commentPopSciDTO 评论入参
     * @return 操作结果
     */
    @PostMapping("/addPopSciComment")
    public Result addPopSciComment(@RequestBody CommentPopSciDTO commentPopSciDTO) {
        return commentService.addPopSciComment(commentPopSciDTO);
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    @PatchMapping("/likeComment/{id}")
    public Result likeComment(@PathVariable("id") Long commentId) {
        return commentService.likeComment(commentId);
    }

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    @PatchMapping("/cancelLikeComment/{id}")
    public Result cancelLikeComment(@PathVariable("id") Long commentId) {
        return commentService.cancelLikeComment(commentId);
    }

    /**
     * 根据科普内容ID查询评论列表（含用户昵称/头像，按评论时间倒序）
     * @param popsciId 科普内容主键ID
     * @return 评论VO列表
     */
    @GetMapping("/listPopSciComment/{popsciId}")
    public Result<List<CommentPopSciVO>> listPopSciComment(@PathVariable("popsciId") Long popsciId) {
        List<CommentPopSciVO> commentList = commentService.listPopSciComment(popsciId);
        return Result.success(commentList);
    }
    // 原有代码无修改，删除接口仍为：
    @DeleteMapping("/deleteComment/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }
}