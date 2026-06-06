package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.result.PageResult;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 种植计划完整详情VO（包含节点/数据/成果）
 *
 * @author su
 */
@Data
public class PlantPlanDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 计划基础信息（主表数据）
     */
    private PlantPlanVO planInfo;

    /**
     * 该计划的所有节点列表
     */
    private List<PlantNodeVO> nodeList;

    /**
     * 该计划的日常种植数据（支持分页/按时间筛选）
     */
    private PageResult<PlantDataVO> dataPage;

    /**
     * 该计划的采收成果（无则为null）
     */
    private PlantResultVO resultInfo;

}