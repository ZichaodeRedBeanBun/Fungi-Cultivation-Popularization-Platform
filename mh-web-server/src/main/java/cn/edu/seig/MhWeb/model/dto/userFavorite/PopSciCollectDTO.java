package cn.edu.seig.MhWeb.model.dto.userFavorite;

import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 科普内容收藏/取消收藏DTO
 *
 * @author su
 */
@Data
public class PopSciCollectDTO {
    /**
     * 科普内容ID
     */
    @NotNull(message = "科普内容ID不能为空")
    private Long popsciId;

    /**
     * 收藏类型：0-图文，1-视频
     */
    @NotNull(message = "收藏类型不能为空")
    private FavoriteTypeEnum favoriteType;

    /**
     * 菌种ID（冗余存储）
     */
    @NotNull(message = "菌种ID不能为空")
    private Long mushroomId;
}