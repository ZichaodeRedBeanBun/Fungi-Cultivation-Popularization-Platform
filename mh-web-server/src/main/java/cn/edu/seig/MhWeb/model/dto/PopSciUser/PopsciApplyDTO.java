package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普号认证申请DTO（普通用户→待认证科普号）
 */
@Data
public class PopsciApplyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

    /**
     * 科普号简介（可选，100字以内）
     */
    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    private String introduction;
}