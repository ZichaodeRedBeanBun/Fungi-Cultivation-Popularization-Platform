package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 科普内容精简VO（推荐图文/视频列表展示专用）
 *
 * @author su
 */
@Data
public class PopSciContentSummaryVO {
    /**
     * 科普内容主键ID（用于后续查询详情）
     */
    private Long id;

    /**
     * 内容类型：article-图文，video-视频
     */
    private PopSciContentTypeEnum contentType;

    private PopSciContentStatusEnum contentStatus;
    /**
     * 标题（前端用theme接收，保留原有注解）
     */
    @JsonProperty("theme")
    private String title;

    /**
     * 作者名字
     */
    private String authorName;

    /**
     * 发布日期（保留原有格式化）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 菌种名称（补充展示，非数据库字段）
     */
    private String mushroomName;

    /**
     * 图文封面图URL
     */
    private String articleCoverUrl;

    /**
     * 视频封面图URL
     */
    private String videoCoverUrl;
}