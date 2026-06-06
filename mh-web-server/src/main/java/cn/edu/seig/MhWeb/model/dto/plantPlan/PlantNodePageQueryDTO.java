package cn.edu.seig.MhWeb.model.dto.plantPlan;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 种植节点分页查询DTO
 *
 * @author su
 */
@Data
public class PlantNodePageQueryDTO implements Serializable {

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
     * 关联种植计划ID
     */
    private Integer planId;

    /**
     * 节点类型（备料/灭菌/接种/发菌/出菇/采收）
     */
    private String nodeType;

    /**
     * 节点状态：1=未完成 2=已完成 3=逾期未完成
     */
    private Integer status;

}