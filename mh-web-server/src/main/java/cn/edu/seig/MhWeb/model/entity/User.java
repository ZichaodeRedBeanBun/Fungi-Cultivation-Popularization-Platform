package cn.edu.seig.MhWeb.model.entity;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户+科普号信息表实体类
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_user") // 匹配数据库表名 mh_user（原 tb_user 修正）
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户主键ID（匹配数据库 user_id）
     */
    @TableId(value = "user_id", type = IdType.AUTO) // 主键字段映射+自增策略
    private Long userId; // 数据库是 int 类型，用 Integer 更适配（原 Long 修正）

    /**
     * 用户名（普通用户/科普号通用）
     * 格式：4-16位字符（字母、数字、下划线、连字符）
     */
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @TableField("username")
    private String username;

    /**
     * 密码（MD5加密存储）
     * 格式：8-18 位数字、字母、符号的任意两种组合
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{8,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @TableField("password")
    private String password;

    /**
     * 用户头像URL（MinIO存储路径，匹配数据库 avatar）
     */
    @TableField("avatar")
    private String avatar;
    /**
     * 用户简介
     * 用户简介格式：100 字以内
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    @TableField("intro")
    private String introduction;


    /**
     * 绑定邮箱（登录/通知用，数据库允许null，移除@NotBlank）
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @TableField("email")
    private String email;

    /**
     * 用户身份：1=普通用户，2=待认证科普号，3=已认证科普号
     * 数据库是 tinyint(1)，用 Integer 适配
     */
    @TableField("user_type")
    private UserTypeEnum userType;

    /**
     * 科普号认证号码（仅待认证/已认证科普号填写）
     */
    @TableField("certificate_num")
    private String certificateNum;

    /**
     * 科普号所属单位（仅待认证/已认证科普号填写）
     */
    @TableField("affiliate_unit")
    private String affiliateUnit;

    /**
     * 科普号认证申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("cert_apply_time")
    private LocalDateTime certApplyTime;

    /**
     * 管理员审核科普号的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("cert_audit_time")
    private LocalDateTime certAuditTime;

    /**
     * 审核管理员ID（关联admin表的admin_id）
     */
    @TableField("cert_auditor_id")
    private Long certAuditorId;

    /**
     * 账号状态：1=正常，2=封禁（普通用户/科普号通用）
     * 数据库是 tinyint(1)，用 Integer 适配（若需枚举可参考下方说明）
     */
    @TableField("status")
    private UserStatusEnum userStatus;

    /**
     * 用户创建时间（匹配数据库 create_time）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
    /**
     * 用户修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;


}