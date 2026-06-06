package cn.edu.seig.MhWeb.model.dto.user;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     * 用户名格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 用户密码
     * 密码格式：8-18 位数字、字母、符号的任意两种组合
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{8,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    private String password;

    /**
     * 用户身份：1=普通用户，2=待认证科普号，3=已认证科普号
     * 添加用户时必填，需指定初始类型
     */
    @NotNull(message = MessageConstant.USER_TYPE + MessageConstant.NOT_NULL)
    private UserTypeEnum userType;

    /**
     * 用户邮箱
     */
    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    /**
     * 用户简介
     * 用户简介格式：100 字以内
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    @TableField("intro")
    private String introduction;

    /**
     * 用户状态：0-启用，1-禁用
     */
    @NotNull(message = MessageConstant.NOT_NULL+MessageConstant.USER_STATUS_INVALID)
    @TableField("status")
    private UserStatusEnum userStatus;
    /**
     * 科普号字段
     */
    /**
     * 科普号认证号码（非必填）
     */
    private String certificateNum;

    /**
     * 科普号所属单位（非必填）
     */
    private String affiliateUnit;

}
