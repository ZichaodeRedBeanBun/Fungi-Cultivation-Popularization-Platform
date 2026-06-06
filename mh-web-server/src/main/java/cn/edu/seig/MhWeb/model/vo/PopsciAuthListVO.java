package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科普号认证申请列表VO
 */
@Data
public class PopsciAuthListVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private String email;
    private String certificateNum;
    private String affiliateUnit;
    private LocalDateTime certApplyTime;
    private String auditRemark; // 审核备注
}