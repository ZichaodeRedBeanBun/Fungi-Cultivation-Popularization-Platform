package cn.edu.seig.MhWeb.model.dto.plantPlan;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新种植计划DTO
 *
 * @author su
 */
@Data
public class PlantPlanUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 种植计划主键ID（必填）
     */
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    /**
     * 计划名称（可选，更新时非必填）
     */
    private String planName;

    /**
     * 种植菌型（可选）
     */
    private String fungusType;

    /**
     * 种植面积（亩/㎡，可选）
     */
    private BigDecimal plantArea;

    /**
     * 计划开始时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 计划结束时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 预期产量（kg，可选）
     */
    private BigDecimal expectYield;

    /**
     * 预期收益（元，可选）
     */
    private BigDecimal expectIncome;

    /**
     * 计划状态：1=执行中 2=已完成 3=暂停 4=作废（可选）
     */
    private PlantPlanStatusEnum status;

    /**
     * 计划备注（可选）
     */
    private String remark;

}