package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import lombok.Data;

/**
 * 科普号分页查询DTO（对应歌手的ArtistDTO）
 * @author su
 */
@Data
public class PopSciAuthorDTO {
    /**
     * 分页参数-页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 分页参数-页大小（默认10）
     */
    private Integer pageSize = 10;

    /**
     * 科普号名称（模糊查询）
     */
    private String authorName;

    /**
     * 科普号类型：2=待认证，3=已认证
     */
    private Integer userType;

    /**
     * 认证号码
     */
    private String certificateNum;
}