package cn.edu.seig.MhWeb.model.dto.PopSciContent;

import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普内容审核DTO（管理端-管理员使用）
 * 仅负责审核操作，不包含内容本身的编辑字段
 *
 * @author su
 */
@Data
public class PopSciContentAuditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科普内容主键ID（必填，指定要审核的内容）
     */
    @NotNull(message = "待审核的科普内容ID不能为空")
    private Long id;

    /**
     * 审核后状态（2=审核通过，3=审核驳回，必填）
     */
    @NotNull(message = "审核状态不能为空")
    private PopSciContentStatusEnum contentStatus;

    /**
     * 驳回原因（仅content_status=3时必填）
     */
    @NotBlank(message = "驳回时必须填写驳回原因", groups = RejectGroup.class)
    private String rejectReason;

    // 分组校验：仅驳回时校验驳回原因
    public interface RejectGroup {}

    // 自定义校验：审核状态只能是通过/驳回（排除待审核）
    // @AssertTrue(message = "审核状态只能选择“审核通过”或“审核驳回”")
    public boolean isStatusValid() {
        return contentStatus == PopSciContentStatusEnum.PASSED
                || contentStatus == PopSciContentStatusEnum.REJECTED;
    }
}