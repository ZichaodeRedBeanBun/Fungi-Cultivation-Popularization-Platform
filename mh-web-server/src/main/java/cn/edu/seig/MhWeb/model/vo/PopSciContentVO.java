package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import cn.edu.seig.MhWeb.model.entity.Picture;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 科普内容VO（前端展示）
 *
 * @author su
 */
@Data
public class PopSciContentVO {

    /**
     * 科普内容主键ID
     */
    private Long id;

    /**
     * 内容类型：article-图文，video-视频
     */
    private PopSciContentTypeEnum contentType;

    /**
     * 标题
     */
    @JsonProperty("theme")
    private String title;

    /**
     * 作者名字
     */
    private String authorName;

    /**
     * 发布日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    /**
     * 图文正文
     */
    private String content;

    /**
     * 视频播放链接
     */
    private String videoUrl;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 内容状态：1=待审核，2=审核通过，3=审核驳回
     */
    private PopSciContentStatusEnum contentStatus;

    /**
     * 菌种名称（补充展示，非数据库字段）
     */
    private String mushroomName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 驳回原因
     */
    private String rejectReason;
    /**
     * 该科普内容关联的所有图片列表
     */
    private List<Picture> pictureList;

    /**
     * 图文封面图URL
     */
    private String articleCoverUrl;

    /**
     * 视频封面图URL
     */
    private String videoCoverUrl;

    private Integer praiseCount;

    private Integer reviewCount;
    private Integer collectCount;
}