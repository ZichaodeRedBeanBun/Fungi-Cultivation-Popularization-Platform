package cn.edu.seig.MhWeb.model.dto.user;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理端：管理员更新任意用户信息的DTO（含科普号核心字段）
 */
@Data
public class UserAdminUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（必传，指定要修改的用户）
     */
    @NotNull(message = MessageConstant.USER_ID + MessageConstant.NOT_NULL)
    private Long userId;

    /**
     * 用户名（可选修改）
     */
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 绑定邮箱（可选修改）
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    /**
     * 用户简介（可选修改）
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    private String introduction;

    /**
     * 用户身份（可选修改：1=普通，2=待认证科普号，3=已认证科普号）
     */
    private UserTypeEnum userType;

    /**
     * 科普号认证号码（仅科普号修改）
     */
    private String certificateNum;

    /**
     * 科普号所属单位（仅科普号修改）
     */
    private String affiliateUnit;

    /**
     * 账号状态（可选修改）
     */
    @NotNull(message=MessageConstant.USER_STATUS_INVALID)
    private UserStatusEnum userStatus;
}