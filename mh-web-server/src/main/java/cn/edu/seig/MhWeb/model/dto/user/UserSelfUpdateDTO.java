package cn.edu.seig.MhWeb.model.dto.user;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户端：登录用户更新自身基础信息的DTO（普通/科普号用户通用）
 */
@Data
public class UserSelfUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（可选修改，传null/不传则不更新）
     * 格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 绑定邮箱（可选修改，传null/不传则不更新）
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    /**
     * 用户简介（可选修改，传null/不传则不更新）
     * 格式：100字以内
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    private String introduction;
}