package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciBannerStatusEnum;
import cn.edu.seig.MhWeb.mapper.AdminMapper;
import cn.edu.seig.MhWeb.mapper.PictureMapper;
import cn.edu.seig.MhWeb.mapper.PopSciBannerMapper;
import cn.edu.seig.MhWeb.mapper.PopSciContentMapper;

import cn.edu.seig.MhWeb.model.dto.PopSciBannerDTO;
import cn.edu.seig.MhWeb.model.entity.Admin;
import cn.edu.seig.MhWeb.model.entity.Picture;
import cn.edu.seig.MhWeb.model.entity.PopSciBanner;
import cn.edu.seig.MhWeb.model.entity.PopSciContent;
import cn.edu.seig.MhWeb.model.vo.PopSciBannerVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.PopSciBannerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科普内容轮播图服务（复用图文详情图/视频封面）
 */
@CacheConfig(cacheNames = "popSciBannerCache") // 全局缓存名称
@Service
public class PopSciBannerServiceImpl extends ServiceImpl<PopSciBannerMapper, PopSciBanner> implements PopSciBannerService {
    @Autowired
    private PopSciBannerMapper bannerMapper;
    @Autowired
    private PopSciContentMapper contentMapper;
    @Autowired
    private PictureMapper pictureMapper;

    /**
     * 管理员端：分页查询轮播图列表
     */
    @Override
    @Cacheable(key = "'adminBannerList' + #bannerDTO.pageNum + #bannerDTO.pageSize + #bannerDTO.status")
    public Result<PageResult<PopSciBannerVO>> getAllBanners(PopSciBannerDTO bannerDTO) {
        // 1. 分页条件
        Page<PopSciBanner> page = new Page<>(bannerDTO.getPageNum(), bannerDTO.getPageSize());
        // 2. 查询条件（筛选状态）
        QueryWrapper<PopSciBanner> queryWrapper = new QueryWrapper<>();
        if (bannerDTO.getStatus() != null) {
            queryWrapper.eq("status", bannerDTO.getStatus());
        }
        queryWrapper.orderByDesc("sort").orderByDesc("create_time");

        // 3. 分页查询
        IPage<PopSciBanner> bannerPage = bannerMapper.selectPage(page, queryWrapper);
        if (bannerPage.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, null));
        }

        // 4. 转换VO（关联科普内容+图片）
        List<PopSciBannerVO> voList = bannerPage.getRecords().stream()
                .map(banner -> {
                    PopSciBannerVO vo = new PopSciBannerVO();
                    BeanUtils.copyProperties(banner, vo);
                    // 关联科普内容信息
                    PopSciContent content = contentMapper.selectById(banner.getContentId());
                    if (content != null) {
                        vo.setContentTitle(content.getTitle());
                        vo.setContentType(content.getContentType().getCode());
                        // 提取科普内容的图片（图文详情图首张/视频封面）
                        vo.setBannerUrl(getContentPicUrl(banner.getContentId(), content.getContentType().getCode()));
                    }
                    return vo;
                }).collect(Collectors.toList());

        return Result.success(new PageResult<>(bannerPage.getTotal(), voList));
    }

    /**
     * 管理员端：添加轮播图（仅关联科普内容，不操作图片）
     */
    @Override
    @CacheEvict(cacheNames = "popSciBannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result addBanner(Long contentId) {
        // 1. 校验科普内容：存在 + 审核通过
        PopSciContent content = contentMapper.selectById(contentId);
        if (content == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }
        if (content.getContentStatus().getId()!= 2) {
            return Result.error("仅审核通过的科普内容可加入轮播图");
        }

        // 2. 校验：该科普内容是否已加入轮播图（唯一索引兜底，代码层二次校验）
        QueryWrapper<PopSciBanner> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("content_id", contentId);
        if (bannerMapper.selectOne(checkWrapper) != null) {
            return Result.error("该科普内容已在轮播图列表中，无需重复添加");
        }

        // 3. 新增轮播图记录（默认启用，排序为0）
        PopSciBanner banner = new PopSciBanner();
        banner.setContentId(contentId)
                .setSort(0)
                .setStatus(PopSciBannerStatusEnum.ENABLE.getId());

        if (bannerMapper.insert(banner) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 管理员端：更新轮播图排序/状态
     */
    @Override
    @CacheEvict(cacheNames = "popSciBannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result updateBanner(Long id, Integer sort, Integer status) {
        // 1. 校验轮播图存在
        PopSciBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.error("轮播图记录不存在");
        }

        // 2. 更新字段（仅传了值的字段才更新）
        if (sort != null) {
            banner.setSort(sort);
        }
        if (status != null) {
            // 校验状态有效值
            if (!status.equals(PopSciBannerStatusEnum.ENABLE.getId())
                    && !status.equals(PopSciBannerStatusEnum.DISABLE.getId())) {
                return Result.error("轮播图状态仅支持0（禁用）/1（启用）");
            }
            banner.setStatus(status);
        }

        // 3. 执行更新
        if (bannerMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 管理员端：删除单张轮播图（仅删轮播关系，不删科普内容/图片）
     */
    @Override
    @CacheEvict(cacheNames = "popSciBannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBanner(Long id) {
        PopSciBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.error("轮播图记录不存在");
        }

        // 仅删除轮播图关系记录，不操作科普内容/图片
        if (bannerMapper.deleteById(id) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 管理员端：批量删除轮播图
     */
    @Override
    @CacheEvict(cacheNames = "popSciBannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBanners(List<Long> ids) {
        if (ids.isEmpty()) {
            return Result.error(MessageConstant.BANNER_ID + MessageConstant.NOT_NULL);
        }

        // 批量删除轮播图关系，不影响科普内容/图片
        if (bannerMapper.deleteBatchIds(ids) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 用户端：获取展示的轮播图（启用状态 + 复用科普图片）
     */
    @Override
    @Cacheable(key = "'userBannerList'")
    public Result<List<PopSciBannerVO>> getBannerList() {
        // 1. 查询启用的轮播图（按排序+创建时间倒序，取前9条）
        QueryWrapper<PopSciBanner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", PopSciBannerStatusEnum.ENABLE.getId())
                .orderByDesc("sort")
                .orderByDesc("create_time")
                .last("LIMIT 9");
        List<PopSciBanner> bannerList = bannerMapper.selectList(queryWrapper);

        if (bannerList.isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, List.of());
        }

        // 2. 转换VO（关联科普内容+图片）
        List<PopSciBannerVO> voList = bannerList.stream()
                .map(banner -> {
                    PopSciBannerVO vo = new PopSciBannerVO();
                    BeanUtils.copyProperties(banner, vo);
                    // 关联科普内容
                    PopSciContent content = contentMapper.selectById(banner.getContentId());
                    if (content != null) {
                        vo.setContentId(content.getId());
                        vo.setContentTitle(content.getTitle());
                        vo.setContentType(content.getContentType().getCode());
                        // 提取图片URL（图文详情图首张/视频封面）
                        vo.setBannerUrl(getContentPicUrl(content.getId(), content.getContentType().getCode()));
                    }
                    return vo;
                })
                // 过滤无图片的轮播图
                .filter(vo -> StringUtils.hasText(vo.getBannerUrl()))
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 工具方法：提取科普内容的图片URL（图文详情图首张/视频封面）
     */
    private String getContentPicUrl(Long contentId, String contentType) {
        QueryWrapper<Picture> picQuery = new QueryWrapper<>();
        picQuery.eq("content_id", contentId);

        if ("article".equals(contentType)) {
            // 图文类：取首张图文详情图
            picQuery.eq("type", PictureTypeEnum.ARTICLE_DETAIL.getPictureType())
                    .orderByAsc("id")
                    .last("LIMIT 1");
        } else if ("video".equals(contentType)) {
            // 视频类：取视频封面
            picQuery.eq("type", PictureTypeEnum.VIDEO_COVER.getPictureType())
                    .last("LIMIT 1");
        }

        Picture pic = pictureMapper.selectOne(picQuery);
        return pic != null ? pic.getPicUrl() : "";
    }
}