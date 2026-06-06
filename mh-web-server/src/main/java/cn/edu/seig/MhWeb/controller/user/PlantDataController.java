package cn.edu.seig.MhWeb.controller.user;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantDataVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantDataService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种植数据记录管理前端控制器（用户端）
 *
 * @author su
 */
@RestController
@Slf4j
@RequestMapping("/user/plantData")
public class PlantDataController {

    @Autowired
    private IPlantDataService plantDataService;

    /**
     * 获取种植数据分页列表（支持按计划ID/时间/生长状态筛选）
     */
    @PostMapping("/getPlantDataPage")
    public Result<PageResult<PlantDataVO>> getPlantDataPage(@RequestBody PlantDataPageQueryDTO plantDataPageQueryDTO) {
        log.info("进入getPlantDataPage方法，参数：{}", plantDataPageQueryDTO);
        return plantDataService.getPlantDataPage(plantDataPageQueryDTO);
    }

    /**
     * 根据ID查询种植数据详情
     */
    @GetMapping("/getPlantDataById/{dataId}")
    public Result<PlantDataVO> getPlantDataById(@PathVariable("dataId") Long dataId) {
        log.info("进入getPlantDataById方法，数据ID：{}", dataId);
        return plantDataService.getPlantDataById(dataId);
    }

    /**
     * 新增种植数据记录
     */
    @PostMapping("/addPlantData")
    public Result<Void> addPlantData(@RequestBody @Valid PlantDataAddDTO plantDataAddDTO) {
        log.info("进入addPlantData方法，参数：{}", plantDataAddDTO);
        return plantDataService.addPlantData(plantDataAddDTO);
    }

    /**
     * 更新种植数据记录
     */
    @PutMapping("/updatePlantData")
    public Result<Void> updatePlantData(@RequestBody @Valid PlantDataUpdateDTO plantDataUpdateDTO) {
        log.info("进入updatePlantData方法，参数：{}", plantDataUpdateDTO);
        return plantDataService.updatePlantData(plantDataUpdateDTO);
    }

    /**
     * 删除单个种植数据记录
     */
    @DeleteMapping("/deletePlantData/{dataId}")
    public Result<Void> deletePlantData(@PathVariable("dataId") Long dataId) {
        log.info("进入deletePlantData方法，数据ID：{}", dataId);
        return plantDataService.deletePlantData(dataId);
    }

    /**
     * 批量删除种植数据记录
     */
    @DeleteMapping("/deletePlantDatas")
    public Result<Void> deletePlantDatas(@RequestBody List<Long> dataIds) {
        log.info("进入deletePlantDatas方法，数据ID列表：{}", dataIds);
        return plantDataService.deletePlantDatas(dataIds);
    }

}