package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

/**
 * 科普号ID+名称VO
 * @author su
 */
@Data
public class PopSciAuthorNameVO {
    /**
     * 科普号ID
     */
    private Long userId;

    /**
     * 科普号名称
     */
    private String authorName;

    /**
     * 头像URL
     */
    private String avatar;
}