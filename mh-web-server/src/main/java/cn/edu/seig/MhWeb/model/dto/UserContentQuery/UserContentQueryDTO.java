package cn.edu.seig.MhWeb.model.dto.UserContentQuery;

import cn.edu.seig.MhWeb.enumeration.CommentTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import lombok.Data;

/**
 * 用户个人主页内容查询DTO（适配前端参数）
 */
@Data
public class UserContentQueryDTO {
    private Long userId;
    
    /**
     * 媒体类型：imageText=图文，video=视频
     */
    private PopSciContentTypeEnum  mediaType;
    
    /**
     * 分页参数（可选，前端列表需分页）
     */
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}