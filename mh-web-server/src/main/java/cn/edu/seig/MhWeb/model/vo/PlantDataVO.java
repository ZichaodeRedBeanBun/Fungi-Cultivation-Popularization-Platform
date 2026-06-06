package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 种植数据展示VO
 *
 * @author su
 */
@Data
public class PlantDataVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据记录主键ID
     */
    private Long dataId;

    /**
     * 关联种植计划ID
     */
    private Long planId;

    /**
     * 数据记录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    /**
     * 环境温度（℃）
     */
    private BigDecimal envTemp;

    /**
     * 环境湿度（%）
     */
    private BigDecimal envHumidity;

    /**
     * CO₂浓度（‰）
     */
    private BigDecimal co2Concentration;

    /**
     * 农事操作
     */
    private String farmingOper;

    /**
     * 生长状态：1=优 2=良 3=差
     */
    private Integer growthStatus;

    /**
     * 生长状态名称（前端展示用）
     */
    private String growthStatusName;

    /**
     * 病虫害情况
     */
    private String diseaseStatus;

    /**
     * 当日投入成本（元）
     */
    private BigDecimal inputCost;

    /**
     * 记录人ID
     */
    private Long recorderId;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}