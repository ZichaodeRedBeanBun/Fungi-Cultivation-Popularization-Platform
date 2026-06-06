package cn.edu.seig.MhWeb.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import cn.edu.seig.MhWeb.mapper.MushroomMapper;
import cn.edu.seig.MhWeb.mapper.PictureMapper;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomAddDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import cn.edu.seig.MhWeb.model.entity.Picture;
import cn.edu.seig.MhWeb.model.vo.MushroomSummaryVO;
import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IMushroomService;
import cn.edu.seig.MhWeb.service.MinioService; // 需自行实现MinIO文件服务（参考菌种逻辑）
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菌种信息业务实现类
 *
 * @author su
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "mushroomCache") // 缓存前缀（对齐菌种缓存风格）
public class MushroomServiceImpl extends ServiceImpl<MushroomMapper, MushroomInfo> implements IMushroomService {

    @Autowired
    private MushroomMapper mushroomMapper;

    @Autowired
    private MinioService minioService; // 参考菌种逻辑，需实现MinIO文件上传/删除

    @Autowired
    private PictureMapper pictureMapper;

    /**
     * 获取菌种分页列表
     *
     * @param mushroomPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    @Cacheable(key = "#mushroomPageQueryDTO.pageNum + '-' + #mushroomPageQueryDTO.pageSize + '-' + #mushroomPageQueryDTO.mushroomName")
    public Result<PageResult<MushroomVO>> getAllMushrooms(MushroomPageQueryDTO mushroomPageQueryDTO) {
        // 1. 构建分页对象
        Page<MushroomInfo> page = new Page<>(mushroomPageQueryDTO.getPageNum(), mushroomPageQueryDTO.getPageSize());
        QueryWrapper<MushroomInfo> queryWrapper = new QueryWrapper<>();

        // 2. 构建查询条件（模糊查询菌种名称）
        if (mushroomPageQueryDTO.getMushroomName() != null && !mushroomPageQueryDTO.getMushroomName().isEmpty()) {
            queryWrapper.like("mushroom_name", mushroomPageQueryDTO.getMushroomName());
        }
        // 按更新时间倒序（优化体验）
        queryWrapper.orderByDesc("update_time");
        // 3. 执行分页查询
        IPage<MushroomInfo> mushroomPage = mushroomMapper.selectPage(page, queryWrapper);
        if (mushroomPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 4. 实体转换为VO（前端展示对象）
        List<MushroomVO> mushroomVOList = mushroomPage.getRecords().stream()
                .map(mushroomInfo -> {
                    MushroomVO mushroomVO = new MushroomVO();
                    BeanUtils.copyProperties(mushroomInfo, mushroomVO);
                    // ① 查询封面图（单张，原有逻辑）
                    QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
                    coverQuery.eq("mushroom_id", mushroomInfo.getId())
                            .eq("type", PictureTypeEnum.COVER.getPictureType());
                    Picture coverPic = pictureMapper.selectOne(coverQuery);
                    if (coverPic != null) {
                        mushroomVO.setCoverUrl(coverPic.getPicUrl());
                    }

                    // ② 新增：查询详情图（多张，type=详情图）
//                    QueryWrapper<Picture> detailQuery = new QueryWrapper<>();
//                    detailQuery.eq("mushroom_id", mushroomInfo.getId())
//                            .eq("type", PictureTypeEnum.DETAIL.getPictureType())
//                    List<Picture> detailPics = pictureMapper.selectList(detailQuery);
//                    // 提取详情图URL列表
//                    List<String> detailPicUrls = detailPics.stream()
//                            .map(Picture::getPicUrl)
//                            .collect(Collectors.toList());
//                    mushroomVO.setDetailPicUrls(detailPicUrls);
//                    return mushroomVO;
//                }).collect(Collectors.toList());
                    // ② 核心修改：直接返回Picture列表（无需转换VO）
                    QueryWrapper<Picture> detailQuery = new QueryWrapper<>();
                    detailQuery.eq("mushroom_id", mushroomInfo.getId())
                            .eq("type", PictureTypeEnum.DETAIL.getPictureType())
                            .orderByAsc("id");
                    List<Picture> detailPics = pictureMapper.selectList(detailQuery);

                    // 直接赋值Picture列表（包含id、picUrl等所有字段）
                    mushroomVO.setDetailPicUrls(detailPics);
                    return mushroomVO;
                }).collect(Collectors.toList());

        // 5. 封装分页结果返回
        return Result.success(new PageResult<>(mushroomPage.getTotal(), mushroomVOList));
    }

    /**
     * 新增菌种
     *
     * @param mushroomAddDTO 新增参数
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true) // 新增后清空缓存
    public Result addMushroom(MushroomAddDTO mushroomAddDTO) {
        // 1. 校验菌种名称唯一性（数据库唯一约束兜底，代码层提前校验）
        QueryWrapper<MushroomInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_name", mushroomAddDTO.getMushroomName());
        if (mushroomMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.MUSHROOM_NAME+MessageConstant.ALREADY_EXISTS); // 需在MessageConstant定义："菌种名称已存在"
        }

        // 2. DTO转换为实体
        MushroomInfo mushroomInfo = new MushroomInfo();
        BeanUtils.copyProperties(mushroomAddDTO, mushroomInfo);

        // 3. 插入数据库
        mushroomMapper.insert(mushroomInfo);

        // 4. 返回成功结果
        return Result.success();
    }

    /**
     * 更新菌种信息
     *
     * @param mushroomUpdateDTO 更新参数
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true) // 更新后清空缓存
    public Result updateMushroom(MushroomUpdateDTO mushroomUpdateDTO) {
        // 1. 校验主键存在
        MushroomInfo existMushroom = mushroomMapper.selectById(mushroomUpdateDTO.getId());
        if (existMushroom == null) {
            return Result.error(MessageConstant.MUSHROOM+MessageConstant.NOT_EXIST); // 需定义："菌种信息不存在"
        }

        // 2. 校验菌种名称唯一性（排除自身）
        if (mushroomUpdateDTO.getMushroomName() != null && !mushroomUpdateDTO.getMushroomName().isEmpty()) {
            QueryWrapper<MushroomInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mushroom_name", mushroomUpdateDTO.getMushroomName())
                    .ne("id", mushroomUpdateDTO.getId()); // 排除当前修改的菌种
            if (mushroomMapper.selectCount(queryWrapper) > 0) {
                return Result.error(MessageConstant.MUSHROOM_NAME+MessageConstant.ALREADY_EXISTS);
            }
        }

        // 3. DTO转换为实体（仅更新非空字段）
        MushroomInfo mushroomInfo = new MushroomInfo();
        BeanUtils.copyProperties(mushroomUpdateDTO, mushroomInfo);

        // 4. 执行更新
        int updateCount = mushroomMapper.updateById(mushroomInfo);
        if (updateCount == 0) {
            return Result.error(MessageConstant.FAILED); // 需定义："更新失败"
        }

        // 5. 返回成功结果
        return Result.success();
    }

    /**
     * 更新菌种封面
     *
     * @param id   菌种ID
     * @param file 封面文件
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true) // 更新封面后清空缓存
    public Result updateMushroomCover(Long id, MultipartFile file) {
        // 1. 基础校验
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }
        Long mushroomId = id;
        MushroomInfo existMushroom = mushroomMapper.selectById(mushroomId);
        if (existMushroom == null) {
            return Result.error(MessageConstant.MUSHROOM + MessageConstant.NOT_EXIST);
        }

        try {
            // 2. 上传新封面到MinIO
            String newCoverUrl = minioService.uploadFile(file, "mushroom_cover");

            // 3. 查询已有封面图记录
            QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
            coverQuery.eq("mushroom_id", mushroomId)
                    .eq("type", PictureTypeEnum.COVER);
            Picture existCover = pictureMapper.selectOne(coverQuery);

            // 4. 处理旧封面 + 新增/更新记录
            if (existCover != null) {
                // 删除旧文件
                String oldCoverUrl = existCover.getPicUrl();
                if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
                    minioService.deleteFile(oldCoverUrl);
                }
                // 更新URL
                existCover.setPicUrl(newCoverUrl);
                pictureMapper.updateById(existCover);
            } else {
                // 新增封面记录
                Picture newCover = new Picture();
                newCover.setMushroomId(mushroomId);
                newCover.setPicUrl(newCoverUrl);
                newCover.setType(PictureTypeEnum.COVER);
                pictureMapper.insert(newCover);
            }

            return Result.success();
        } catch (Exception e) {
            log.error("更新菌种封面失败（mushroomId={}）：", id, e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }
    /**
     * 删除单个菌种
     *
     * @param id 菌种ID
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true) // 删除后清空缓存
    public Result deleteMushroom(Long id) {
        Long mushroomId = id;
        MushroomInfo existMushroom = mushroomMapper.selectById(mushroomId);
        if (existMushroom == null) {
            return Result.error(MessageConstant.MUSHROOM + MessageConstant.NOT_EXIST);
        }

        try {
            // 1. 查询该菌种所有图片记录
            QueryWrapper<Picture> picQuery = new QueryWrapper<>();
            picQuery.eq("mushroom_id", mushroomId);
            List<Picture> picList = pictureMapper.selectList(picQuery);

            // 2. 批量删除MinIO文件
            for (Picture pic : picList) {
                String picUrl = pic.getPicUrl();
                if (picUrl != null && !picUrl.isEmpty()) {
                    minioService.deleteFile(picUrl);
                }
            }

            // 3. 删除菌种（外键自动删除Picture表关联记录）
            mushroomMapper.deleteById(mushroomId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除菌种失败（mushroomId={}）：", id, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }
    /**
     * 批量删除菌种
     *
     * @param ids 菌种ID列表
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true) // 批量删除后清空缓存
    public Result deleteMushrooms(List<Long> ids) {
        if (ids.isEmpty()) {
            return Result.error(MessageConstant.MUSHROOM_IDS + MessageConstant.NOT_NULL);
        }

        try {
            // 1. 转换ID类型
            List<Long> mushroomIds = ids.stream().collect(Collectors.toList());

            // 2. 校验菌种存在
            List<MushroomInfo> mushroomList = mushroomMapper.selectBatchIds(mushroomIds);
            if (mushroomList.isEmpty()) {
                return Result.error(MessageConstant.MUSHROOM + MessageConstant.NOT_EXIST);
            }

            // 3. 查询所有关联图片
            QueryWrapper<Picture> picQuery = new QueryWrapper<>();
            picQuery.in("mushroom_id", mushroomIds);
            List<Picture> picList = pictureMapper.selectList(picQuery);

            // 4. 批量删除图片文件
            for (Picture pic : picList) {
                String picUrl = pic.getPicUrl();
                if (picUrl != null && !picUrl.isEmpty()) {
                    minioService.deleteFile(picUrl);
                }
            }

            // 5. 批量删除菌种
            mushroomMapper.deleteBatchIds(mushroomIds);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除菌种失败（ids={}）：", ids, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }
    /**
     * 上传菌种详情图（多张）
     * @param mushroomId 菌种ID
     * @param files 详情图文件列表
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true)
    public Result uploadMushroomDetailPics(Long mushroomId, List<MultipartFile> files) {
        if (files.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }
        Long mushId = mushroomId;
        // 校验菌种存在
        if (mushroomMapper.selectById(mushId) == null) {
            return Result.error(MessageConstant.MUSHROOM + MessageConstant.NOT_EXIST);
        }

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                // 上传文件到MinIO
                String picUrl = minioService.uploadFile(file, "mushroom_detail");
                // 新增详情图记录到mh_picture
                Picture detailPic = new Picture();
                detailPic.setMushroomId(mushId);
                detailPic.setPicUrl(picUrl);
                detailPic.setType(PictureTypeEnum.DETAIL);
                pictureMapper.insert(detailPic);
            }
            return Result.success();
        } catch (Exception e) {
            log.error("上传详情图失败（mushroomId={}）：", mushroomId, e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }

    /**
     * 删除单张详情图
     * @param picId 图片ID
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "mushroomCache", allEntries = true)
    public Result deleteMushroomDetailPic(Long picId) {
        Picture pic = pictureMapper.selectById(picId.intValue());
        if (pic == null || !PictureTypeEnum.DETAIL.equals(pic.getType())) {
            return Result.error(MessageConstant.MUSHROOM_DETAIL+ MessageConstant.NOT_EXIST);
        }

        try {
            // 删除MinIO文件
            minioService.deleteFile(pic.getPicUrl());
            // 删除图片记录
            pictureMapper.deleteById(picId.intValue());
            return Result.success();
        } catch (Exception e) {
            log.error("删除详情图失败（picId={}）：", picId, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }


    /**
     * 获取菌种卡片总数（无筛选，直接统计全量）
     */
    @Override
    @Cacheable(key = "'mushroomCount-all'") // 固定缓存key，全量统计缓存
    public Result<Long> getAllMushroomsCount() {
        // 1. 无筛选条件，直接统计总数
        Long count = mushroomMapper.selectCount(new QueryWrapper<>());
        // 2. 返回结果
        return Result.success(count);
    }

    @Override
    public Result<MushroomVO> getMushroomById(Long mushroomId) {
        // 1. 校验ID非空
        if (mushroomId == null || mushroomId <= 0) {
            return Result.error("菌种ID不合法");
        }
        // 2. 根据ID查询菌种基础信息
        MushroomInfo mushroom = baseMapper.selectById(mushroomId);
        if (mushroom == null) {
            return Result.error("未查询到该菌种信息");
        }
        // 3. 实体转VO
        MushroomVO mushroomVO = new MushroomVO();
        BeanUtils.copyProperties(mushroom, mushroomVO);

        // 查询图片
        LambdaQueryWrapper<Picture> coverWrapper = new LambdaQueryWrapper<>();
        coverWrapper.eq(Picture::getMushroomId, mushroomId)
                .eq(Picture::getType, PictureTypeEnum.COVER.getPictureType());
        Picture coverPic = pictureMapper.selectOne(coverWrapper);
        if (coverPic != null) {
            mushroomVO.setCoverUrl(coverPic.getPicUrl());
        }

        LambdaQueryWrapper<Picture> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(Picture::getMushroomId, mushroomId)
                .eq(Picture::getType, PictureTypeEnum.DETAIL.getPictureType())
                .orderByAsc(Picture::getId);
        List<Picture> detailPics = pictureMapper.selectList(detailWrapper);
        mushroomVO.setDetailPicUrls(detailPics);

        return Result.success(mushroomVO);
    }
    /**
     * 新增：菌种概要VO分页列表（列表展示专用，无推荐算法，保留缓存）
     */
    @Override
    public Result<PageResult<MushroomSummaryVO>> getMushroomSummaryPage(MushroomPageQueryDTO mushroomPageQueryDTO) {
        // 1. 构建分页对象+查询条件
        Page<MushroomInfo> page = new Page<>(mushroomPageQueryDTO.getPageNum(), mushroomPageQueryDTO.getPageSize());
        QueryWrapper<MushroomInfo> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(mushroomPageQueryDTO.getMushroomName())) {
            queryWrapper.like("mushroom_name", mushroomPageQueryDTO.getMushroomName());
        }
        queryWrapper.orderByDesc("update_time");

        // 2. 执行分页查询
        IPage<MushroomInfo> mushroomPage = mushroomMapper.selectPage(page, queryWrapper);
        if (mushroomPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 3. 实体转【菌种概要VO】- 仅保留封面图，删除详情图查询（列表专用）
        List<MushroomSummaryVO> summaryVOList = mushroomPage.getRecords().stream()
                .map(mushroomInfo -> {
                    MushroomSummaryVO summaryVO = new MushroomSummaryVO();
                    BeanUtils.copyProperties(mushroomInfo, summaryVO);
                    // 封面图查询（原有逻辑）
                    QueryWrapper<Picture> coverQuery = new QueryWrapper<>();
                    coverQuery.eq("mushroom_id", mushroomInfo.getId())
                            .eq("type", PictureTypeEnum.COVER.getPictureType());
                    Picture coverPic = pictureMapper.selectOne(coverQuery);
                    if (coverPic != null) {
                        summaryVO.setCoverUrl(coverPic.getPicUrl());
                    }
                    return summaryVO;
                }).collect(Collectors.toList());

        // 4. 封装返回
        return Result.success(new PageResult<>(mushroomPage.getTotal(), summaryVOList));
    }
}