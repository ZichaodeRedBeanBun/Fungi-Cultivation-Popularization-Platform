package cn.edu.seig.MhWeb.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除图片DTO（支持多种删除条件）
 */
@Data
@NoArgsConstructor // ✅ 必须有无参构造函数

public class PictureDTO {
    /**
     * 图片ID（优先使用）
     */
    private Long picId;

    /**
     * 关联科普内容ID（删除该内容下的所有图片）
     */
    private Long contentId;

    /**
     * 关联菌种ID（删除该菌种下的所有图片）
     */
    private Long mushroomId;

    /**
     * 关联病虫害ID（删除该病虫害下的所有图片）
     */
    private Long diseaseId;

    /**
     * 图片URL
     */
    private String picUrl;
}