package cn.edu.seig.MhWeb.model.entity;

import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户收藏科普内容表（图文/视频）
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_user_favorite_popsci")
public class UserFavourite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 收藏记录主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联用户表主键）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏类型：0-图文（article），1-视频（video）
     */
    @TableField("favorite_type")
    private Integer favoriteType;

    /**
     * 关联科普内容ID（mh_popsci_content.id）
     */
    @TableField("popsci_id")
    private Long popsciId;

    /**
     * 关联菌种ID（冗余存储，mh_popsci_content.mushroom_id）
     */
    @TableField("mushroom_id")
    private Long mushroomId;

    /**
     * 收藏创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time")
    private LocalDateTime createTime;

}