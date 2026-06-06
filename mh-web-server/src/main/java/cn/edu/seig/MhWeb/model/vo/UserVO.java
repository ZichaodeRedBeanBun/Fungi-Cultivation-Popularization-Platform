package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户身份
     */
    private Integer userType;
    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String introduction;

    // 科普号专属字段（普通用户为null）
    private String certificateNum;
    private String affiliateUnit;
    private LocalDateTime certApplyTime;
    private LocalDateTime certAuditTime;
}
