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
 * 新增种植计划DTO
 *
 * @author su
 */
@Data
public class PlantPlanAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 计划名称（必填）
     */
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    /**
     * 种植菌型（必填）
     */
    @NotBlank(message = "种植菌型不能为空")
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
     * 计划状态：1=执行中 2=已完成 3=暂停 4=作废（默认1）
     */
    private PlantPlanStatusEnum status;

    /**
     * 计划备注（可选）
     */
    private String remark;

}