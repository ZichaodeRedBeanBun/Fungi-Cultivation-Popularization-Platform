package cn.edu.seig.MhWeb.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import cn.edu.seig.MhWeb.mapper.DiseasePestMapper;
import cn.edu.seig.MhWeb.mapper.MushroomDiseaseRelationMapper;
import cn.edu.seig.MhWeb.mapper.MushroomMapper;
import cn.edu.seig.MhWeb.mapper.PictureMapper;
import cn.edu.seig.MhWeb.model.dto.DiseasePest.DiseasePestAddDTO;
import cn.edu.seig.MhWeb.model.dto.DiseasePest.DiseasePestPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.DiseasePest.DiseasePestUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.DiseasePest;
import cn.edu.seig.MhWeb.model.entity.MushroomDiseaseRelation;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import cn.edu.seig.MhWeb.model.entity.Picture;
import cn.edu.seig.MhWeb.model.vo.DiseasePestSummaryVO;
import cn.edu.seig.MhWeb.model.vo.DiseasePestVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IDiseasePestService;
import cn.edu.seig.MhWeb.service.MinioService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 病虫害服务实现类（全流程使用枚举 + 直接调用Mapper操作数据库）
 *
 * @author su
 */
@Slf4j
@Service
public class DiseasePestServiceImpl extends ServiceImpl<DiseasePestMapper, DiseasePest> implements IDiseasePestService {

    // 核心修改：注入Mapper，直接操作数据库
    @Autowired
    private DiseasePestMapper diseasePestMapper;

    @Autowired
    private MushroomDiseaseRelationMapper mushroomDiseaseRelationMapper;

    @Autowired
    private MushroomMapper mushroomMapper;
    @Autowired
    private PictureMapper pictureMapper;
    @Autowired
    private MinioService minioService;
    /**
     * 获取病虫害分页列表（枚举查询条件 + 缓存 + 新增封面图查询）
     */
    @Override
    public Result<PageResult<DiseasePestVO>> getAllDiseasePests(DiseasePestPageQueryDTO diseasePestPageQueryDTO) {
        // 1. 构建分页对象
        Page<DiseasePest> page = new Page<>(diseasePestPageQueryDTO.getPageNum(), diseasePestPageQueryDTO.getPageSize());

        // 2. 构建查询条件（全程使用枚举）
        LambdaQueryWrapper<DiseasePest> queryWrapper = new LambdaQueryWrapper<>();
        // 模糊查询：病虫害名称
        if (StringUtils.hasText(diseasePestPageQueryDTO.getDiseaseName())) {
            queryWrapper.like(DiseasePest::getDiseaseName, diseasePestPageQueryDTO.getDiseaseName());
        }
        // 精准查询：类型（通过枚举校验+取值）
        DiseasePestTypeEnum queryType = diseasePestPageQueryDTO.getItemTypeEnum();
        if (queryType != null) {
            queryWrapper.eq(DiseasePest::getItemType, queryType.getId());
        }
        // 按更新时间倒序
        queryWrapper.orderByDesc(DiseasePest::getUpdateTime);

        // 3. 执行分页查询（调用Mapper，替代this.page）
        IPage<DiseasePest> diseasePestPage = diseasePestMapper.selectPage(page, queryWrapper);

        // 空数据处理（对齐菌种逻辑）
        if (diseasePestPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 4. 转换为VO（新增：关联查询菌种名称 + 封面图）
        List<DiseasePestVO> voList = diseasePestPage.getRecords().stream()
                .map(diseasePest -> {
                    DiseasePestVO vo = new DiseasePestVO();
                    BeanUtils.copyProperties(diseasePest, vo);
                    vo.setItemType(diseasePest.getItemType());

                    // 查询病虫害封面图
                    QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
                    coverQuery.eq("disease_id", diseasePest.getId()) // 关联病虫害ID（需与Picture表字段一致）
                            .eq("type", PictureTypeEnum.DISEASE_PEST_COVER.getPictureType()); // 封面类型枚举
                    Picture coverPic = pictureMapper.selectOne(coverQuery);
                    if (coverPic != null) {
                        vo.setCoverUrl(coverPic.getPicUrl()); // 赋值封面URL到VO
                    }

                    // 查询关联菌种名称
                    LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
                    relationWrapper.eq(MushroomDiseaseRelation::getDiseaseId, diseasePest.getId());
                    List<MushroomDiseaseRelation> relationList = mushroomDiseaseRelationMapper.selectList(relationWrapper);

                    if (!relationList.isEmpty()) {
                        List<Long> mushroomIds = relationList.stream()
                                .map(MushroomDiseaseRelation::getMushroomId)
                                .distinct()
                                .collect(Collectors.toList());

                        List<MushroomInfo> mushroomList = mushroomMapper.selectBatchIds(mushroomIds);
                        vo.setMushroomList(mushroomList);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // 5. 封装分页结果
        PageResult<DiseasePestVO> pageResult = new PageResult<>();
        pageResult.setTotal(diseasePestPage.getTotal());
        pageResult.setItems(voList);

        return Result.success(pageResult);
    }
    /**
     * 新增病虫害（含关联表插入）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：主表/关联表操作原子性
    public Result addDiseasePest(DiseasePestAddDTO diseasePestAddDTO) {
        log.info("新增病虫害：{}", diseasePestAddDTO);
        // 1. 枚举校验类型合法性
        DiseasePestTypeEnum addType = diseasePestAddDTO.getItemType();
        if (addType == null) {
            return Result.error(String.format(MessageConstant.DISEASE_TYPE + MessageConstant.INVALID));
        }

        // 2. 校验病虫害名称唯一性
        LambdaQueryWrapper<DiseasePest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiseasePest::getDiseaseName, diseasePestAddDTO.getDiseaseName());
        if (diseasePestMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.DISEASE_NAME + MessageConstant.ALREADY_EXISTS);
        }

        // 3. DTO转换为Entity
        DiseasePest diseasePest = new DiseasePest();
        BeanUtils.copyProperties(diseasePestAddDTO, diseasePest);
        diseasePest.setItemType(addType);
        diseasePest.setCreateTime(LocalDateTime.now());
        diseasePest.setUpdateTime(LocalDateTime.now());

        try {
            // 4. 插入主表（获取自增的病虫害ID）
            diseasePestMapper.insert(diseasePest);
            Long diseaseId = diseasePest.getId(); // 插入后自动回填的ID

            // 5. 处理关联表：如果DTO有关联菌种列表，批量插入
            List<Long> mushroomIds = diseasePestAddDTO.getMushroomIds(); // DTO需新增该字段（关联的菌种ID列表）
            if (mushroomIds != null && !mushroomIds.isEmpty()) {
                // 去重：避免重复关联同一菌种
                List<Long> distinctMushroomIds = mushroomIds.stream().distinct().collect(Collectors.toList());
                for (Long mushroomId : distinctMushroomIds) {
                    // 校验菌种是否存在（可选，增强健壮性）
                    if (mushroomMapper.selectById(mushroomId) == null) {
                        throw new RuntimeException("菌种ID：" + mushroomId + " 不存在，新增关联失败");
                    }

                    // 构建关联表实体
                    MushroomDiseaseRelation relation = new MushroomDiseaseRelation();
                    relation.setMushroomId(mushroomId);
                    relation.setDiseaseId(diseaseId); // 注意类型匹配（主表id是Long，关联表是int）

                    // 可选：从病虫害/菌种表同步分类信息（如果需要）
                    // relation.setCategoryId(diseasePest.getCategoryId());
                    // relation.setCategoryName(diseasePest.getCategoryName());

                    // 插入关联表
                    mushroomDiseaseRelationMapper.insert(relation);
                }
            }

        } catch (Exception e) {
            log.error("新增病虫害失败，参数：{}", diseasePestAddDTO, e);
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }

        return Result.success();
    }

    /**
     * 编辑病虫害（含关联表更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateDiseasePest(DiseasePestUpdateDTO diseasePestUpdateDTO) {
        // 1. 枚举校验类型合法性
        DiseasePestTypeEnum updateType = diseasePestUpdateDTO.getItemType();
        if (updateType == null) {
            return Result.error(String.format(MessageConstant.DISEASE_TYPE + MessageConstant.INVALID));
        }

        // 2. 校验主键是否存在
        Long id = diseasePestUpdateDTO.getId();
        if (id == null || diseasePestMapper.selectById(id) == null) {
            return Result.error(MessageConstant.DISEASE + MessageConstant.NOT_EXIST);
        }

        // 3. 校验病虫害名称唯一性（排除自身）
        LambdaQueryWrapper<DiseasePest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiseasePest::getDiseaseName, diseasePestUpdateDTO.getDiseaseName())
                .ne(DiseasePest::getId, id);
        if (diseasePestMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.DISEASE_NAME + MessageConstant.ALREADY_EXISTS);
        }

        // 4. DTO转换为Entity
        DiseasePest diseasePest = new DiseasePest();
        BeanUtils.copyProperties(diseasePestUpdateDTO, diseasePest);
        diseasePest.setItemType(updateType);
        diseasePest.setUpdateTime(LocalDateTime.now());

        try {
            // 5. 更新主表
            int updateCount = diseasePestMapper.updateById(diseasePest);
            if (updateCount == 0) {
                return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
            }

            // 6. 处理关联表：先删除该病虫害的所有旧关联
            LambdaQueryWrapper<MushroomDiseaseRelation> relationDelWrapper = new LambdaQueryWrapper<>();
            relationDelWrapper.eq(MushroomDiseaseRelation::getDiseaseId, id.intValue());
            mushroomDiseaseRelationMapper.delete(relationDelWrapper);

            // 7. 插入新的关联数据（如果DTO有新的菌种列表）
            List<Long> newMushroomIds = diseasePestUpdateDTO.getMushroomIds(); // DTO需新增该字段
            if (newMushroomIds != null && !newMushroomIds.isEmpty()) {
                List<Long> distinctIds = newMushroomIds.stream().distinct().collect(Collectors.toList());
                for (Long mushroomId : distinctIds) {
                    if (mushroomMapper.selectById(mushroomId) == null) {
                        throw new RuntimeException("菌种ID：" + mushroomId + " 不存在，更新关联失败");
                    }

                    MushroomDiseaseRelation relation = new MushroomDiseaseRelation();
                    relation.setMushroomId(mushroomId);
                    relation.setDiseaseId(id);
                    // 可选：同步分类信息
                    // relation.setCategoryId(diseasePest.getCategoryId());
                    // relation.setCategoryName(diseasePest.getCategoryName());

                    mushroomDiseaseRelationMapper.insert(relation);
                }
            }

        } catch (Exception e) {
            log.error("编辑病虫害失败，参数：{}", diseasePestUpdateDTO, e);
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success();
    }

    /**
     * 删除单个病虫害（多对多逻辑：仅无关联菌种时可删主表 + 新增封面删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDiseasePest(Long id) {
        if (id == null) {
            return Result.error(MessageConstant.DISEASE_ID + MessageConstant.NOT_NULL);
        }

        // 1. 校验病虫害是否存在
        DiseasePest existDisease = diseasePestMapper.selectById(id);
        if (existDisease == null) {
            return Result.error(MessageConstant.DISEASE + MessageConstant.NOT_EXIST);
        }

        // 2. 核心：查询该病虫害的关联记录数（多对多关键校验）
        LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(MushroomDiseaseRelation::getDiseaseId, id.intValue());
        long relationCount = mushroomDiseaseRelationMapper.selectCount(relationWrapper);

        // 3. 若仍有关联菌种，禁止删除主表
        if (relationCount > 0) {
            return Result.error("该病虫害仍关联" + relationCount + "个菌种，无法删除（请先删除所有关联关系）");
        }

        try {
            // 删除病虫害封面
            // 3.1 查询该病虫害的封面图记录
            QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
            coverQuery.eq("disease_id", id)
                    .eq("type", PictureTypeEnum.DISEASE_PEST_COVER.getPictureType());
            List<Picture> coverPicList = pictureMapper.selectList(coverQuery);

            // 3.2 删除MinIO中的封面文件
            for (Picture pic : coverPicList) {
                String picUrl = pic.getPicUrl();
                if (picUrl != null && !picUrl.isEmpty()) {
                    minioService.deleteFile(picUrl);
                }
            }

            // 3.3 删除Picture表中的封面记录
            if (!coverPicList.isEmpty()) {
                pictureMapper.deleteBatchIds(coverPicList.stream().map(Picture::getId).collect(Collectors.toList()));
            }

            // 4. 无关联时，删除病虫害主表
            diseasePestMapper.deleteById(id);
        } catch (Exception e) {
            log.error("删除病虫害失败，ID：{}", id, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success("病虫害及封面删除成功");
    }

    /**
     * 批量删除病虫害（多对多逻辑：仅无关联的可删 + 新增封面删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDiseasePests(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(MessageConstant.DISEASE_ID + MessageConstant.NOT_NULL);
        }

        // 存储可删除的ID、不可删除的ID（用于提示）
        List<Long> deletableIds = new ArrayList<>();
        List<Long> undeletableIds = new ArrayList<>();

        for (Long id : ids) {
            // 校验病虫害是否存在
            if (diseasePestMapper.selectById(id) == null) {
                undeletableIds.add(id);
                continue;
            }

            // 检查关联记录数
            LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(MushroomDiseaseRelation::getDiseaseId, id);
            long relationCount = mushroomDiseaseRelationMapper.selectCount(relationWrapper);

            if (relationCount > 0) {
                // 仍有关联，不可删
                undeletableIds.add(id);
            } else {
                // 无关联，可删
                deletableIds.add(id);
            }
        }

        try {
            // 批量删除病虫害封面
            if (!deletableIds.isEmpty()) {
                // 1.1 查询可删病虫害的所有封面记录
                QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
                coverQuery.in("disease_id", deletableIds)
                        .eq("type", PictureTypeEnum.DISEASE_PEST_COVER.getPictureType());
                List<Picture> coverPicList = pictureMapper.selectList(coverQuery);

                // 1.2 批量删除MinIO中的封面文件
                for (Picture pic : coverPicList) {
                    String picUrl = pic.getPicUrl();
                    if (picUrl != null && !picUrl.isEmpty()) {
                        minioService.deleteFile(picUrl);
                    }
                }

                // 1.3 批量删除Picture表中的封面记录
                if (!coverPicList.isEmpty()) {
                    pictureMapper.deleteBatchIds(coverPicList.stream().map(Picture::getId).collect(Collectors.toList()));
                }

                // 2. 批量删除可删的病虫害主表数据
                diseasePestMapper.deleteBatchIds(deletableIds);
            }

            // 3. 构建提示信息
            StringBuilder msg = new StringBuilder();
            if (!deletableIds.isEmpty()) {
                msg.append("已删除无关联的病虫害ID：").append(deletableIds).append("（含关联封面）；");
            }
            if (!undeletableIds.isEmpty()) {
                msg.append("以下病虫害仍有关联菌种，未删除：").append(undeletableIds);
            }

            return Result.success(msg.toString());

        } catch (Exception e) {
            log.error("批量删除病虫害失败，IDs：{}", ids, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }

    /**
     * 获取病虫害总数（带缓存）
     */
    @Override
    @Cacheable(cacheNames = "diseasePestCache", key = "'diseasePestCount-'")
    public Result<Long> getAllDiseasePestsCount() {
        QueryWrapper<DiseasePest> queryWrapper = new QueryWrapper<>();
        // 按关联菌种ID筛选
        Long count = diseasePestMapper.selectCount(queryWrapper);
        return Result.success(count);
    }

    @Override
    public Result<DiseasePestVO> getDiseasePestById(Long diseasePestId) {
        // 1. 校验ID非空
        if (diseasePestId == null || diseasePestId <= 0) {
            return Result.error("病虫害ID不合法");
        }
        // 2. 根据ID查询病虫害基础信息
        DiseasePest diseasePest = baseMapper.selectById(diseasePestId);
        if (diseasePest == null) {
            return Result.error("未查询到该病虫害信息");
        }
        // 3. 实体转VO
        DiseasePestVO diseasePestVO = new DiseasePestVO();
        BeanUtils.copyProperties(diseasePest, diseasePestVO);

        // 查询关联菌种列表
        LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(MushroomDiseaseRelation::getId, diseasePestId);
        List<MushroomDiseaseRelation> relationList = mushroomDiseaseRelationMapper.selectList(relationWrapper);
        List<Long> mushroomIds = relationList.stream()
                .map(MushroomDiseaseRelation::getMushroomId)
                .collect(Collectors.toList());

        List<MushroomInfo> mushroomList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mushroomIds)) {
            mushroomList = mushroomMapper.selectBatchIds(mushroomIds);
        }
        diseasePestVO.setMushroomList(mushroomList);

        // 查询病虫害图片
        LambdaQueryWrapper<Picture> coverWrapper = new LambdaQueryWrapper<>();
        coverWrapper.eq(Picture::getDiseaseId, diseasePestId) // 用新增的disease_id关联
                .eq(Picture::getType, PictureTypeEnum.DISEASE_PEST_COVER.getPictureType());
        Picture coverPic = pictureMapper.selectOne(coverWrapper);
        if (coverPic != null) {
            diseasePestVO.setCoverUrl(coverPic.getPicUrl());
        }

        return Result.success(diseasePestVO);
    }

    /**
     * 单独删除病虫害-菌种的关联关系（不删主表）
     * @param diseaseId 病虫害ID
     * @param mushroomId 菌种ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDiseaseMushroomRelation(Long diseaseId, Long mushroomId) {
        // 1. 基础校验
        if (diseaseId == null || mushroomId == null) {
            return Result.error("病虫害ID和菌种ID不能为空");
        }
        if (diseasePestMapper.selectById(diseaseId) == null) {
            return Result.error(MessageConstant.DISEASE + MessageConstant.NOT_EXIST);
        }
        if (mushroomMapper.selectById(mushroomId) == null) {
            return Result.error(MessageConstant.MUSHROOM + MessageConstant.NOT_EXIST);
        }

        // 2. 删除指定的关联记录
        LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(MushroomDiseaseRelation::getDiseaseId, diseaseId.intValue())
                .eq(MushroomDiseaseRelation::getMushroomId, mushroomId);
        int deleteCount = mushroomDiseaseRelationMapper.delete(relationWrapper);

        if (deleteCount == 0) {
            return Result.error("该病虫害与菌种无关联，无需删除");
        }

        return Result.success("已解除病虫害ID：" + diseaseId + " 与菌种ID：" + mushroomId + " 的关联");
    }
    /**
     * 新增：病虫害概要VO分页列表（列表展示专用，无推荐算法，带封面图）
     */
    @Override
    public Result<PageResult<DiseasePestSummaryVO>> getDiseasePestSummaryPage(DiseasePestPageQueryDTO diseasePestPageQueryDTO) {
        // 1. 构建分页对象+查询条件（原有枚举逻辑完全保留）
        Page<DiseasePest> page = new Page<>(diseasePestPageQueryDTO.getPageNum(), diseasePestPageQueryDTO.getPageSize());
        LambdaQueryWrapper<DiseasePest> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(diseasePestPageQueryDTO.getDiseaseName())) {
            queryWrapper.like(DiseasePest::getDiseaseName, diseasePestPageQueryDTO.getDiseaseName());
        }
        DiseasePestTypeEnum queryType = diseasePestPageQueryDTO.getItemTypeEnum();
        if (queryType != null) {
            queryWrapper.eq(DiseasePest::getItemType, queryType.getId());
        }
        queryWrapper.orderByDesc(DiseasePest::getUpdateTime);

        // 2. 执行分页查询
        IPage<DiseasePest> diseasePestPage = diseasePestMapper.selectPage(page, queryWrapper);
        if (diseasePestPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 3. 实体转【病虫害概要VO】+ 新增封面图查询 + 保留关联菌种
        List<DiseasePestSummaryVO> summaryVOList = diseasePestPage.getRecords().stream()
                .map(diseasePest -> {
                    DiseasePestSummaryVO summaryVO = new DiseasePestSummaryVO();
                    BeanUtils.copyProperties(diseasePest, summaryVO);

                    // 原有关联菌种查询逻辑（完全保留，避免循环查库）
                    LambdaQueryWrapper<MushroomDiseaseRelation> relationWrapper = new LambdaQueryWrapper<>();
                    relationWrapper.eq(MushroomDiseaseRelation::getDiseaseId, diseasePest.getId());
                    List<MushroomDiseaseRelation> relationList = mushroomDiseaseRelationMapper.selectList(relationWrapper);
                    if (!relationList.isEmpty()) {
                        List<Long> mushroomIds = relationList.stream()
                                .map(MushroomDiseaseRelation::getMushroomId)
                                .distinct()
                                .collect(Collectors.toList());
                        summaryVO.setMushroomList(mushroomMapper.selectBatchIds(mushroomIds));
                    }

                    // 核心新增：病虫害封面图查询（与菌种/科普封面逻辑统一）
                    //TODO管理端对应的增删改查数据也要加上去
                    QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
                    coverQuery.eq("disease_id", diseasePest.getId()) // 关联病虫害ID
                            .eq("type", PictureTypeEnum.DISEASE_PEST_COVER); // 病虫害封面枚举
                    Picture coverPic = pictureMapper.selectOne(coverQuery);
                    if (coverPic != null) {
                        summaryVO.setCoverUrl(coverPic.getPicUrl());
                    }

                    return summaryVO;
                })
                .collect(Collectors.toList());

        // 4. 封装返回
        PageResult<DiseasePestSummaryVO> pageResult = new PageResult<>();
        pageResult.setTotal(diseasePestPage.getTotal());
        pageResult.setItems(summaryVOList);
        return Result.success(pageResult);
    }

    /**
     * 更新病虫害封面
     *
     * @param id   病虫害ID
     * @param file 封面文件
     * @return 操作结果
     */
    @Override
    public Result updateDiseasePestCover(Long id, MultipartFile file) {
        // 1. 基础校验：文件非空 + 病虫害记录存在
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }
        Long diseasePestId = id;
        // 查询病虫害基础信息（替换为你的病虫害实体类）
        DiseasePest existDiseasePest = diseasePestMapper.selectById(diseasePestId);
        if (existDiseasePest == null) {
            return Result.error(MessageConstant.DISEASE + MessageConstant.NOT_EXIST);
        }

        try {
            // 2. 上传新封面到MinIO（指定病虫害封面存储目录）
            String newCoverUrl = minioService.uploadFile(file, "diseaseCover");

            // 3. 查询已有封面图记录（按病虫害ID + 封面类型筛选）
            QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
            coverQuery.eq("disease_id", diseasePestId) // 替换为picture表中关联病虫害的字段
                    .eq("type", PictureTypeEnum.DISEASE_PEST_COVER); // 复用封面类型枚举（或新增DISEASE_COVER枚举）
            Picture existCover = pictureMapper.selectOne(coverQuery);

            // 4. 处理旧封面 + 新增/更新封面记录
            if (existCover != null) {
                // 删除MinIO中的旧封面文件
                String oldCoverUrl = existCover.getPicUrl();
                if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
                    minioService.deleteFile(oldCoverUrl);
                }
                // 更新数据库中的封面URL
                existCover.setPicUrl(newCoverUrl);
                pictureMapper.updateById(existCover);
            } else {
                // 新增封面记录（无旧封面时）
                Picture newCover = new Picture();
                newCover.setDiseaseId(diseasePestId); // 替换为picture表中关联病虫害的字段
                newCover.setPicUrl(newCoverUrl);
                newCover.setType(PictureTypeEnum.DISEASE_PEST_COVER); // 或自定义DISEASE_PEST_COVER类型
                pictureMapper.insert(newCover);
            }

            return Result.success();
        } catch (Exception e) {
            log.error("更新病虫害封面失败（diseasePestId={}）：", id, e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }
}