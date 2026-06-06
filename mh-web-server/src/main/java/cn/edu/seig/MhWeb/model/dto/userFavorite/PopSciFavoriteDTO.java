package cn.edu.seig.MhWeb.model.dto.userFavorite;

import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 科普内容收藏查询DTO
 *
 * @author su
 */
@Data
public class PopSciFavoriteDTO {
    /**
     * 页码（默认1）
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10）
     */
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;

    /**
     * 收藏类型：0-图文，1-视频（可选）
     */
    private FavoriteTypeEnum favoriteType;

    /**
     * 菌种ID（可选，筛选指定菌种的收藏内容）
     */
    private Long mushroomId;
}