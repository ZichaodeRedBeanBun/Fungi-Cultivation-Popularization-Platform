package cn.edu.seig.MhWeb.controller.user;


import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import cn.edu.seig.MhWeb.model.dto.userFavorite.PopSciFavoriteDTO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IUserFavoritePopSciService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收藏科普内容控制器
 *
 * @author su
 */
@RestController
@RequestMapping("/user/favorite")
public class UserFavoritePopSciController {

    @Autowired
    private IUserFavoritePopSciService userFavoritePopSciService;

    /**
     * 收藏科普内容
     *
     * @param popsciId      科普内容ID
     * @param favoriteType  收藏类型：0-图文，1-视频
     * @param mushroomName    菌种ID
     * @return 收藏结果
     */
    @PostMapping("/collect")
    public Result collectPopSci(
            @RequestParam @Valid Long popsciId,
            @RequestParam @Valid FavoriteTypeEnum favoriteType,
            @RequestParam @Valid String mushroomName) {
        return userFavoritePopSciService.collectPopSci(popsciId, favoriteType, mushroomName);
    }

    /**
     * 取消收藏科普内容
     *
     * @param popsciId 科普内容ID
     * @return 取消收藏结果
     */
    @DeleteMapping("/cancelCollect")
    public Result cancelCollectPopSci(@RequestParam @Valid Long popsciId) {
        return userFavoritePopSciService.cancelCollectPopSci(popsciId);
    }
    /**
     * 批量取消收藏科普内容
     *
     * @param popsciIds 科普内容ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batchCancelCollect")
    public Result batchCancelCollectPopSci(@RequestBody List<Long> popsciIds) {
        return userFavoritePopSciService.batchCancelCollectPopSci(popsciIds);
    }
    /**
     * 获取用户收藏的科普内容列表（分页）
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @PostMapping("/getFavoriteList")
    public Result<PageResult<PopSciContentSummaryVO>> getUserFavoritePopSciList(@RequestBody @Valid PopSciFavoriteDTO dto) {
        return userFavoritePopSciService.getUserFavoritePopSciList(dto);
    }

}