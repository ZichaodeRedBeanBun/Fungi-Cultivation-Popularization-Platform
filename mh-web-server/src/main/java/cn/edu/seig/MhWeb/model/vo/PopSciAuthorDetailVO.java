package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科普号详情VO（对应歌手的ArtistDetailVO）
 * @author su
 */
@Data
public class PopSciAuthorDetailVO {
    /**
     * 科普号ID
     */
    private Long userId;

    /**
     * 科普号名称
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 绑定邮箱
     */
    private String email;

    /**
     * 科普号类型：2=待认证，3=已认证
     */
    private Integer userType;

    /**
     * 所属单位
     */
    private String affiliateUnit;

    /**
     * 认证号码
     */
    private String certificateNum;

    /**
     * 认证申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime certApplyTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime certAuditTime;

    /**
     * 账号状态：1=正常，2=封禁
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}