package cn.edu.seig.MhWeb.controller.user;


import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantNodeVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantNodeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种植节点管理前端控制器（用户端）
 *
 * @author su
 */
@RestController
@Slf4j
@RequestMapping("/user/plantNode")
public class PlantNodeController {

    @Autowired
    private IPlantNodeService plantNodeService;

    /**
     * 根据计划ID查询种植节点列表
     */
    @GetMapping("/getPlantNodeListByPlanId/{planId}")
    public Result<List<PlantNodeVO>> getPlantNodeListByPlanId(@PathVariable("planId") Long planId) {
        log.info("进入getPlantNodeListByPlanId方法，计划ID：{}", planId);
        return plantNodeService.getNodeListByPlanId(planId);
    }

    /**
     * 新增种植节点
     */
    @PostMapping("/addPlantNode")
    public Result addPlantNode(@RequestBody @Valid PlantNodeAddDTO plantNodeAddDTO) {
        log.info("进入addPlantNode方法，参数：{}", plantNodeAddDTO);
        return plantNodeService.addPlantNode(plantNodeAddDTO);
    }

    /**
     * 更新种植节点
     */
    @PutMapping("/updatePlantNode")
    public Result updatePlantNode(@RequestBody @Valid PlantNodeUpdateDTO plantNodeUpdateDTO) {
        log.info("进入updatePlantNode方法，参数：{}", plantNodeUpdateDTO);
        return plantNodeService.updatePlantNode(plantNodeUpdateDTO);
    }

    /**
     * 标记节点为已完成
     */
    @PutMapping("/markNodeCompleted/{nodeId}")
    public Result markNodeCompleted(@PathVariable("nodeId") Long nodeId) {
        log.info("进入markNodeCompleted方法，节点ID：{}", nodeId);
        return plantNodeService.markNodeCompleted(nodeId);
    }

    /**
     * 删除单个种植节点
     */
    @DeleteMapping("/deletePlantNode/{nodeId}")
    public Result deletePlantNode(@PathVariable("nodeId") Long nodeId) {
        log.info("进入deletePlantNode方法，节点ID：{}", nodeId);
        return plantNodeService.deletePlantNode(nodeId);
    }

    /**
     * 批量删除种植节点
     */
    @DeleteMapping("/deletePlantNodes")
    public Result deletePlantNodes(@RequestBody List<Long> nodeIds) {
        log.info("进入deletePlantNodes方法，节点ID列表：{}", nodeIds);
        return plantNodeService.deletePlantNodes(nodeIds);
    }

}