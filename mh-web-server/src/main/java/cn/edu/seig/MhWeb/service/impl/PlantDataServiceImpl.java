package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.mapper.PlantDataMapper;
import cn.edu.seig.MhWeb.mapper.PlantPlanMapper;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.PlantData;
import cn.edu.seig.MhWeb.model.entity.PlantPlan;
import cn.edu.seig.MhWeb.model.vo.PlantDataVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantDataService;
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
 * 种植数据业务实现类
 * （无缓存、完整权限校验、符合MyBatis-Plus IService规范）
 *
 * @author su
 */
@Slf4j
@Service
public class PlantDataServiceImpl extends ServiceImpl<PlantDataMapper, PlantData> implements IPlantDataService {

    @Autowired
    private PlantDataMapper plantDataMapper;

    @Autowired
    private PlantPlanMapper plantPlanMapper;

    /**
     * 转换PlantData实体为VO（统一VO转换逻辑）
     */
    private PlantDataVO convertToVO(PlantData plantData) {
        PlantDataVO vo = new PlantDataVO();
        BeanUtils.copyProperties(plantData, vo);

        // 补充计划名称（可选，根据VO结构调整）
        if (plantData.getPlanId() != null) {
            PlantPlan plan = plantPlanMapper.selectById(plantData.getPlanId().intValue());
        }
        return vo;
    }

    /**
     * 校验种植数据归属当前用户（核心权限方法）
     */
    private boolean checkDataBelongToCurrentUser(Long dataId) {
        PlantData existData = plantDataMapper.selectById(dataId);
        if (existData == null) {
            return false;
        }
        Long userId = BaseContext.getCurrentId();
        // 通过plan_id关联计划表，校验用户归属
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existData.getPlanId().intValue())
                .eq("user_id", userId);
        return plantPlanMapper.exists(planQueryWrapper);
    }

    /**
     * 校验计划归属当前用户（新增数据时用）
     */
    private boolean checkPlanBelongToCurrentUser(Long planId) {
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", planId)
                .eq("user_id", userId);
        return plantPlanMapper.exists(planQueryWrapper);
    }

    @Override
    public Result<PageResult<PlantDataVO>> getPlantDataPage(PlantDataPageQueryDTO plantDataPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取种植数据分页列表，参数：{}，当前用户ID：{}", plantDataPageQueryDTO, userId);

        // 1. 构建分页对象
        Page<PlantData> page = new Page<>(
                plantDataPageQueryDTO.getPageNum(),
                plantDataPageQueryDTO.getPageSize()
        );

        // 2. 构建查询条件（核心：仅查询当前用户的种植数据）
        QueryWrapper<PlantData> queryWrapper = new QueryWrapper<>();

        // 按计划ID筛选
        if (plantDataPageQueryDTO.getPlanId() != null) {
            // 先校验计划归属，避免查询他人计划的数据
            if (!checkPlanBelongToCurrentUser(plantDataPageQueryDTO.getPlanId())) {
                log.warn("查询种植数据失败：计划{}不属于当前用户", plantDataPageQueryDTO.getPlanId());
                return Result.success(new PageResult<>(0L, null));
            }
            queryWrapper.eq("plan_id", plantDataPageQueryDTO.getPlanId());
        }

        // 按时间范围筛选（可选，根据DTO字段调整）
        if (plantDataPageQueryDTO.getRecordTime() != null) {
            queryWrapper.ge("record_time", plantDataPageQueryDTO.getRecordTime());
        }

        // 按记录时间倒序
        queryWrapper.orderByDesc("create_time");

        // 3. 执行分页查询
        IPage<PlantData> dataPage = plantDataMapper.selectPage(page, queryWrapper);
        if (dataPage.getRecords().isEmpty()) {
            log.info("当前条件下无种植数据");
            return Result.success(new PageResult<>(0L, null));
        }

        // 4. 转换为VO列表
        List<PlantDataVO> voList = dataPage.getRecords().stream()
                .filter(data -> checkPlanBelongToCurrentUser(data.getPlanId())) // 二次过滤，确保数据归属
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 5. 封装分页结果
        return Result.success(new PageResult<>(dataPage.getTotal(), voList));
    }

    @Override
    public Result addPlantData(PlantDataAddDTO plantDataAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增种植数据，参数：{}，当前用户ID：{}", plantDataAddDTO, userId);

        // 1. 校验计划归属当前用户
        if (!checkPlanBelongToCurrentUser(plantDataAddDTO.getPlanId())) {
            log.warn("新增种植数据失败：计划{}不存在或不属于当前用户", plantDataAddDTO.getPlanId());
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST + "或无访问权限");
        }

        // 2. DTO转换为实体
        PlantData plantData = new PlantData();
        BeanUtils.copyProperties(plantDataAddDTO, plantData);

        // 3. 补充基础字段
        plantData.setCreateTime(LocalDateTime.now());
        plantData.setRecorderId(Long.valueOf(userId)); // 记录人ID

        // 4. 插入数据库
        plantDataMapper.insert(plantData);
        log.info("新增种植数据成功，数据ID：{}", plantData.getDataId());

        return Result.success();
    }

    @Override
    public Result updatePlantData(PlantDataUpdateDTO plantDataUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("更新种植数据，参数：{}，当前用户ID：{}", plantDataUpdateDTO, userId);

        // 1. 校验数据存在且归属当前用户
        Long dataId = plantDataUpdateDTO.getDataId();
        if (!checkDataBelongToCurrentUser(dataId)) {
            log.warn("更新种植数据失败：数据{}不存在或不属于当前用户", dataId);
            return Result.error(MessageConstant.PLANT_DATA + MessageConstant.NOT_EXIST + "或无修改权限");
        }

        // 2. 构建更新实体
        PlantData updateData = new PlantData();
        BeanUtils.copyProperties(plantDataUpdateDTO, updateData);

        // 3. 执行更新
        plantDataMapper.updateById(updateData);
        log.info("更新种植数据成功，数据ID：{}", dataId);

        return Result.success();
    }

    @Override
    public Result deletePlantData(Long dataId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除种植数据，数据ID：{}，当前用户ID：{}", dataId, userId);

        // 1. 校验数据存在且归属当前用户
        if (!checkDataBelongToCurrentUser(dataId)) {
            log.warn("删除种植数据失败：数据{}不存在或不属于当前用户", dataId);
            return Result.error(MessageConstant.PLANT_DATA + MessageConstant.NOT_EXIST + "或无删除权限");
        }

        // 2. 执行删除
        plantDataMapper.deleteById(dataId);
        log.info("删除种植数据成功，数据ID：{}", dataId);

        return Result.success();
    }

    @Override
    public Result<PlantDataVO> getPlantDataById(Long dataId) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询种植数据详情，数据ID：{}，当前用户ID：{}", dataId, userId);

        // 1. 校验数据存在且归属当前用户
        if (!checkDataBelongToCurrentUser(dataId)) {
            log.warn("查询种植数据失败：数据{}不存在或不属于当前用户", dataId);
            return Result.error(MessageConstant.PLANT_DATA + MessageConstant.NOT_EXIST + "或无访问权限");
        }

        // 2. 查询数据并转换为VO
        PlantData plantData = plantDataMapper.selectById(dataId);
        PlantDataVO vo = convertToVO(plantData);

        return Result.success(vo);
    }

    @Override
    public Result deletePlantDatas(List<Long> dataIds) {
        Long userId = BaseContext.getCurrentId();
        log.info("批量删除种植数据，数据ID列表：{}，当前用户ID：{}", dataIds, userId);

        // 1. 参数校验：ID列表不能为空
        if (dataIds == null || dataIds.isEmpty()) {
            log.warn("批量删除种植数据失败：数据ID列表为空");
            return Result.error(MessageConstant.PLANT_DATA + MessageConstant.ID + MessageConstant.NOT_NULL);
        }

        // 2. 校验所有数据存在且归属当前用户
        List<Long> invalidIds = dataIds.stream()
                .filter(id -> !checkDataBelongToCurrentUser(id))
                .collect(Collectors.toList());

        if (!invalidIds.isEmpty()) {
            log.warn("批量删除种植数据失败：部分数据不存在或无权限，无效ID列表：{}", invalidIds);
            return Result.error("数据ID：" + invalidIds + "不存在或无删除权限");
        }

        // 3. 执行批量删除
        plantDataMapper.deleteBatchIds(dataIds);
        log.info("批量删除种植数据成功，共删除{}条数据", dataIds.size());

        return Result.success();
    }
}