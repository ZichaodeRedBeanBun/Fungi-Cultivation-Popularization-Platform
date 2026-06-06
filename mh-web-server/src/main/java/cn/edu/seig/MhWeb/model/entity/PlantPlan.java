package cn.edu.seig.MhWeb.model.entity;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
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
 * 种植计划主表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_plant_plan")
public class PlantPlan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 种植计划主键ID
     */
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    /**
     * 所属用户ID（关联mh_user的user_id）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 计划名称（如2025春季香菇大棚种植计划）
     */
    @TableField("plan_name")
    private String planName;

    /**
     * 种植菌型（香菇/金针菇/羊肚菌等）
     */
    @TableField("fungus_type")
    private String fungusType;

    /**
     * 种植面积（亩/㎡，如2.5亩）
     */
    @TableField("plant_area")
    private BigDecimal plantArea;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 计划结束时间（执行中可空，完成后填充）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 预期产量（kg，大数据推荐值）
     */
    @TableField("expect_yield")
    private BigDecimal expectYield;

    /**
     * 预期收益（元，大数据推荐值）
     */
    @TableField("expect_income")
    private BigDecimal expectIncome;

    /**
     * 计划状态：1=执行中 2=已完成 3=暂停 4=作废
     */
    @TableField("status")
    private PlantPlanStatusEnum status;

    /**
     * 计划创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 计划更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 计划备注（如大棚3号，木屑配方）
     */
    @TableField("remark")
    private String remark;

}