package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.mapper.PlantNodeMapper;
import cn.edu.seig.MhWeb.mapper.PlantPlanMapper;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantNodeUpdateDTO;
import cn.edu.seig.MhWeb.model.vo.PlantNodeVO;
import cn.edu.seig.MhWeb.model.entity.PlantNode;
import cn.edu.seig.MhWeb.model.entity.PlantPlan;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPlantNodeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * 种植节点业务实现类
 * （已移除所有缓存注解，完善权限校验）
 *
 * @author su
 */
@Slf4j
@Service
// 1. 移除缓存配置注解
public class PlantNodeServiceImpl extends ServiceImpl<PlantNodeMapper, PlantNode> implements IPlantNodeService {

    @Autowired
    private PlantNodeMapper plantNodeMapper;
    @Autowired
    private PlantPlanMapper plantPlanMapper;
    @Override
    // 2. 移除缓存注解
    public Result<List<PlantNodeVO>> getNodeListByPlanId(Long planId) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询计划{}的节点列表，当前用户ID：{}", planId, userId);

        // 1. 校验计划归属当前用户（核心：确保只能查询自己的计划）
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", planId)
                .eq("user_id", userId);
        PlantPlan existPlan = plantPlanMapper.selectOne(planQueryWrapper);
        if (existPlan == null) {
            log.warn("计划{}不存在或不属于当前用户", planId);
            return Result.error("种植计划不存在或无访问权限");
        }

        // 2. 查询该计划下的所有节点（仅当前用户的计划）
        QueryWrapper<PlantNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", planId)
                .orderByAsc("plan_time"); // 按预计执行时间升序
        List<PlantNode> nodeList = plantNodeMapper.selectList(queryWrapper);

        // 3. 转换为VO并补充状态名称、自动处理逾期
        List<PlantNodeVO> voList = nodeList.stream()
                .map(node -> {
                    PlantNodeVO vo = new PlantNodeVO();
                    BeanUtils.copyProperties(node, vo);

                    // 补充状态名称（未完成/已完成/逾期未完成）
                    vo.setStatus(node.getStatus());

                    // 自动计算逾期状态：预计时间已过且未完成 → 标记为逾期（3）
                    LocalDateTime now = LocalDateTime.now();
                    if (node.getStatus() == 1 && node.getPlanTime().isBefore(now)) {
                        vo.setStatus(3);
                        vo.setStatusName("逾期未完成");
                        // 同步更新数据库状态（可选，保证数据一致性）
                        if (!node.getStatus().equals(3)) { // 避免重复更新
                            node.setStatus(3);
                            plantNodeMapper.updateById(node);
                        }
                    }
                    return vo;
                }).collect(Collectors.toList());

        // 4. 包装为Result返回
        return Result.success(voList);
    }

    @Override
    // 3. 移除缓存注解
    public Result addPlantNode(PlantNodeAddDTO plantNodeAddDTO) {
        log.info("新增种植节点：{}", plantNodeAddDTO);

        // 1. 完善TODO：校验计划归属当前用户
        Long planId = plantNodeAddDTO.getPlanId();
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", planId);
        PlantPlan existPlan = plantPlanMapper.selectOne(planQueryWrapper);
        if (existPlan == null) {
            log.warn("新增种植节点失败：计划{}不存在或不属于当前用户", planId);
            return Result.error("种植计划不存在或无访问权限");
        }

        // 2. 自动计算提醒时间（提前1天）
        LocalDateTime remindTime = plantNodeAddDTO.getPlanTime().minusDays(1);
        if (StringUtils.isEmpty(plantNodeAddDTO.getRemindTime())) {
            plantNodeAddDTO.setRemindTime(remindTime);
        }

        // 3. DTO转实体
        PlantNode node = new PlantNode();
        BeanUtils.copyProperties(plantNodeAddDTO, node);
        node.setStatus(1); // 默认未完成

        // 4. 插入数据库
        plantNodeMapper.insert(node);

        return Result.success();
    }

    @Override
    // 4. 移除缓存注解
    public Result updatePlantNode(PlantNodeUpdateDTO plantNodeUpdateDTO) {
        log.info("更新种植节点：{}", plantNodeUpdateDTO);

        // 1. 校验节点存在且归属当前用户
        Long nodeId = plantNodeUpdateDTO.getNodeId();
        PlantNode existNode = plantNodeMapper.selectById(nodeId);
        if (existNode == null) {
            log.warn("更新种植节点失败：节点{}不存在", nodeId);
            return Result.error(MessageConstant.PLANT_NODE + MessageConstant.NOT_EXIST);
        }

        // 补充权限校验：节点关联的计划归属当前用户
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existNode.getPlanId());
        PlantPlan existPlan = plantPlanMapper.selectOne(planQueryWrapper);
        if (existPlan == null) {
            log.warn("更新种植节点失败：无权限修改非本人所属的节点{}", nodeId);
            return Result.error("无权限修改该种植节点");
        }

        // 2. 转换为实体并更新时间
        PlantNode updateNode = new PlantNode();
        BeanUtils.copyProperties(plantNodeUpdateDTO, updateNode);

        // 3. 执行更新
        plantNodeMapper.updateById(updateNode);

        return Result.success();
    }

    @Override
    // 5. 移除缓存注解
    public Result deletePlantNode(Long nodeId) {
        log.info("删除种植节点：{}", nodeId);

        // 1. 校验节点存在
        PlantNode existNode = plantNodeMapper.selectById(nodeId);
        if (existNode == null) {
            log.warn("删除种植节点失败：节点{}不存在", nodeId);
            return Result.error(MessageConstant.PLANT_NODE + MessageConstant.NOT_EXIST);
        }

        // 补充权限校验：节点关联的计划归属当前用户
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existNode.getPlanId());
        PlantPlan existPlan = plantPlanMapper.selectOne(planQueryWrapper);
        if (existPlan == null) {
            return Result.error("删除种植节点失败");
        }
        // 2. 删除节点
        plantNodeMapper.deleteById(nodeId);
        return Result.success();
    }

    @Override
    // 6. 移除缓存注解
    public Result markNodeCompleted(Long nodeId) {
        log.info("标记节点{}为完成", nodeId);

        // 1. 校验节点存在
        PlantNode existNode = plantNodeMapper.selectById(nodeId);
        if (existNode == null) {
            log.warn("标记节点完成失败：节点{}不存在", nodeId);
            return Result.error(MessageConstant.PLANT_NODE + MessageConstant.NOT_EXIST);
        }

        // 补充权限校验：节点关联的计划归属当前用户
        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.eq("plan_id", existNode.getPlanId());
        PlantPlan existPlan = plantPlanMapper.selectOne(planQueryWrapper);
        if (existPlan == null) {
            return Result.error("标记节点完成失败");
        }

        // 2. 更新状态和实际完成时间
        existNode.setStatus(2);
        existNode.setActualTime(LocalDateTime.now());
        plantNodeMapper.updateById(existNode);

        return Result.success();
    }

    @Override
    // 7. 移除缓存注解，修正返回值类型为Result
    public Result deletePlantNodes(List<Long> nodeIds) {
        log.info("批量删除种植节点，节点ID列表：{}，当前用户ID：{}", nodeIds);

        // 1. 参数校验：节点ID列表不能为空
        if (nodeIds == null || nodeIds.isEmpty()) {
            log.warn("批量删除种植节点失败：节点ID列表为空");
            return Result.error(MessageConstant.PLANT_NODE + MessageConstant.ID + MessageConstant.NOT_NULL);
        }

        // 2. 校验节点存在且归属当前用户（核心权限控制）
        // 2.1 查询待删除的节点列表
        QueryWrapper<PlantNode> nodeQueryWrapper = new QueryWrapper<>();
        nodeQueryWrapper.in("node_id", nodeIds);
        List<PlantNode> existNodes = plantNodeMapper.selectList(nodeQueryWrapper);
        // 2.2 校验节点是否存在
        if (existNodes.isEmpty()) {
            return Result.error(MessageConstant.PLANT_NODE + MessageConstant.NOT_EXIST);
        }
        // 2.3 提取节点关联的计划ID，校验所有计划归属当前用户
        List<Long> planIds = existNodes.stream()
                .map(PlantNode::getPlanId)
                .distinct()
                .collect(Collectors.toList());

        QueryWrapper<PlantPlan> planQueryWrapper = new QueryWrapper<>();
        planQueryWrapper.in("plan_id", planIds); // 仅查询当前用户的计划
        List<PlantPlan> existPlans = plantPlanMapper.selectList(planQueryWrapper);

        // 校验：所有节点关联的计划都必须属于当前用户
        if (existPlans.size() != planIds.size()) {
            return Result.error("批量删除种植节点失败");
        }

        // 3. 执行批量删除
        plantNodeMapper.deleteBatchIds(nodeIds);
        log.info("批量删除种植节点成功，共删除{}个节点，节点ID列表：{}", existNodes.size(), nodeIds);

        // 4. 返回成功结果
        return Result.success();
    }
}