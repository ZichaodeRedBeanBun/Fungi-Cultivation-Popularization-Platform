package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 种植计划展示VO
 *
 * @author su
 */
@Data
public class PlantPlanVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 种植计划主键ID
     */
    private Long planId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 种植菌型（香菇/金针菇/羊肚菌等）
     */
    private String fungusType;

    /**
     * 种植面积（亩/㎡）
     */
    private BigDecimal plantArea;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 预期产量（kg）
     */
    private BigDecimal expectYield;

    /**
     * 预期收益（元）
     */
    private BigDecimal expectIncome;

    /**
     * 计划状态：1=执行中 2=已完成 3=暂停 4=作废
     */
    private PlantPlanStatusEnum status;


    /**
     * 计划备注
     */
    private String remark;

    /**
     * 计划创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 计划更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}