package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import cn.edu.seig.MhWeb.model.dto.userFavorite.PopSciFavoriteDTO;
import cn.edu.seig.MhWeb.model.entity.UserFavourite;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户收藏科普内容Service接口
 *
 * @author su
 */
public interface IUserFavoritePopSciService extends IService<UserFavourite> {
    /**
     * 收藏科普内容
     *
     * @param popsciId      科普内容ID
     * @param favoriteType  收藏类型：0-图文，1-视频
     * @param mushroomName    菌种ID
     * @return 收藏结果
     */
    Result collectPopSci(Long popsciId, FavoriteTypeEnum favoriteType, String mushroomName);

    /**
     * 取消收藏科普内容
     *
     * @param popsciId 科普内容ID
     * @return 取消收藏结果
     */
    Result cancelCollectPopSci(Long popsciId);

    /**
     * 获取用户收藏的科普内容列表（分页）
     *
     * @param dto 查询条件（页码、每页条数、收藏类型、菌种ID）
     * @return 分页结果
     */
    Result<PageResult<PopSciContentSummaryVO>> getUserFavoritePopSciList(PopSciFavoriteDTO dto);

    Result batchCancelCollectPopSci(List<Long> popsciIds);
}