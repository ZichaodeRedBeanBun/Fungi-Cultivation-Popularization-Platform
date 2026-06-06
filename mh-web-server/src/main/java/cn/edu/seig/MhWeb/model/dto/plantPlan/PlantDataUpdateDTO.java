package cn.edu.seig.MhWeb.model.dto.plantPlan;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新种植数据DTO
 *
 * @author su
 */
@Data
public class PlantDataUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据记录主键ID（必填）
     */
    @NotNull(message = "数据ID不能为空")
    private Long dataId;

    /**
     * 数据记录时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recordTime;

    /**
     * 环境温度（℃，可选）
     */
    private BigDecimal envTemp;

    /**
     * 环境湿度（%，可选）
     */
    private BigDecimal envHumidity;

    /**
     * CO₂浓度（‰，可选）
     */
    private BigDecimal co2Concentration;

    /**
     * 农事操作（可选）
     */
    private String farmingOper;

    /**
     * 生长状态：1=优 2=良 3=差（可选）
     */
    private PlantPlanStatusEnum growthStatus;

    /**
     * 病虫害情况（可选）
     */
    private String diseaseStatus;

    /**
     * 当日投入成本（元，可选）
     */
    private BigDecimal inputCost;

}