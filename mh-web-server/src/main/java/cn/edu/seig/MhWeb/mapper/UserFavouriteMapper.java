package cn.edu.seig.MhWeb.mapper;

import cn.edu.seig.MhWeb.model.dto.userFavorite.PopSciFavoriteDTO;
import cn.edu.seig.MhWeb.model.entity.UserFavourite;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public interface UserFavouriteMapper extends BaseMapper<UserFavourite> {
    @Select("SELECT popsci_id FROM mh_user_favorite_popsci WHERE user_id = #{userId} ")
    List<Long> getFavoriteContentIdsByUserId(Long userId);
    /**
     * 根据用户ID和科普内容ID查询收藏记录
     * @param userId 用户ID
     * @param popsciId 科普内容ID
     * @return 收藏记录列表
     */
    List<UserFavourite> selectByUserIdAndPopsciId(
            @Param("userId") Long userId,
            @Param("popsciId") Long popsciId);
    /**
     * 根据用户ID、收藏类型、菌种ID查询收藏的科普内容ID
     * @param dto
     * @return 收藏的科普内容ID列表
     */
    List<Long> selectPopsciIdsByUserIdAndType(
            @Param("dto") PopSciFavoriteDTO dto,
            @Param("userId") Long userId
    );
    /**
     * 批量查询科普内容精简 VO
     */
    List<PopSciContentSummaryVO> selectPopSciSummaryByPopsciIds(@Param("popsciIds") List<Long> popsciIds);
    
    /**
     * 分页查询用户收藏的科普内容精简 VO
     * @param page 分页对象
     * @param userId 用户 ID
     * @param dto 查询条件（收藏类型、菌种 ID 等）
     * @return 分页结果
     */
    IPage<PopSciContentSummaryVO> selectPopSciSummaryPage(
            @Param("page") Page<PopSciContentSummaryVO> page,
            @Param("userId") Long userId,
            @Param("dto") PopSciFavoriteDTO dto
    );
}
