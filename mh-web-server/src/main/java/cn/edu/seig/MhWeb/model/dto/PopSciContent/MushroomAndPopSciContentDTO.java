package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菌菇与科普内容关联查询DTO
 *
 * @author su
 */
@Data
public class MushroomAndPopSciContentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull
    private Integer pageSize;

    /**
     * 关联菌种ID（外键，精准查询）
     */
    private Integer mushroomId;

    /**
     * 科普内容类型（article-图文/ video-视频，精准查询）
     */
    private String contentType;

    /**
     * 科普内容标题（模糊查询）
     */
    private String title;
//
//    /**
//     * 科普内容状态（1=待审核/2=审核通过/3=审核驳回，精准查询）
//     */
//    private Integer contentStatus;
//
//    /**
//     * 发布日期起始（范围查询）
//     */
//    private LocalDate publishDateStart;
//
//    /**
//     * 发布日期结束（范围查询）
//     */
//    private LocalDate publishDateEnd;
}