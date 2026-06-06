package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.mapper.CommentMapper;
import cn.edu.seig.MhWeb.mapper.PopSciContentMapper;
import cn.edu.seig.MhWeb.mapper.UserMapper;

import cn.edu.seig.MhWeb.model.dto.comment.CommentPopSciDTO;
import cn.edu.seig.MhWeb.model.entity.Comment;
import cn.edu.seig.MhWeb.model.entity.PopSciContent;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.model.vo.CommentPopSciVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.edu.seig.MhWeb.constant.MessageConstant.LIKE;

/**
 * 科普内容评论服务实现类
 *
 * @author su
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper; // 用于查询用户信息（VO组装）
    @Autowired
    private PopSciContentMapper popSciContentMapper;

    /**
     * 新增科普内容评论
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 新增评论后清空科普内容缓存
    public Result addPopSciComment(CommentPopSciDTO commentPopSciDTO) {
        try {
            // 1. 参数校验
            if (commentPopSciDTO.getPopsciId() == null) {
                return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
            }
            if (!StringUtils.hasText(commentPopSciDTO.getContent())) {
                return Result.error("评论内容不能为空");
            }

            // 2. 获取当前登录用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return Result.error(MessageConstant.USER + MessageConstant.NOT_LOGIN);
            }

            // 3. 构建评论实体
            Comment comment = new Comment();
            comment.setUserId(userId)
                    .setContentId(commentPopSciDTO.getPopsciId())
                    .setContent(commentPopSciDTO.getContent())
                    .setCreateTime(LocalDateTime.now())
                    .setLikeCount(0); // 初始点赞数为0

            // 4. 插入数据库
            int insertCount = commentMapper.insert(comment);
            if (insertCount == 0) {
                log.error("新增科普内容评论失败（userId={}, popsciId={}）", userId, commentPopSciDTO.getPopsciId());
                return Result.error(MessageConstant.COMMENT+MessageConstant.ADD + MessageConstant.FAILED);
            }

            log.info("用户{}新增科普内容{}评论成功", userId, commentPopSciDTO.getPopsciId());
            return Result.success(MessageConstant.COMMENT+MessageConstant.ADD + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("新增科普内容评论异常（popsciId={}）：", commentPopSciDTO.getPopsciId(), e);
            return Result.error(MessageConstant.COMMENT+MessageConstant.ADD + MessageConstant.FAILED);
        }
    }

    /**
     * 点赞评论
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 点赞后清空缓存
    public Result likeComment(Long commentId) {
        try {
            // 1. 校验评论存在
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                return Result.error(MessageConstant.COMMENT + MessageConstant.NOT_FOUND);
            }

            // 2. 更新点赞数（空值处理）
            Integer likeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
            comment.setLikeCount(likeCount + 1);

            // 3. 执行更新
            int updateCount = commentMapper.updateById(comment);
            if (updateCount == 0) {
                log.error("点赞评论失败（commentId={}）", commentId);
                return Result.error(LIKE + MessageConstant.FAILED);
            }

            log.info("评论{}点赞成功，当前点赞数：{}", commentId, comment.getLikeCount());
            return Result.success(LIKE + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("点赞评论异常（commentId={}）：", commentId, e);
            return Result.error(LIKE + MessageConstant.FAILED);
        }
    }

    /**
     * 取消点赞评论
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 取消点赞后清空缓存
    public Result cancelLikeComment(Long commentId) {
        try {
            // 1. 校验评论存在
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                return Result.error(MessageConstant.COMMENT + MessageConstant.NOT_FOUND);
            }

            // 2. 更新点赞数（空值+负数处理）
            Integer likeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
            comment.setLikeCount(Math.max(0, likeCount - 1)); // 保证点赞数≥0

            // 3. 执行更新
            int updateCount = commentMapper.updateById(comment);
            if (updateCount == 0) {
                log.error("取消点赞评论失败（commentId={}）", commentId);
                return Result.error(MessageConstant.DELETE+LIKE + MessageConstant.FAILED);
            }

            log.info("评论{}取消点赞成功，当前点赞数：{}", commentId, comment.getLikeCount());
            return Result.success(MessageConstant.DELETE+LIKE + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("取消点赞评论异常（commentId={}）：", commentId, e);
            return Result.error(MessageConstant.DELETE+LIKE + MessageConstant.FAILED);
        }
    }

       /**
     * 根据科普内容ID查询评论列表（含用户信息，按创建时间倒序）
     */
    @Override
    public List<CommentPopSciVO> listPopSciComment(Long popsciId) {
        try {
            // 1. 参数校验
            if (popsciId == null) {
                log.error("查询科普评论失败：科普内容ID不能为空");
                throw new IllegalArgumentException(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
            }

            // 2. 联表查询：评论表 + 用户表（获取用户名/头像），按创建时间倒序
            List<CommentPopSciVO> commentVOList = commentMapper.listPopSciCommentByContentId(popsciId);

            log.info("查询科普内容{}的评论列表成功，共{}条评论", popsciId, commentVOList.size());
            return commentVOList;
        } catch (Exception e) {
            log.error("查询科普内容{}的评论列表异常：", popsciId, e);
            throw new RuntimeException(MessageConstant.COMMENT +MessageConstant.NOT_EXIST);
        }
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证删除评论+更新数原子性
    public Result deleteComment(Long commentId) {
        try {
            // 1. 基础参数校验
            if (commentId == null) {
                return Result.error(MessageConstant.COMMENT + MessageConstant.ID + MessageConstant.NOT_NULL);
            }

            // 2. 查询评论信息，判断是否存在
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                return Result.error(MessageConstant.COMMENT + MessageConstant.NOT_EXIST);
            }
            Long commentUserId = comment.getUserId(); // 评论发布者ID（mh_content_comment.user_id）
            Long popsciContentId = comment.getContentId(); // 关联的科普内容ID

            // 3. 查询科普内容的【作者ID+评论数】，判断科普内容是否存在
            PopSciContent popsciContent = popSciContentMapper.getAuthorAndReviewCount(popsciContentId);
            if (popsciContent == null) {
                log.error("删除评论失败：关联的科普内容不存在（contentId={}）", popsciContentId);
                return Result.error("关联的科普内容已删除，无法操作评论");
            }
            Long popsciAuthorId = popsciContent.getAuthorId(); // 科普内容作者ID（mh_popsci_content.author_id）

            // 4. 获取当前操作用户ID（========== 自行补充用户ID逻辑 ==========）
            Long currentOperateUserId = BaseContext.getCurrentId();
            if (currentOperateUserId == null) {
                return Result.error(MessageConstant.USER + MessageConstant.NOT_LOGIN);
            }

            // 5. 核心权限判断：当前用户是「评论发布者」OR「科普内容作者」，否则无权限
            if (!currentOperateUserId.equals(commentUserId) && !currentOperateUserId.equals(popsciAuthorId)) {
                log.error("删除评论失败：无操作权限（commentId={}, 当前用户={}, 评论作者={}, 科普作者={}）",
                        commentId, currentOperateUserId, commentUserId, popsciAuthorId);
                return Result.error("仅评论发布者和科普内容作者可删除该评论");
            }

            // 6. 执行删除：先删评论，再同步更新科普内容的评论数（开启事务，原子操作）
            int deleteCount = commentMapper.deleteById(commentId);
            if (deleteCount == 0) {
                log.error("删除科普评论失败（commentId={}）", commentId);
                return Result.error(MessageConstant.COMMENT + MessageConstant.DELETE + MessageConstant.FAILED);
            }
            // 同步更新：科普内容评论数减1（最小为0，避免负数）
            popSciContentMapper.decrementReviewCount(popsciContentId);

            log.info("用户{}删除科普评论成功（commentId={}, 科普内容ID={}）",
                    currentOperateUserId, commentId, popsciContentId);
            return Result.success(MessageConstant.COMMENT + MessageConstant.DELETE + MessageConstant.SUCCESS);

        } catch (Exception e) {
            log.error("删除科普评论异常（commentId={}）：", commentId, e);
            return Result.error(MessageConstant.COMMENT + MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }

}