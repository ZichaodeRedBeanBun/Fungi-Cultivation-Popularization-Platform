package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.mapper.PlantPlanMapper;
import cn.edu.seig.MhWeb.model.dto.plantPlan.*;
import cn.edu.seig.MhWeb.model.entity.PlantPlan;
import cn.edu.seig.MhWeb.model.vo.*;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantDataService;
import cn.edu.seig.MhWeb.service.IPlantNodeService;
import cn.edu.seig.MhWeb.service.IPlantPlanService;
import cn.edu.seig.MhWeb.service.IPlantResultService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 种植计划业务实现类
 * （已移除缓存、替换为状态枚举）
 *
 * @author su
 */
@Slf4j
@Service
// 1. 移除缓存配置注解
// @CacheConfig(cacheNames = "plantPlanCache")
public class PlantPlanServiceImpl extends ServiceImpl<PlantPlanMapper, PlantPlan> implements IPlantPlanService {

    @Autowired
    private PlantPlanMapper plantPlanMapper;

    // 2. 注入缺失的子表Service
    @Autowired
    private IPlantNodeService plantNodeService;
    @Autowired
    private IPlantDataService plantDataService;
    @Autowired
    private IPlantResultService plantResultService;

    /**
     * 获取种植计划分页列表（移除缓存注解）
     */
    @Override
    // 移除缓存注解
    // @Cacheable(key = "#plantPlanPageQueryDTO.pageNum + '-' + #plantPlanPageQueryDTO.pageSize + '-' + #plantPlanPageQueryDTO.status + '-' + #plantPlanPageQueryDTO.planName + '-' + getCurrentUserId()")
    public Result<PageResult<PlantPlanVO>> getPlantPlanPage(PlantPlanPageQueryDTO plantPlanPageQueryDTO) {
        log.info("获取种植计划分页列表，参数：{}", plantPlanPageQueryDTO);

        // 1. 构建分页对象
        Page<PlantPlan> page = new Page<>(plantPlanPageQueryDTO.getPageNum(), plantPlanPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        // 2. 构建查询条件（仅查询当前用户的计划）
        QueryWrapper<PlantPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 模糊查询计划名称
        if (StringUtils.hasText(plantPlanPageQueryDTO.getPlanName())) {
            queryWrapper.like("plan_name", plantPlanPageQueryDTO.getPlanName());
        }

        // 按状态筛选（适配“执行中”和“所有计划”页面）
        if (plantPlanPageQueryDTO.getStatus() != null) {
            queryWrapper.eq("status", plantPlanPageQueryDTO.getStatus().getId());
        }

        // 按更新时间倒序
        queryWrapper.orderByDesc("update_time");

        // 3. 执行分页查询
        IPage<PlantPlan> planPage = plantPlanMapper.selectPage(page, queryWrapper);
        if (planPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 4. 转换为VO并补充状态名称（使用枚举）
        List<PlantPlanVO> planVOList = planPage.getRecords().stream()
                .map(plantPlan -> {
                    PlantPlanVO planVO = new PlantPlanVO();
                    BeanUtils.copyProperties(plantPlan, planVO);
                    // 枚举替换硬编码状态名称
                    planVO.setStatus(plantPlan.getStatus());
                    return planVO;
                }).collect(Collectors.toList());

        // 5. 封装分页结果
        return Result.success(new PageResult<>(planPage.getTotal(), planVOList));
    }

    /**
     * 新增种植计划（移除缓存注解）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantPlanCache", allEntries = true)
    public Result<Long> addPlantPlan(PlantPlanAddDTO plantPlanAddDTO) {
        log.info("新增种植计划，参数：{}", plantPlanAddDTO);

        // 1. DTO转换为实体
        PlantPlan plantPlan = new PlantPlan();
        BeanUtils.copyProperties(plantPlanAddDTO, plantPlan);
        Long userId = BaseContext.getCurrentId();
        // 2. 设置关联字段
        plantPlan.setUserId(userId);
        log.info("从BaseContext获取到的用户ID：{}", userId);
        plantPlan.setCreateTime(LocalDateTime.now());
        plantPlan.setUpdateTime(LocalDateTime.now());

        // 3. 插入数据库
        plantPlanMapper.insert(plantPlan);
        log.info("种植计划新增成功，生成的planId：{}", plantPlan.getPlanId()); // 关键！确认入库的planId

        return Result.success(MessageConstant.ADD+MessageConstant.SUCCESS,plantPlan.getPlanId());
    }

    /**
     * 更新种植计划（移除缓存注解）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantPlanCache", allEntries = true)
    public Result updatePlantPlan(PlantPlanUpdateDTO plantPlanUpdateDTO) {
        log.info("更新种植计划，参数：{}", plantPlanUpdateDTO);

        // 1. 校验计划存在且归属当前用户
        Long planId = plantPlanUpdateDTO.getPlanId();
        Long userId = BaseContext.getCurrentId();
        PlantPlan existPlan = plantPlanMapper.selectOne(
                new QueryWrapper<PlantPlan>()
                        .eq("plan_id", planId)
                        .eq("user_id", userId)
        );
        if (existPlan == null) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }

        // 2. 构建更新实体（仅更新非空字段）
        PlantPlan updatePlan = new PlantPlan();
        BeanUtils.copyProperties(plantPlanUpdateDTO, updatePlan);
        updatePlan.setUpdateTime(LocalDateTime.now());

        // 3. 执行更新
        plantPlanMapper.updateById(updatePlan);

        return Result.success();
    }

    /**
     * 删除单个种植计划（移除缓存注解）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantPlanCache", allEntries = true)
    public Result deletePlantPlan(Long planId) {
        log.info("删除种植计划，计划ID：{}", planId);
        Long userId = BaseContext.getCurrentId();
        // 1. 校验计划存在且归属当前用户
        PlantPlan existPlan = plantPlanMapper.selectOne(
                new QueryWrapper<PlantPlan>()
                        .eq("plan_id", planId)
                        .eq("user_id", userId)
        );
        if (existPlan == null) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }

        // 2. 执行删除（外键级联删除关联的种植数据/节点/成果）
        plantPlanMapper.deleteById(planId);

        return Result.success();
    }

    /**
     * 批量删除种植计划（移除缓存注解）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantPlanCache", allEntries = true)
    public Result deletePlantPlans(List<Long> planIds) {
        log.info("批量删除种植计划，计划ID列表：{}", planIds);

        if (planIds.isEmpty()) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.ID + MessageConstant.NOT_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        // 1. 校验计划归属当前用户
        QueryWrapper<PlantPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("plan_id", planIds)
                .eq("user_id", userId);
        List<PlantPlan> existPlans = plantPlanMapper.selectList(queryWrapper);
        if (existPlans.size() != planIds.size()) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }

        // 2. 批量删除
        plantPlanMapper.deleteBatchIds(planIds);

        return Result.success();
    }

    /**
     * 根据ID查询种植计划详情（移除缓存注解）
     */
    @Override
    // 移除缓存注解
    // @Cacheable(key = "#planId + '-' + getCurrentUserId()")
    public Result<PlantPlanVO> getPlantPlanById(Long planId) {
        log.info("查询种植计划详情，计划ID：{}", planId);
        Long userId = BaseContext.getCurrentId();
        // 1. 校验计划存在且归属当前用户
        PlantPlan existPlan = plantPlanMapper.selectOne(
                new QueryWrapper<PlantPlan>()
                        .eq("plan_id", planId)
                        .eq("user_id", userId)
        );
        if (existPlan == null) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }

        // 2. 转换为VO并补充状态名称（使用枚举）
        PlantPlanVO planVO = new PlantPlanVO();
        BeanUtils.copyProperties(existPlan, planVO);
        planVO.setStatus(existPlan.getStatus());

        return Result.success(planVO);
    }

    /**
     * 查询计划完整详情（修复planId类型转换、补充枚举使用）
     */
    @Override
    public Result<PlantPlanDetailVO> getPlantPlanFullDetail(Long planId, PlantDataPageQueryDTO dataPageQueryDTO) {
        log.info("查询计划完整详情，计划ID：{}，数据分页参数：{}", planId, dataPageQueryDTO);
        Long userId = BaseContext.getCurrentId();
        // 1. 修复类型转换：Long转Long（适配getPlantPlanById的参数类型）
        Result<PlantPlanVO> planResult = getPlantPlanById(planId);
        if (planResult == null || planResult.getData() == null) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }
        PlantPlanVO planVO = planResult.getData();

        // 2. 设置数据查询的planId和用户ID
        dataPageQueryDTO.setPlanId(planId);
        // 修复类型：getCurrentUserId()是Integer，转Long适配recorderId
        dataPageQueryDTO.setRecorderId(userId);

        // 3. 查询该计划的所有节点（按预计执行时间排序）
        Result<List<PlantNodeVO>> nodeResult = plantNodeService.getNodeListByPlanId(planId);
        List<PlantNodeVO> nodeList = nodeResult.getData() == null ? List.of() : nodeResult.getData();

        // 4. 查询该计划的日常数据（分页+按时间筛选）
        Result<PageResult<PlantDataVO>> dataResult = plantDataService.getPlantDataPage(dataPageQueryDTO);
        PageResult<PlantDataVO> dataPage = dataResult.getData() == null ? new PageResult<>(0L, List.of()) : dataResult.getData();

        // 5. 查询该计划的成果（一对一）
        PlantResultVO resultVO = plantResultService.getResultByPlanId(planId);

        // 6. 封装完整VO
        PlantPlanDetailVO detailVO = new PlantPlanDetailVO();
        detailVO.setPlanInfo(planVO);
        detailVO.setNodeList(nodeList);
        detailVO.setDataPage(dataPage);
        detailVO.setResultInfo(resultVO);

        return Result.success(detailVO);
    }
}