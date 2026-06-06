package cn.edu.seig.MhWeb.model.dto.plantPlan;

import cn.edu.seig.MhWeb.enumeration.PlantGrowthStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 种植数据分页查询DTO
 *
 * @author su
 */
@Data
public class PlantDataPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 关联种植计划ID（必填）
     */
    private Long planId;

    /**
     * 记录时间开始范围（如2025-01-01 00:00:00）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recordTime;

    /**
     * 生长状态：1=优 2=良 3=差（为空则查所有）
     */
    private PlantGrowthStatusEnum growthStatus;

    /**
     * 记录人ID（后台管理用，前端不传，由后端获取当前登录用户）
     */
    private Long recorderId;

}