package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantNodeVO;
import cn.edu.seig.MhWeb.model.entity.PlantNode;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 种植节点业务接口
 *
 * @author su
 */
public interface IPlantNodeService extends IService<PlantNode> {

    /**
     * 根据计划ID查询节点列表（按预计执行时间升序）
     */
    Result<List<PlantNodeVO>> getNodeListByPlanId(Long planId);

    /**
     * 新增种植节点
     */
    Result addPlantNode(PlantNodeAddDTO plantNodeAddDTO);

    /**
     * 更新种植节点
     */
    Result updatePlantNode(PlantNodeUpdateDTO plantNodeUpdateDTO);

    /**
     * 删除种植节点
     */
    Result deletePlantNode(Long nodeId);

    /**
     * 标记节点完成（自动填充实际完成时间+状态）
     */
    Result markNodeCompleted(Long nodeId);


    /**
     * 批量删除种植节点
     */
    Result deletePlantNodes(List<Long> nodeIds);

}