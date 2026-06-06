package cn.edu.seig.MhWeb.model.dto.user;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     * 用户名格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 用户身份：1=普通用户，2=待认证科普号，3=已认证科普号
     * 添加用户时必填，需指定初始类型
     */
    @NotNull(message = MessageConstant.USER_TYPE + MessageConstant.NOT_NULL)
    private Integer userType;

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
    private String introduction;

    /**
     * 科普号认证号码（必填）
     */
    @NotBlank(message = MessageConstant.CERTIFICATE_NUM + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,20}$", message = MessageConstant.CERTIFICATE_NUM + MessageConstant.FORMAT_ERROR)
    private String certificateNum;

    /**
     * 科普号所属单位（必填）
     */
    @NotBlank(message = MessageConstant.AFFILIATE_UNIT + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^.{2,50}$", message = MessageConstant.AFFILIATE_UNIT + MessageConstant.FORMAT_ERROR)
    private String affiliateUnit;
}
