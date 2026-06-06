package cn.edu.seig.MhWeb.controller.user;

import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantPlanVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantPlanService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种植计划管理前端控制器
 *
 * @author su
 */
@RestController
@Slf4j
@RequestMapping("/user/plantPlan")
public class PlantPlanController {

    @Autowired
    private IPlantPlanService plantPlanService;

    /**
     * 获取种植计划分页列表（支持筛选执行中/所有计划）
     */
    @PostMapping("/getPlantPlanPage")
    public Result<PageResult<PlantPlanVO>> getPlantPlanPage(@RequestBody PlantPlanPageQueryDTO plantPlanPageQueryDTO) {
        log.info("进入getPlantPlanPage方法，参数：{}", plantPlanPageQueryDTO);
        return plantPlanService.getPlantPlanPage(plantPlanPageQueryDTO);
    }

    /**
     * 根据ID查询种植计划详情
     */
    @GetMapping("/getPlantPlanById/{planId}")
    public Result<PlantPlanVO> getPlantPlanById(@PathVariable("planId") Long planId) {
        log.info("进入getPlantPlanById方法，计划ID：{}", planId);
        return plantPlanService.getPlantPlanById(planId);
    }

    /**
     * 新增种植计划
     */
    @PostMapping("/addPlantPlan")
    public Result<Long> addPlantPlan(@RequestBody @Valid PlantPlanAddDTO plantPlanAddDTO) {
        log.info("进入addPlantPlan方法，参数：{}", plantPlanAddDTO);
        return plantPlanService.addPlantPlan(plantPlanAddDTO);
    }

    /**
     * 更新种植计划
     */
    @PutMapping("/updatePlantPlan")
    public Result<Void> updatePlantPlan(@RequestBody @Valid PlantPlanUpdateDTO plantPlanUpdateDTO) {
        log.info("进入updatePlantPlan方法，参数：{}", plantPlanUpdateDTO);
        return plantPlanService.updatePlantPlan(plantPlanUpdateDTO);
    }

    /**
     * 删除单个种植计划
     */
    @DeleteMapping("/deletePlantPlan/{planId}")
    public Result<Void> deletePlantPlan(@PathVariable("planId") Long planId) {
        log.info("进入deletePlantPlan方法，计划ID：{}", planId);
        return plantPlanService.deletePlantPlan(planId);
    }

    /**
     * 批量删除种植计划
     */
    @DeleteMapping("/deletePlantPlans")
    public Result<Void> deletePlantPlans(@RequestBody List<Long> planIds) {
        log.info("进入deletePlantPlans方法，计划ID列表：{}", planIds);
        return plantPlanService.deletePlantPlans(planIds);
    }

}