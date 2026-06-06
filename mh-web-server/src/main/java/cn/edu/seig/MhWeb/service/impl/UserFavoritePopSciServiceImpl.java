package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.enumeration.FavoriteTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import cn.edu.seig.MhWeb.mapper.*;
import cn.edu.seig.MhWeb.model.dto.userFavorite.PopSciFavoriteDTO;
import cn.edu.seig.MhWeb.model.entity.*;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciContentService;
import cn.edu.seig.MhWeb.service.IUserFavoritePopSciService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.edu.seig.MhWeb.constant.MessageConstant.COLLECT;

/**
 * 用户收藏科普内容Service实现类
 *
 * @author su
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "favoriteCache") // 收藏缓存前缀（参考科普内容的缓存规范）
public class UserFavoritePopSciServiceImpl extends ServiceImpl<UserFavouriteMapper, UserFavourite> implements IUserFavoritePopSciService {

    // 注入关联Mapper（参考科普内容Service的注入风格）
    @Autowired
    private UserFavouriteMapper userFavouriteMapper;
    @Autowired
    private PopSciContentMapper popSciContentMapper;
    @Autowired
    private MushroomMapper mushroomMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PictureMapper pictureMapper;
    @Autowired
    private IPopSciContentService popSciContentService;

    /**
     * 收藏科普内容：原有逻辑 + 收藏数+1（添加事务，保证原子操作）
     */
    /**
     * 收藏科普内容：原有逻辑 + 收藏数+1（添加事务，保证原子操作）
     * 适配参数：mushroomName → 转换为唯一mushroomId
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证：新增记录+更新收藏数原子操作
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 刷新科普内容缓存
    public Result collectPopSci(Long popsciId, FavoriteTypeEnum favoriteType, String mushroomName) {
        try {
            // 1. 基础参数非空校验（含菌种名称）
            if (popsciId == null || favoriteType == null) {
                return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_NULL);
            }
            // 菌种名称非空+非空白校验
            if (mushroomName == null || mushroomName.trim().isEmpty()) {
                return Result.error("菌种名称不能为空");
            }
            // 用户登录态校验
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return Result.error("请先登录后再进行收藏操作");
            }
            String trimMushName = mushroomName.trim(); // 去除首尾空格，避免查询匹配失败

            // 2. 核心：通过菌种名称查询唯一mushroomId
            List<MushroomInfo> mushroomList = mushroomMapper.selectByMushroomName(trimMushName);
            // 2.1 菌种名称不存在
            if (CollectionUtils.isEmpty(mushroomList)) {
                return Result.error("菌种名称【" + trimMushName + "】不存在，请核对后重试");
            }
            // 2.2 菌种名称不唯一（数据异常，避免脏数据）
            if (mushroomList.size() > 1) {
                log.warn("菌种名称【{}】查询到多条记录，数据异常", trimMushName);
                return Result.error("菌种名称存在重复，请联系管理员处理");
            }
            // 2.3 提取唯一的菌种ID
            Long mushroomId = mushroomList.get(0).getId();

            // 3. 校验是否已收藏（避免重复收藏）
            List<UserFavourite> existFavorites = userFavouriteMapper.selectByUserIdAndPopsciId(userId, popsciId);
            if (!CollectionUtils.isEmpty(existFavorites)) {
                return Result.error("该科普内容已收藏，无需重复收藏");
            }

            // 4. 校验科普内容是否存在且审核通过（仅通过的内容可收藏）
            PopSciContent existContent = popSciContentService.getById(popsciId);
            if (existContent == null) {
                return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
            }
            if (!PopSciContentStatusEnum.PASSED.equals(existContent.getContentStatus())) {
                return Result.error("仅审核通过的科普内容可进行收藏操作");
            }

            // 5. 新增用户收藏记录（使用查询到的mushroomId）
            UserFavourite favorite = new UserFavourite();
            favorite.setUserId(userId);
            favorite.setPopsciId(popsciId);
            favorite.setFavoriteType(favoriteType.getId());
            favorite.setMushroomId(mushroomId); // 存入数据库的还是菌种ID，保证原有表结构不变
            favorite.setCreateTime(LocalDateTime.now());
            userFavouriteMapper.insert(favorite);
            log.info("用户{}收藏科普内容{}成功，菌种：{}({})，新增收藏记录",
                    userId, popsciId, trimMushName, mushroomId);

            // 6. 更新科普内容收藏数+1（空值兼容：默认0+1）
            int newCollectCount = Optional.ofNullable(existContent.getCollectCount()).orElse(0) + 1;
            existContent.setCollectCount(newCollectCount);
            popSciContentService.updateById(existContent);
            log.info("科普内容{}收藏数更新成功，当前收藏数：{}", popsciId, newCollectCount);

            return Result.success(MessageConstant.COLLECT + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("用户{}收藏科普内容{}失败，菌种名称：{}",
                    BaseContext.getCurrentId(), popsciId, mushroomName, e);
            // 事务自动回滚：新增记录/收藏数更新都会回滚
            return Result.error(MessageConstant.COLLECT + MessageConstant.FAILED);
        }
    }
    /**
     * 取消收藏科普内容：原有逻辑 + 收藏数-1（添加事务，保证原子操作）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 新增：事务，异常则回滚
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 刷新科普内容缓存
    public Result cancelCollectPopSci(Long popsciId) {
        // 1. 基础校验
        if (popsciId == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("请先登录后再进行取消收藏操作");
        }

        // 2. 校验是否存在收藏记录（无记录则无需取消）
        List<UserFavourite> existFavorites = userFavouriteMapper.selectByUserIdAndPopsciId(userId, popsciId);
        if (CollectionUtils.isEmpty(existFavorites)) {
            return Result.error("未收藏该科普内容，无需取消");
        }

        // 3. 原有逻辑：删除用户收藏记录（批量删除，兼容多记录异常情况）
        List<Long> favoriteIds = existFavorites.stream().map(UserFavourite::getId).collect(java.util.stream.Collectors.toList());
        userFavouriteMapper.deleteBatchIds(favoriteIds);
        log.info("用户{}取消收藏科普内容{}成功，删除收藏记录{}条", userId, popsciId, favoriteIds.size());

        // 4. 新增核心逻辑：更新科普内容收藏数-1，保底0
        PopSciContent existContent = popSciContentService.getById(popsciId);
        if (existContent != null) { // 双重校验，避免内容已被删除
            int newCollectCount = Math.max(0, Optional.ofNullable(existContent.getCollectCount()).orElse(0) - 1);
            existContent.setCollectCount(newCollectCount);
            popSciContentService.updateById(existContent);
            log.info("科普内容{}收藏数更新成功，当前收藏数：{}", popsciId, newCollectCount);
        }

        return Result.success("取消" + MessageConstant.COLLECT + MessageConstant.SUCCESS);
    }

    /**
     * 获取用户收藏的科普内容列表（分页）
     */
    @Override
    public Result<PageResult<PopSciContentSummaryVO>> getUserFavoritePopSciList(PopSciFavoriteDTO dto) {
        try {
            Long userId = BaseContext.getCurrentId();
            Assert.notNull(userId, MessageConstant.USER + MessageConstant.NOT_LOGIN);

            // 1. 调用Mapper查询收藏的科普内容ID列表（带条件过滤）
            List<Long> popsciIdList = userFavouriteMapper.selectPopsciIdsByUserIdAndType(dto,userId);
            if (CollectionUtils.isEmpty(popsciIdList)) {
                return Result.success(new PageResult<>(0L, Collections.emptyList()));
            }

            // 2. 调用 Mapper 批量查询精简 VO（直接返回全部，前端自行分页）
            List<PopSciContentSummaryVO> summaryVOList = userFavouriteMapper.selectPopSciSummaryByPopsciIds(popsciIdList);
            // 5. 封装分页结果
            PageResult<PopSciContentSummaryVO> pageResult = new PageResult<>(
                    (long) popsciIdList.size(), // 总条数
                    summaryVOList
            );
            return Result.success(MessageConstant.COLLECT+MessageConstant.QUERY,pageResult);

        } catch (Exception e) {
            log.error("获取用户收藏列表失败：", e);
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result batchCancelCollectPopSci(List<Long> popsciIds) {
        // 1. 基础校验
        if (CollectionUtils.isEmpty(popsciIds)) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("请先登录后再进行批量取消收藏操作");
        }

        // 2. 批量查询存在的收藏记录 (使用 LambdaQueryWrapper)
        List<UserFavourite> existFavorites = userFavouriteMapper.selectList(
                new LambdaQueryWrapper<UserFavourite>()
                        .eq(UserFavourite::getUserId, userId)
                        .in(UserFavourite::getPopsciId, popsciIds)
        );

        if (CollectionUtils.isEmpty(existFavorites)) {
            return Result.error("未收藏选中的科普内容，无需取消");
        }

        // 3. 批量删除收藏记录
        List<Long> favoriteIds = existFavorites.stream().map(UserFavourite::getId).collect(Collectors.toList());
        userFavouriteMapper.deleteBatchIds(favoriteIds);
        log.info("用户{}批量取消收藏科普内容成功，删除收藏记录{}条", userId, favoriteIds.size());

        // 4. 按科普内容ID分组，统计每个ID被取消收藏的次数
        Map<Long, Long> popsciCancelCountMap = existFavorites.stream()
                .collect(Collectors.groupingBy(UserFavourite::getPopsciId, Collectors.counting()));

        // 5. 批量更新科普内容收藏数
        List<PopSciContent> contentsToUpdate = new ArrayList<>();
        for (Long popsciId : popsciCancelCountMap.keySet()) {
            PopSciContent existContent = popSciContentService.getById(popsciId);
            if (existContent != null) {
                int currentCount = Optional.ofNullable(existContent.getCollectCount()).orElse(0);
                long cancelCount = popsciCancelCountMap.get(popsciId);
                int newCollectCount = Math.max(0, currentCount - (int) cancelCount);
                existContent.setCollectCount(newCollectCount);
                contentsToUpdate.add(existContent);
                log.info("科普内容{}收藏数更新成功，当前收藏数：{}", popsciId, newCollectCount);
            }
        }
        if (!contentsToUpdate.isEmpty()) {
            popSciContentService.updateBatchById(contentsToUpdate);
        }

        return Result.success("批量取消" + MessageConstant.COLLECT + MessageConstant.SUCCESS);
    }

}