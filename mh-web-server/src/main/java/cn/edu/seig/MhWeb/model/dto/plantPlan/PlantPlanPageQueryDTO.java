package cn.edu.seig.MhWeb.model.dto.plantPlan;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 种植计划分页查询DTO
 *
 * @author su
 */
@Data
public class PlantPlanPageQueryDTO implements Serializable {

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
     * 计划名称（模糊查询）
     */
    private String planName;

    /**
     * 计划状态：1=执行中 2=已完成 3=暂停 4=作废（为空则查所有）
     */
    private PlantPlanStatusEnum status;

    /**
     * 用户ID（后台管理用，前端不传，由后端获取当前登录用户）
     */
    private Integer userId;

}