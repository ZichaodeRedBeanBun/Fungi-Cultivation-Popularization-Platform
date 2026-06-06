package cn.edu.seig.MhWeb.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 种植数据记录表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_plant_data")
public class PlantData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据记录主键ID
     */
    @TableId(value = "data_id", type = IdType.AUTO)
    private Long dataId;

    /**
     * 关联种植计划ID（mh_plant_plan的plan_id）
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 数据记录时间（如2025-03-10 08:00:00）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 环境温度（℃）
     */
    @TableField("env_temp")
    private BigDecimal envTemp;

    /**
     * 环境湿度（%）
     */
    @TableField("env_humidity")
    private BigDecimal envHumidity;

    /**
     * CO₂浓度（‰，可选）
     */
    @TableField("co2_concentration")
    private BigDecimal co2Concentration;

    /**
     * 农事操作（如浇水5L/㎡；灭菌2小时）
     */
    @TableField("farming_oper")
    private String farmingOper;

    /**
     * 生长状态：1=优 2=良 3=差
     */
    @TableField("growth_status")
    private Integer growthStatus;

    /**
     * 病虫害情况（如无；少量褐腐病，已喷生物药剂）
     */
    @TableField("disease_status")
    private String diseaseStatus;

    /**
     * 当日投入成本（元）
     */
    @TableField("input_cost")
    private BigDecimal inputCost;

    /**
     * 记录人ID（关联mh_user的user_id）
     */
    @TableField("recorder_id")
    private Long recorderId;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

}