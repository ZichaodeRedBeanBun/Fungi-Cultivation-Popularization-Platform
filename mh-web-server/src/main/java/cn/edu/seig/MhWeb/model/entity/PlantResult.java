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
 * 种植成果表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_plant_result")
public class PlantResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成果记录主键ID
     */
    @TableId(value = "result_id", type = IdType.AUTO)
    private Long resultId;

    /**
     * 关联种植计划ID（mh_plant_plan的plan_id）
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 采收时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("harvest_time")
    private LocalDateTime harvestTime;

    /**
     * 实际产量（kg）
     */
    @TableField("actual_yield")
    private BigDecimal actualYield;

    /**
     * 产量品质（特级/一级/普通）
     */
    @TableField("yield_grade")
    private String yieldGrade;

    /**
     * 实际收益（元）
     */
    @TableField("actual_income")
    private BigDecimal actualIncome;

    /**
     * 减产/损失原因（如病害/天气/操作不当）
     */
    @TableField("loss_reason")
    private String lossReason;

    /**
     * 计划总结（大数据智能生成/手动填写）
     */
    @TableField("summary")
    private String summary;

    /**
     * 成果记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 成果记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

}