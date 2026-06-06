package cn.edu.seig.MhWeb.mapper;

import cn.edu.seig.MhWeb.model.entity.Comment;
import cn.edu.seig.MhWeb.model.vo.CommentPopSciVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author su
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 联表查询科普内容的评论列表（含用户用户名、头像）
     * @param contentId 科普内容ID（即popsciId）
     * @return 评论VO列表
     */
    /**
     * 联表查询科普内容的评论列表（含用户信息 + 科普内容作者ID）
     * @param contentId 科普内容ID（即popsciId）
     * @return 评论VO列表
     */
    @Select("""
        SELECT 
            c.id AS commentId,
            u.user_id AS userId, -- 评论作者ID（评论用户的ID）
            u.username AS username,
            u.avatar AS userAvatar,
            c.content,
            c.create_time AS createTime,
            c.like_count AS likeCount,
            p.author_id AS contentAuthorId -- 新增：科普内容的作者ID（对应mh_popsci_content的author_id）
        FROM mh_content_comment c
        LEFT JOIN mh_user u ON c.user_id = u.user_id
        INNER JOIN mh_popsci_content p ON c.content_id = p.id -- 关联科普内容表，获取作者ID
        WHERE c.content_id = #{contentId}
        ORDER BY c.create_time DESC
        """)
    List<CommentPopSciVO> listPopSciCommentByContentId(@Param("contentId") Long contentId);
}
