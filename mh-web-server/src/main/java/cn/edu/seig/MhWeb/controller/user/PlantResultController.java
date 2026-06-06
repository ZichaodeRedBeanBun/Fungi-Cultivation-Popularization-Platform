package cn.edu.seig.MhWeb.controller.user;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantResultVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantResultService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种植成果管理前端控制器（用户端）
 *
 * @author su
 */
@RestController
@Slf4j
@RequestMapping("/user/plantResult")
public class PlantResultController {

    @Autowired
    private IPlantResultService plantResultService;

    /**
     * 根据计划ID查询种植成果（一对一）
     */
    @GetMapping("/getPlantResultByPlanId/{planId}")
    public Result<PlantResultVO> getPlantResultByPlanId(@PathVariable("planId") Integer planId) {
        log.info("进入getPlantResultByPlanId方法，计划ID：{}", planId);
        return Result.success(plantResultService.getResultByPlanId(Long.valueOf(planId)));
    }

//    /**
//     * 根据ID查询种植成果详情
//     */
//    @GetMapping("/getPlantResultById/{resultId}")
//    public Result<PlantResultVO> getPlantResultById(@PathVariable("resultId") Long resultId) {
//        log.info("进入getPlantResultById方法，成果ID：{}", resultId);
//        return plantResultService.getPlantResultById(resultId);
//    }

    /**
     * 新增种植成果
     */
    @PostMapping("/addPlantResult")
    public Result<Void> addPlantResult(@RequestBody @Valid PlantResultAddDTO plantResultAddDTO) {
        log.info("进入addPlantResult方法，参数：{}", plantResultAddDTO);
        return plantResultService.addPlantResult(plantResultAddDTO);
    }

    /**
     * 更新种植成果
     */
    @PutMapping("/updatePlantResult")
    public Result<Void> updatePlantResult(@RequestBody @Valid PlantResultUpdateDTO plantResultUpdateDTO) {
        log.info("进入updatePlantResult方法，参数：{}", plantResultUpdateDTO);
        return plantResultService.updatePlantResult(plantResultUpdateDTO);
    }

    /**
     * 删除单个种植成果
     */
    @DeleteMapping("/deletePlantResult/{resultId}")
    public Result<Void> deletePlantResult(@PathVariable("resultId") Long resultId) {
        log.info("进入deletePlantResult方法，成果ID：{}", resultId);
        return plantResultService.deletePlantResult(resultId);
    }

//    /**
//     * 批量删除种植成果
//     */
//    @DeleteMapping("/deletePlantResults")
//    public Result<Void> deletePlantResults(@RequestBody List<Long> resultIds) {
//        log.info("进入deletePlantResults方法，成果ID列表：{}", resultIds);
//        return plantResultService.deletePlantResults(resultIds);
//    }

}