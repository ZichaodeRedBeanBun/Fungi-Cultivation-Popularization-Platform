package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普内容核心查询DTO（基础版）
 * 覆盖80%高频查询场景：分页 + 菌种ID + 标题 + 内容类型
 *
 * @author su
 */
@Data
public class PopSciContentPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize;

    /**
     * 关联菌种名称
     */
    private String mushroomName;

    /**
     * 科普内容标题（模糊查询）
     */
    @JsonAlias("theme")
    private String title;

    /**
     * 内容类型（article-图文/ video-视频，精准查询）
     */
    private PopSciContentTypeEnum contentType;


    /**
     * 科普号名称（模糊查询）
     */
    private String authorName; // 修正：原命名为author，语义更清晰

    /**
     * 内容状态：1=待审核，2=审核通过（可展示），3=审核驳回
     */
    private PopSciContentStatusEnum contentStatus;
}