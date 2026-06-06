package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantPlanUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantPlanDetailVO;
import cn.edu.seig.MhWeb.model.vo.PlantPlanVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.seig.MhWeb.model.entity.PlantPlan;

import java.util.List;

/**
 * 种植计划业务接口
 *
 * @author su
 */
public interface IPlantPlanService extends IService<PlantPlan> {

    /**
     * 获取种植计划分页列表（支持按状态/菌型/名称筛选）
     *
     * @param plantPlanPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    Result<PageResult<PlantPlanVO>> getPlantPlanPage(PlantPlanPageQueryDTO plantPlanPageQueryDTO);

    /**
     * 新增种植计划
     *
     * @param plantPlanAddDTO 新增参数
     * @return 操作结果
     */
    Result<Long> addPlantPlan(PlantPlanAddDTO plantPlanAddDTO);

    /**
     * 更新种植计划
     *
     * @param plantPlanUpdateDTO 更新参数
     * @return 操作结果
     */
    Result<Void> updatePlantPlan(PlantPlanUpdateDTO plantPlanUpdateDTO);

    /**
     * 删除单个种植计划
     *
     * @param planId 计划ID
     * @return 操作结果
     */
    Result<Void> deletePlantPlan(Long planId);

    /**
     * 批量删除种植计划
     *
     * @param planIds 计划ID列表
     * @return 操作结果
     */
    Result<Void> deletePlantPlans(List<Long> planIds);

    /**
     * 根据ID查询种植计划详情
     *
     * @param planId 计划ID
     * @return 计划详情VO
     */
    Result<PlantPlanVO> getPlantPlanById(Long planId);

    /**
     * 获取计划完整详情（含节点/数据/成果）
     * @param planId 计划ID
     * @param dataPageQueryDTO 数据分页条件（页码/条数/时间范围）
     * @return 完整详情VO
     */
    Result<PlantPlanDetailVO> getPlantPlanFullDetail(Long planId, PlantDataPageQueryDTO dataPageQueryDTO);
}