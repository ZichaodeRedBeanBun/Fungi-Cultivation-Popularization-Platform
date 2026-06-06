package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import cn.edu.seig.MhWeb.model.entity.Picture;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 科普内容发布/编辑DTO（用户端-科普号用户使用）
 * 仅负责内容本身的新增/修改，不包含审核相关字段
 *
 * @author su
 */
@Data
public class PopSciContentPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科普内容主键ID（修改时必填，新增时为空）
     */
    private Long id;

    /**
     * 关联菌种ID（必填）
     */
    @NotNull(message = "菌种ID不能为空")
    @Positive(message = "菌种ID必须为正整数")
    private Long mushroomId;

    /**
     * 内容类型（article-图文/ video-视频，必填）
     */
    @NotNull(message = "内容类型不能为空") // 修正：枚举类型用@NotNull，而非@NotBlank
    private PopSciContentTypeEnum contentType;

    /**
     * 标题（必填）
     */
    @NotBlank(message = "标题不能为空")
    @JsonAlias("theme")
    private String title;

    /**
     * 关联科普号ID（必填，当前登录科普号的ID）
     */
    @NotNull(message = "科普号ID不能为空")
    @Positive(message = "科普号ID必须为正整数")
    private Long authorId;

    /**
     * 发布日期（选填，默认填充当前日期）
     */
    private LocalDate publishDate;

    /**
     * 图文正文（图文类型必填）
     */
    private String content;

    /**
     * 视频播放链接（视频类型必填）
     */
    private String videoUrl;

    /**
     * 图文详情图URL（图文类型选填，编辑时替换用）
     */
    private List<String> articleDetailPicUrl;

    /**
     * 视频封面URL（视频类型选填，编辑时替换用）
     */
    private String videoCoverUrl;

    /**
     * 内容描述（选填）
     */
    private String description;

    // 自定义校验：图文类型必须填正文，视频类型必须填播放链接
    // @AssertTrue(message = "图文类型必须填写正文内容，视频类型必须填写播放链接")
//    public boolean isContentValid() {
//        if (contentType == PopSciContentTypeEnum.ARTICLE && (content == null || content.trim().isEmpty())) {
//            return false;
//        }
//        if (contentType == PopSciContentTypeEnum.VIDEO && (videoUrl == null || videoUrl.trim().isEmpty())) {
//            return false;
//        }
//        return true;
//    }
}