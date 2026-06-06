package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普号认证审核DTO（管理员操作）
 */
@Data
public class PopsciAuditDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待审核用户ID（必填）
     */
    @NotNull(message = MessageConstant.USER_ID + MessageConstant.NOT_NULL)
    private Integer userId; // 对齐User实体的Integer类型

    /**
     * 用户身份：1=普通用户，2=待认证科普号，3=已认证科普号
     * 数据库是 tinyint(1)，用 Integer 适配
     */
    @TableField("user_type")
    private UserTypeEnum userType;
    /**
     * 审核状态：1=通过，0=驳回（必填）
     */
    @NotNull(message = MessageConstant.AUDIT_STATUS + MessageConstant.NOT_NULL)
    private Integer auditStatus;
    /**
     * 审核备注（可选，100字以内）
     */
    private String auditRemark;
}