package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * 科普内容新增DTO
 *
 * @author su
 */
@Data
public class PopSciContentAddDTO {

    /**
     * 关联菌种ID（必填）
     */
    @NotNull(message = "关联菌种ID不能为空")
    @Positive(message = "菌种ID必须为正整数")
    private Long mushroomId; // 统一为Long，适配Service层ID类型

    /**
     * 内容类型：article-图文，video-视频（必填）
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 标题（必填）
     */
    @NotBlank(message = "科普内容标题不能为空")
    private String title;

    /**
     * 关联科普号ID（必填）
     */
    @NotNull(message = "科普号ID不能为空")
    @Positive(message = "科普号ID必须为正整数")
    private Long authorId;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 图文正文（图文类型必填）
     */
    private String content;


    /**
     * 内容描述
     */
    private String description;


    // 自定义校验：图文类型必须填正文，视频类型必须填播放链接
    // 需配合全局校验器生效（也可在Service层校验）
    // @AssertTrue(message = "图文类型必须填写正文内容")
//    public boolean isContentValid() {
//        if ("article".equals(contentType) && (content == null || content.trim().isEmpty())) {
//            return false;
//        }
//        if ("video".equals(contentType) && (videoUrl == null || videoUrl.trim().isEmpty())) {
//            return false;
//        }
//        return true;
//    }
}