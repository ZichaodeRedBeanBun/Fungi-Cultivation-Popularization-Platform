package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 种植成果展示VO
 *
 * @author su
 */
@Data
public class PlantResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成果记录主键ID
     */
    private Long resultId;

    /**
     * 关联种植计划ID
     */
    private Long planId;

    /**
     * 采收时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestTime;

    /**
     * 实际产量（kg）
     */
    private BigDecimal actualYield;

    /**
     * 产量品质（特级/一级/普通）
     */
    private String yieldGrade;

    /**
     * 实际收益（元）
     */
    private BigDecimal actualIncome;

    /**
     * 减产/损失原因
     */
    private String lossReason;

    /**
     * 计划总结
     */
    private String summary;

    /**
     * 成果记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 成果记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 扩展：对比预期值（从计划主表获取）
    private BigDecimal expectYield; // 预期产量
    private BigDecimal expectIncome; // 预期收益
    private String yieldCompare; // 产量对比（如“超出5kg”/“不足3kg”）
    private String incomeCompare; // 收益对比

}