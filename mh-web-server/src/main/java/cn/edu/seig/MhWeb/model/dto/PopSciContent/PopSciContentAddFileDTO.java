package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 科普内容新增DTO（接收文件版）
 * 前端暂存的文件通过此DTO上传，而非直接传URL
 *
 * @author su
 */
@Data
public class PopSciContentAddFileDTO implements Serializable {

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
    @NotNull(message = "内容类型不能为空")
    private PopSciContentTypeEnum contentType;

    /**
     * 标题（必填）
     */
    @NotBlank(message = "标题不能为空")
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
     * 内容描述（选填）
     */
    private String description;
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

}