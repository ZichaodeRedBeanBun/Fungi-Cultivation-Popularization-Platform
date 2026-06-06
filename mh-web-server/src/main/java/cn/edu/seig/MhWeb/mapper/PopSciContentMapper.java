package cn.edu.seig.MhWeb.mapper;


import cn.edu.seig.MhWeb.model.dto.UserContentQuery.UserContentQueryDTO;
import cn.edu.seig.MhWeb.model.entity.PopSciContent;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author su
 */
@Mapper
public interface PopSciContentMapper extends BaseMapper<PopSciContent> {
        /**
         * 按类型查询随机科普内容（图文/视频）
         * @param contentType 内容类型：article/video
         * @param limit 返回数量
         * @return 随机内容列表
         */
        List<PopSciContent> getRandomPopSciContentsByType(@Param("contentType") String contentType, @Param("limit") Integer limit);

        /**
         * 按菌种ID+内容类型推荐内容（排除已收藏）
         * @param sortedMushroomIds 排序后的菌种ID
         * @param favoriteContentIds 已收藏内容ID（排除）
         * @param contentType 内容类型：article/video
         * @param limit 候选数量
         * @return 推荐内容列表
         */
        List<PopSciContent> getRecommendedByMushroomIdsAndType(
                @Param("sortedMushroomIds") List<Long> sortedMushroomIds,
                @Param("favoriteContentIds") List<Long> favoriteContentIds,
                @Param("contentType") String contentType,
                @Param("limit") Integer limit
        );

        // 保留原有方法（getFavoriteMushroomIds / getRecommendedByMushroomIds 等）
        List<Long> getFavoriteMushroomIds(@Param("favoriteContentIds") List<Long> favoriteContentIds);
        List<PopSciContent> getRecommendedByMushroomIds(
                @Param("sortedMushroomIds") List<Long> sortedMushroomIds,
                @Param("favoriteContentIds") List<Long> favoriteContentIds,
                @Param("limit") Integer limit
        );

        /**
         * 按类型+权重排序查询随机内容（未登录/兜底用）
         * @param contentType 内容类型（article/video）
         * @param targetCount 目标数量
         * @return 高权重随机内容列表
         */
        List<PopSciContent> getRandomHighWeightContentByType(
                @Param("contentType") String contentType,
                @Param("targetCount") int targetCount);

        /**
         * 按菌种+类型+权重排序推荐（排除已收藏）
         * @param sortedMushroomIds 排序后的菌种ID列表
         * @param favoriteContentIds 已收藏内容ID（排除）
         * @param contentType 内容类型
         * @param candidateCount 候选数量
         * @return 按权重排序的推荐内容
         */
        List<PopSciContent> getRecommendedByMushroomIdsAndTypeWithWeight(
                @Param("sortedMushroomIds") List<Long> sortedMushroomIds,
                @Param("favoriteContentIds") List<Long> favoriteContentIds,
                @Param("contentType") String contentType,
                @Param("candidateCount") int candidateCount);

        /**
         * 查询用户收藏的科普内容ID
         */
        List<Long> getFavoriteContentIdsByUserId(@Param("userId") Long userId);
        /**
         * 根据科普内容ID查询【作者ID+评论数】（仅用于评论删除权限判断+数据同步）
         * @param contentId 科普内容ID（mh_popsci_content.id）
         * @return 科普内容对象（仅封装author_id和review_count）
         */
        @Select("SELECT author_id, review_count FROM mh_popsci_content WHERE id = #{contentId}")
        PopSciContent getAuthorAndReviewCount(@Param("contentId") Long contentId);

        /**
         * 科普内容评论数减1（最小为0，保证数据不出现负数）
         * @param contentId 科普内容ID
         */
        @Update("UPDATE mh_popsci_content SET review_count = IF(review_count > 0, review_count - 1, 0) WHERE id = #{contentId}")
        void decrementReviewCount(@Param("contentId") Long contentId);

        /**
         * 科普内容动态查询（支持多条件任意组合 + 标题/描述模糊查询）
         * @param mushroomId 菌种ID（可为null）
         * @param contentType 内容类型（article/video，可为null）
         * @param authorId 科普号ID（可为null）
         * @param contentStatus 内容状态（1/2/3，可为null）
         * @param keyword 模糊查询关键词（匹配标题/描述，可为null/空字符串）
         * @return 符合条件的科普内容列表
         */
        List<PopSciContent> pageQuery(
                @Param("mushroomId") Integer mushroomId,
                @Param("contentType") String contentType,
                @Param("authorId") Integer authorId,
                @Param("contentStatus") Integer contentStatus,
                @Param("keyword") String keyword // 新增：模糊查询关键词参数
        );
        /**
         * 查询用户发布的科普内容精简VO（分页）
         */
        List<PopSciContentSummaryVO> selectUserContentSummary(@Param("dto") UserContentQueryDTO dto,
                                                              @Param("offset") Integer offset);

        /**
         * 查询用户发布的科普内容总数
         */
        Long countUserContent(@Param("dto") UserContentQueryDTO dto);
}
