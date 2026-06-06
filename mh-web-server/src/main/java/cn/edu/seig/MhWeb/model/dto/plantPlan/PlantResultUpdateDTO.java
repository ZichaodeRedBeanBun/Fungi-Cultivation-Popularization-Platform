package cn.edu.seig.MhWeb.model.dto.plantPlan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新种植成果DTO
 *
 * @author su
 */
@Data
public class PlantResultUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成果记录主键ID（必填）
     */
    @NotNull(message = "成果ID不能为空")
    private Long resultId;

    /**
     * 采收时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime harvestTime;

    /**
     * 实际产量（kg，可选）
     */
    private BigDecimal actualYield;

    /**
     * 产量品质（特级/一级/普通，可选）
     */
    private String yieldGrade;

    /**
     * 实际收益（元，可选）
     */
    private BigDecimal actualIncome;

    /**
     * 减产/损失原因（可选）
     */
    private String lossReason;

    /**
     * 计划总结（可选）
     */
    private String summary;

}