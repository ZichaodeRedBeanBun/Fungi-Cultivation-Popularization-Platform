package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserManagementVO implements Serializable {

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
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 用户创建时间
     */
    private LocalDateTime createTime;

    /**
     * 用户修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户状态：0-启用，1-禁用
     */
    private UserStatusEnum userStatus;

    // 科普号专属字段
    private String certificateNum;
    private String affiliateUnit;
    private LocalDateTime certApplyTime;
    private LocalDateTime certAuditTime;
    private Integer certAuditorId; // 审核管理员ID
}
