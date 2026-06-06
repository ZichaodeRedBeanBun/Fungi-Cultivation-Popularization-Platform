package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import cn.edu.seig.MhWeb.mapper.PlantPlanMapper;
import cn.edu.seig.MhWeb.mapper.PlantResultMapper;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.PlantPlan;
import cn.edu.seig.MhWeb.model.entity.PlantResult;
import cn.edu.seig.MhWeb.model.vo.PlantResultVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantResultService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// 1. 新增BaseContext导入（请确认实际包路径，若不一致请自行调整）
import cn.edu.seig.MhWeb.context.BaseContext;

/**
 * 种植成果业务实现类
 * （已移除缓存、补充删除方法、修正查询成果的SQL条件错误、统一使用BaseContext获取用户ID）
 *
 * @author su
 */
@Slf4j
@Service
// 移除缓存配置注解
// @CacheConfig(cacheNames = "plantResultCache")
public class PlantResultServiceImpl extends ServiceImpl<PlantResultMapper, PlantResult> implements IPlantResultService {

    @Autowired
    private PlantResultMapper plantResultMapper;

    @Autowired
    private PlantPlanMapper plantPlanMapper;

    /**
     * 根据计划ID查询种植成果（修正SQL条件错误，移除缓存，改用BaseContext获取用户ID）
     */
    @Override
    // 移除缓存注解
    // @Cacheable(key = "#planId + '-' + getCurrentUserId()")
    public PlantResultVO getResultByPlanId(Long planId) {
        // 2. 改用BaseContext获取用户ID（Long类型）
        Long userId = BaseContext.getCurrentId();
        log.info("查询计划{}的种植成果，当前用户ID：{}", planId, userId);

        // 1. 修正SQL条件：根据plan_id（外键）查询成果，而非主键id
        QueryWrapper<PlantResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", planId);
        PlantResult result = plantResultMapper.selectOne(queryWrapper);
        if (result == null) {
            log.warn("计划{}暂无种植成果数据", planId);
            return null;
        }

        // 2. 转换为VO
        PlantResultVO vo = new PlantResultVO();
        BeanUtils.copyProperties(result, vo);

        // 3. 补充预期值和对比信息（关联计划表）
        // 适配planId类型：Long转Integer，按指定写法构造QueryWrapper校验user_id
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", planId.intValue())
                .eq("user_id", userId);
        PlantPlan plan = plantPlanMapper.selectOne(planQueryWrapper);

        if (plan != null) {
            vo.setExpectYield(plan.getExpectYield());
            vo.setExpectIncome(plan.getExpectIncome());

            // 产量对比（空值校验，避免NPE）
            if (result.getActualYield() != null && plan.getExpectYield() != null) {
                BigDecimal yieldDiff = result.getActualYield().subtract(plan.getExpectYield());
                vo.setYieldCompare(yieldDiff.compareTo(BigDecimal.ZERO) >= 0 ?
                        "超出" + yieldDiff + "kg" : "不足" + yieldDiff.abs() + "kg");
            } else {
                vo.setYieldCompare("数据不全，无法对比");
            }

            // 收益对比（空值校验，避免NPE）
            if (plan.getExpectIncome() != null && result.getActualIncome() != null) {
                BigDecimal incomeDiff = result.getActualIncome().subtract(plan.getExpectIncome());
                vo.setIncomeCompare(incomeDiff.compareTo(BigDecimal.ZERO) >= 0 ?
                        "超出" + incomeDiff + "元" : "不足" + incomeDiff.abs() + "元");
            } else {
                vo.setIncomeCompare("数据不全，无法对比");
            }
        }

        return vo;
    }

    /**
     * 新增种植成果（移除缓存注解，改用BaseContext获取用户ID）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantResultCache", allEntries = true)
    public Result<Void> addPlantResult(PlantResultAddDTO plantResultAddDTO) {
        // 3. 改用BaseContext获取用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("新增种植成果，参数：{}，当前用户ID：{}", plantResultAddDTO, userId);

        // 1. 校验计划归属当前用户（按指定写法构造QueryWrapper）
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", plantResultAddDTO.getPlanId().intValue())
                .eq("user_id", userId);
        PlantPlan plan = plantPlanMapper.selectOne(planQueryWrapper);

        if (plan == null) {
            return Result.error(MessageConstant.PLANT_PLAN + MessageConstant.NOT_EXIST);
        }

        // 2. 校验计划状态为“已完成”（使用枚举，避免硬编码）
        if (!PlantPlanStatusEnum.COMPLETED.getId().equals(plan.getStatus())) {
            return Result.error("仅状态为【" + PlantPlanStatusEnum.COMPLETED.getStatusDesc() + "】的计划可填写成果");
        }

        // 3. 校验该计划是否已存在成果（避免重复添加）
        QueryWrapper<PlantResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", plantResultAddDTO.getPlanId());
        PlantResult existResult = plantResultMapper.selectOne(queryWrapper);
        if (existResult != null) {
            return Result.error("该计划已填写种植成果，不可重复添加");
        }

        // 4. DTO转实体
        PlantResult result = new PlantResult();
        BeanUtils.copyProperties(plantResultAddDTO, result);
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());

        // 5. 插入数据库
        plantResultMapper.insert(result);

        return Result.success();
    }

    /**
     * 更新种植成果（移除缓存注解，改用BaseContext获取用户ID）
     */
    @Override
    // 移除缓存清除注解
    // @CacheEvict(cacheNames = "plantResultCache", allEntries = true)
    public Result<Void> updatePlantResult(PlantResultUpdateDTO plantResultUpdateDTO) {
        // 4. 改用BaseContext获取用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("更新种植成果，参数：{}，当前用户ID：{}", plantResultUpdateDTO, userId);

        // 1. 校验成果存在
        PlantResult existResult = plantResultMapper.selectById(plantResultUpdateDTO.getResultId());
        if (existResult == null) {
            return Result.error(MessageConstant.PLANT_RESULT + MessageConstant.NOT_EXIST);
        }

        // 2. 校验成果归属当前用户（按指定写法构造QueryWrapper）
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existResult.getPlanId().intValue())
                .eq("user_id", userId);
        PlantPlan plan = plantPlanMapper.selectOne(planQueryWrapper);

        if (plan == null) {
            return Result.error("无权限修改该种植成果");
        }

        // 3. 转换为实体并更新时间
        PlantResult updateResult = new PlantResult();
        BeanUtils.copyProperties(plantResultUpdateDTO, updateResult);
        updateResult.setUpdateTime(LocalDateTime.now());

        // 4. 执行更新
        plantResultMapper.updateById(updateResult);

        return Result.success();
    }

    /**
     * 补充：删除种植成果（完整实现，含权限校验，改用BaseContext获取用户ID）
     */
    @Override
    public Result<Void> deletePlantResult(Long resultId) {
        // 5. 改用BaseContext获取用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("删除种植成果，成果ID：{}，当前用户ID：{}", resultId, userId);

        // 1. 校验成果存在
        PlantResult existResult = plantResultMapper.selectById(resultId);
        if (existResult == null) {
            return Result.error(MessageConstant.PLANT_RESULT + MessageConstant.NOT_EXIST);
        }

        // 2. 校验成果归属当前用户（按指定写法构造QueryWrapper）
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existResult.getPlanId().intValue())
                .eq("user_id", userId);
        PlantPlan plan = plantPlanMapper.selectOne(planQueryWrapper);

        if (plan == null) {
            return Result.error("无权限删除该种植成果");
        }

        // 3. 执行删除
        plantResultMapper.deleteById(resultId);
        log.info("种植成果{}删除成功", resultId);

        return Result.success();
    }

    // 6. 废弃并删除原getCurrentUserId()方法
}