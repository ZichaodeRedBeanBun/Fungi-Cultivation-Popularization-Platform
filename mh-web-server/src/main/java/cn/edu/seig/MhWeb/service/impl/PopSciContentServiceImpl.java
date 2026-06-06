package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.enumeration.PictureTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import cn.edu.seig.MhWeb.mapper.*;
import cn.edu.seig.MhWeb.model.dto.PictureDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.*;
import cn.edu.seig.MhWeb.model.dto.UserContentQuery.UserContentQueryDTO;
import cn.edu.seig.MhWeb.model.entity.*;
import cn.edu.seig.MhWeb.model.vo.PictureVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciContentService;
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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 科普内容业务实现类（适配Controller修改 + 文件/图片删除逻辑）
 *
 * @author su
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "popsciCache") // 科普内容缓存前缀
public class PopSciContentServiceImpl extends ServiceImpl<PopSciContentMapper, PopSciContent> implements IPopSciContentService {

    @Autowired
    private PopSciContentMapper popSciContentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PictureMapper pictureMapper;
    @Autowired
    private MinioService minioService;

    @Autowired
    private MushroomMapper mushroomMapper;

    /**
     * 获取科普内容分页列表
     */
    @Override
    @Cacheable(key = "#popSciContentPageQueryDTO.pageNum + '-' + #popSciContentPageQueryDTO.pageSize + '-' + #popSciContentPageQueryDTO.mushroomName + '-' + #popSciContentPageQueryDTO.title + '-' + #popSciContentPageQueryDTO.contentType + '-' + #popSciContentPageQueryDTO.authorName + '-' + #popSciContentPageQueryDTO.contentStatus+'-'+#popSciContentPageQueryDTO.id")
    public Result<PageResult<PopSciContentVO>> getAllPopSciContents(PopSciContentPageQueryDTO popSciContentPageQueryDTO) {
        Page<PopSciContent> page = new Page<>(popSciContentPageQueryDTO.getPageNum(), popSciContentPageQueryDTO.getPageSize());
        QueryWrapper<PopSciContent> queryWrapper = new QueryWrapper<>();
        if(popSciContentPageQueryDTO.getId()!=null){
            queryWrapper.like("id", popSciContentPageQueryDTO.getId());
        }
        // 标题模糊查询
        if (StringUtils.hasText(popSciContentPageQueryDTO.getTitle())) {
            queryWrapper.like("title", popSciContentPageQueryDTO.getTitle());
        }

        // 菌种名称模糊查询（关联mh_mushroom_info表）
        if (StringUtils.hasText(popSciContentPageQueryDTO.getMushroomName())) {
            QueryWrapper<MushroomInfo> mhWrapper = new QueryWrapper<>();
            mhWrapper.like("mushroom_name", popSciContentPageQueryDTO.getMushroomName());
            List<MushroomInfo> mushroomList = mushroomMapper.selectList(mhWrapper);

            // 无匹配菌种，直接返回空页
            if (CollectionUtils.isEmpty(mushroomList)) {
                return Result.success(new PageResult<>(0L, null));
            }

            // 提取菌种ID列表，in查询
            List<Long> mushroomIdList = mushroomList.stream()
                    .map(MushroomInfo::getId)
                    .collect(Collectors.toList());
            queryWrapper.in("mushroom_id", mushroomIdList);
        }

        // 内容类型精准查询（article/video）
        if (popSciContentPageQueryDTO.getContentType() != null) {
            PopSciContentTypeEnum typeEnum = popSciContentPageQueryDTO.getContentType();
            queryWrapper.eq("content_type", typeEnum.name().toLowerCase()); // 匹配数据库的article/video
        }

        // 科普号名称模糊查询（关联mh_user表）
        if (StringUtils.hasText(popSciContentPageQueryDTO.getAuthorName())) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.like("username", popSciContentPageQueryDTO.getAuthorName());
            List<User> userList = userMapper.selectList(userWrapper);

            // 无匹配科普号，直接返回空页
            if (CollectionUtils.isEmpty(userList)) {
                return Result.success(new PageResult<>(0L, null));
            }

            // 提取用户ID列表，in查询
            List<Long> userIdList = userList.stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
            queryWrapper.in("author_id", userIdList);
        }

        // 内容状态精准查询
        if (popSciContentPageQueryDTO.getContentStatus() != null) {
            queryWrapper.eq("content_status", popSciContentPageQueryDTO.getContentStatus().getId());
        }

        IPage<PopSciContent> contentPage = popSciContentMapper.selectPage(page, queryWrapper);
        List<PopSciContent> contentList = contentPage.getRecords();
        if (CollectionUtils.isEmpty(contentList)) {
            return Result.success(new PageResult<>(contentPage.getTotal(), null));
        }

        List<Long> contentIdList = contentList.stream()
                .map(PopSciContent::getId)
                .collect(Collectors.toList());

        QueryWrapper<Picture> picQueryWrapper = new QueryWrapper<>();
        picQueryWrapper.in("content_id", contentIdList);
        List<Picture> pictureList = pictureMapper.selectList(picQueryWrapper);

        Map<Long, List<Picture>> contentPicMap = pictureList.stream()
                .collect(Collectors.groupingBy(Picture::getContentId));

        List<PopSciContentVO> voList = contentList.stream()
                .map(content -> {
                    PopSciContentVO vo = new PopSciContentVO();
                    BeanUtils.copyProperties(content, vo);

                    User user = userMapper.selectById(content.getAuthorId());
                    if (user != null) {
                        vo.setAuthorName(user.getUsername());
                    }

                    MushroomInfo mushroom = mushroomMapper.selectById(content.getMushroomId());
                    if (mushroom != null) {
                        vo.setMushroomName(mushroom.getMushroomName());
                    }

                    List<Picture> pics = contentPicMap.getOrDefault(content.getId(), Collections.emptyList());
                    vo.setPictureList(pics); // 直接设置Picture列表
                    if (!pics.isEmpty()) {
                        if (content.getContentType()==PopSciContentTypeEnum.ARTICLE) {
                            vo.setArticleCoverUrl(pics.get(0).getPicUrl());
                        } else if (content.getContentType()==PopSciContentTypeEnum.VIDEO) {
                            vo.setVideoCoverUrl(pics.get(0).getPicUrl()); // 视频封面
                        }
                    }
                    return vo;
                }).collect(Collectors.toList());

        return Result.success(new PageResult<>(contentPage.getTotal(), voList));
    }

    /**
     * 新增科普内容（补充字段初始化）
     */
    @Override
    public Result addPopSciContent(PopSciContentAddFileDTO addFileDTO,
                                   List<MultipartFile> articleDetailFiles,
                                   MultipartFile videoCoverFile,
                                   MultipartFile videoFile) {
        handleFileUpload(addFileDTO, articleDetailFiles, videoCoverFile, videoFile);

        PopSciContent content = new PopSciContent();
        BeanUtils.copyProperties(addFileDTO, content);

        // 处理默认值
        if (content.getPublishDate() == null) {
            content.setPublishDate(LocalDate.now());
        }
        content.setContentStatus(PopSciContentStatusEnum.PENDING);

        content.setPraiseCount(0);
        content.setReviewCount(0);
        content.setCollectCount(0);

        boolean success = save(content);
        if (!success) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ADD + MessageConstant.FAILED);
        }
        
        Long newContentId = content.getId();
        saveOrUpdatePictures(newContentId,
                addFileDTO.getArticleDetailPicUrl(),
                addFileDTO.getVideoCoverUrl(),
                addFileDTO.getMushroomId());

        return Result.success(MessageConstant.POPSCI_CONTENT + MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 处理文件上传
     */
    private void handleFileUpload(PopSciContentAddFileDTO addFileDTO,
                                  List<MultipartFile> articleDetailFiles,
                                  MultipartFile videoCoverFile,
                                  MultipartFile videoFile) {
        PopSciContentTypeEnum contentType = addFileDTO.getContentType();
        if (contentType == null) {
            throw new RuntimeException("内容类型不能为空");
        }

        if (PopSciContentTypeEnum.ARTICLE.equals(contentType)) {
            if (articleDetailFiles != null && !articleDetailFiles.isEmpty()) {
                List<String> detailPicUrls = new ArrayList<>();
                for (MultipartFile file : articleDetailFiles) {
                    String url = minioService.uploadFile(file, "picture");
                    detailPicUrls.add(url);
                }
                addFileDTO.setArticleDetailPicUrl(detailPicUrls);
            }
        } else if (PopSciContentTypeEnum.VIDEO.equals(contentType)) {
            if (videoCoverFile != null && !videoCoverFile.isEmpty()) {
                String coverUrl = minioService.uploadFile(videoCoverFile, "cover");
                addFileDTO.setVideoCoverUrl(coverUrl);
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                String videoUrl = minioService.uploadFile(videoFile, "video");
                addFileDTO.setVideoUrl(videoUrl);
            }
        }
    }
    /**
     * 编辑科普内容
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result updatePopSciContent(PopSciContentAddFileDTO addFileDTO,
                                      List<MultipartFile> articleDetailFiles,
                                      MultipartFile videoCoverFile,
                                      MultipartFile videoFile) {
        // 1. 校验数据存在
        Long contentId = addFileDTO.getId();
        if (contentId == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_NULL);
        }

        PopSciContent existContent = popSciContentMapper.selectById(contentId);
        if (existContent == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }

        handleUpdateFileUpload(addFileDTO, existContent, articleDetailFiles, videoCoverFile, videoFile);

        PopSciContent content = new PopSciContent();
        BeanUtils.copyProperties(addFileDTO, content);
        content.setId(contentId);
        content.setContentStatus(PopSciContentStatusEnum.PENDING);

        int updateCount = popSciContentMapper.updateById(content);
        if (updateCount == 0) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        pictureMapper.delete(new LambdaQueryWrapper<Picture>()
                .eq(Picture::getContentId, contentId));
        saveOrUpdatePictures(contentId,
                addFileDTO.getArticleDetailPicUrl(),
                addFileDTO.getVideoCoverUrl(),
                existContent.getMushroomId());

        return Result.success(MessageConstant.POPSCI_CONTENT + MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 处理文件上传（区分图文/视频）
     */
    private void handleUpdateFileUpload(PopSciContentAddFileDTO addFileDTO,
                                        PopSciContent existContent,
                                        List<MultipartFile> articleDetailFiles,
                                        MultipartFile videoCoverFile,
                                        MultipartFile videoFile) {
        PopSciContentTypeEnum contentType = addFileDTO.getContentType();
        if (contentType == null) {
            throw new RuntimeException("内容类型不能为空");
        }

        // 图文类型：合并新旧URL
        if (PopSciContentTypeEnum.ARTICLE.equals(contentType)) {
            List<String> oldPicUrls = addFileDTO.getArticleDetailPicUrl() == null
                    ? new ArrayList<>()
                    : addFileDTO.getArticleDetailPicUrl();

            // 处理新上传的文件
            if (articleDetailFiles != null && !articleDetailFiles.isEmpty()) {
                List<String> newPicUrls = new ArrayList<>();
                for (MultipartFile file : articleDetailFiles) {
                    String url = minioService.uploadFile(file, "picture");
                    newPicUrls.add(url);
                }
                oldPicUrls.addAll(newPicUrls);
            }

            addFileDTO.setArticleDetailPicUrl(new ArrayList<>(new LinkedHashSet<>(oldPicUrls)));
        }
        // 视频类型：直接替换URL
        else if (PopSciContentTypeEnum.VIDEO.equals(contentType)) {
            if (videoCoverFile != null && !videoCoverFile.isEmpty()) {
                String coverUrl = minioService.uploadFile(videoCoverFile, "cover");
                addFileDTO.setVideoCoverUrl(coverUrl);
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                String videoUrl = minioService.uploadFile(videoFile, "video");
                addFileDTO.setVideoUrl(videoUrl);
            }
        }
    }
    /**
     * 封装图片保存逻辑
     */
    private void saveOrUpdatePictures(Long contentId,
                                      List<String> articleDetailPicUrl,
                                      String videoCoverUrl,
                                      Long mushroomId) {
        if (articleDetailPicUrl != null && !articleDetailPicUrl.isEmpty()) {
            for (String url : articleDetailPicUrl) {
                if (url != null && !url.trim().isEmpty()) {
                    Picture pic = new Picture();
                    pic.setPicUrl(url);
                    pic.setMushroomId(mushroomId);
                    pic.setContentId(contentId);
                    pic.setType(PictureTypeEnum.ARTICLE_DETAIL);
                    pictureMapper.insert(pic);
                }
            }
        }

        if (videoCoverUrl != null && !videoCoverUrl.trim().isEmpty()) {
            Picture pic = new Picture();
            pic.setPicUrl(videoCoverUrl);
            pic.setMushroomId(mushroomId);
            pic.setContentId(contentId);
            pic.setType(PictureTypeEnum.VIDEO_COVER);
            pictureMapper.insert(pic);
        }
    }
    /**
     * 审核科普内容
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true) // 审核后清空缓存
    public Result auditPopSciContent(PopSciContentAuditDTO popSciContentAuditDTO) {
        // 1. 校验数据存在（Long转Integer）
        Long contentId = popSciContentAuditDTO.getId();
        if (contentId == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_NULL);
        }
        PopSciContent existContent = popSciContentMapper.selectById(contentId);
        if (existContent == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }

        // 2. 校验审核状态
        if (existContent.getContentStatus().getId() != PopSciContentStatusEnum.PENDING.getId()) {
            return Result.error("仅待审核的内容可进行审核操作");
        }
        if (popSciContentAuditDTO.getContentStatus().getId() != PopSciContentStatusEnum.PASSED.getId()
                && popSciContentAuditDTO.getContentStatus().getId() != PopSciContentStatusEnum.REJECTED.getId()) {
            return Result.error("审核状态只能是2（通过）或3（驳回）");
        }

        // 3. 驳回时校验驳回原因
        if (popSciContentAuditDTO.getContentStatus().getId() == PopSciContentStatusEnum.REJECTED.getId()
                && !StringUtils.hasText(popSciContentAuditDTO.getRejectReason())) {
            return Result.error("审核驳回时必须填写驳回原因");
        }

        // 4. 执行审核更新
        PopSciContent content = new PopSciContent();
        content.setId(contentId);
        content.setContentStatus(popSciContentAuditDTO.getContentStatus());
        content.setRejectReason(popSciContentAuditDTO.getRejectReason());
        content.setAuditTime(LocalDateTime.now());

        int updateCount = popSciContentMapper.updateById(content);
        if (updateCount == 0) {
            return Result.error(MessageConstant.CONTENT_AUDIT + MessageConstant.FAILED);
        }
        return Result.success();
    }

    /**
     * 删除单个科普内容
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result deletePopSciContent(Long id) {
        if (id == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_NULL);
        }
        Integer contentId = id.intValue();

        PopSciContent existContent = popSciContentMapper.selectById(contentId);
        if (existContent == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }

        try {
            QueryWrapper<Picture> picQuery = new QueryWrapper<>();
            picQuery.eq("content_id", contentId);
            List<Picture> picList = pictureMapper.selectList(picQuery);

            PopSciContentTypeEnum typeEnum = PopSciContentTypeEnum.getByCode(existContent.getContentType().getCode());
            // 图文类型：删除图文详情图
                for (Picture pic : picList) {
                    if (PictureTypeEnum.ARTICLE_DETAIL.equals(pic.getType())) {
                        minioService.deleteFile(pic.getPicUrl());
                    }
                }
            }
            // 视频类型：删除视频文件 + 视频封面
                // 删除视频文件
                    minioService.deleteFile(existContent.getVideoUrl());
                }
                // 删除视频封面
                for (Picture pic : picList) {
                    if (PictureTypeEnum.VIDEO_COVER.equals(pic.getType())) {
                        minioService.deleteFile(pic.getPicUrl());
                    }
                }
            }

            if (!picList.isEmpty()) {
                pictureMapper.deleteBatchIds(picList.stream().map(Picture::getId).collect(Collectors.toList()));
            }

            // TODO: 待补充 - 删除该科普内容关联的评论数据（需注入CommentMapper，执行delete操作）

            // 删除图文详情图
            popSciContentMapper.deleteById(contentId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除科普内容失败（id={}）：", id, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }
    /**
     * 上传图文详情图（纯工具：仅上传，返回URL）
     */
    @Override
    public Result<String> uploadArticleDetailPic(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }

        try {
            String picUrl = minioService.uploadFile(file, "picture");

            // 3. 仅返回URL
            return Result.success(MessageConstant.SUCCESS, picUrl);
        } catch (Exception e) {
            log.error("上传图文详情图失败：", e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }

    /**
     * 上传视频封面
     */
    @Override
    public Result<String> uploadVideoCover(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }

        try {
            // 直接上传文件到MinIO
            String coverUrl = minioService.uploadFile(file, "cover");
            return Result.success(coverUrl);
        } catch (Exception e) {
            log.error("上传视频封面失败：", e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }

    /**
     * 上传视频文件
     */
    @Override
    public Result<String> uploadVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }

        try {
            String videoUrl = minioService.uploadFile(file, "popsci_video");
            return Result.success(videoUrl);
        } catch (Exception e) {
            log.error("上传视频文件失败：", e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }
    /**
     * 删除单张图文详情图
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result deleteArticleDetailPic(Long picId) {
        Picture pic = pictureMapper.selectById(picId.intValue());
        if (pic == null || !PictureTypeEnum.ARTICLE_DETAIL.equals(pic.getType())) {
            return Result.error("图文详情图不存在");
        }

        try {
            // 删除MinIO文件
            minioService.deleteFile(pic.getPicUrl());
            // 删除图片记录
            pictureMapper.deleteById(picId.intValue());
            return Result.success();
        } catch (Exception e) {
            log.error("删除图文详情图失败（picId={}）：", picId, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }

    /**
     * 删除视频封面
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result deleteVideoCover(Long picId) {
        Picture pic = pictureMapper.selectById(picId.intValue());
        if (pic == null || !PictureTypeEnum.VIDEO_COVER.equals(pic.getType())) {
            return Result.error("视频封面不存在");
        }

        try {
            minioService.deleteFile(pic.getPicUrl());
            pictureMapper.deleteById(picId.intValue());
            return Result.success();
        } catch (Exception e) {
            log.error("删除视频封面失败（picId={}）：", picId, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }
    /**
     * 批量删除科普内容
     */
    @Override
    @CacheEvict(cacheNames = "popsciCache", allEntries = true)
    public Result deletePopSciContents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }

        // 2. 类型转换（List<Long>→List<Integer>）
        List<Integer> contentIds = ids.stream()
                .filter(id -> id != null)
                .map(Long::intValue)
                .collect(Collectors.toList());
        if (contentIds.isEmpty()) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }

        try {
            // 3. 校验数据存在
            List<PopSciContent> contentList = popSciContentMapper.selectBatchIds(contentIds);
            if (contentList.isEmpty()) {
                return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
            }

            // 4. 批量处理文件和图片
            for (PopSciContent content : contentList) {
                // 查询关联图片
                QueryWrapper<Picture> picQuery = new QueryWrapper<>();
                picQuery.eq("content_id", content.getId());
                List<Picture> picList = pictureMapper.selectList(picQuery);

                // 区分类型删除文件
                PopSciContentTypeEnum typeEnum = PopSciContentTypeEnum.getByCode(content.getContentType().getCode());
                if (PopSciContentTypeEnum.ARTICLE.equals(typeEnum)) {
                    picList.stream().filter(pic -> PictureTypeEnum.ARTICLE_DETAIL.equals(pic.getType()))
                            .forEach(pic -> minioService.deleteFile(pic.getPicUrl()));
                }
                if (PopSciContentTypeEnum.VIDEO.equals(typeEnum)) {
                    // 删除视频文件
                    if (StringUtils.hasText(content.getVideoUrl())) {
                        minioService.deleteFile(content.getVideoUrl());
                    }
                    // 删除视频封面
                    picList.stream().filter(pic -> PictureTypeEnum.VIDEO_COVER.equals(pic.getType()))
                            .forEach(pic -> minioService.deleteFile(pic.getPicUrl()));
                }

                // 删除图片记录
                if (!picList.isEmpty()) {
                    pictureMapper.deleteBatchIds(picList.stream().map(Picture::getId).collect(Collectors.toList()));
                }

                // TODO: 待补充 - 批量删除评论（按content_id批量删除）
            }

            // 5. 批量删除科普内容
            popSciContentMapper.deleteBatchIds(contentIds);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除科普内容失败（ids={}）：", ids, e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
    }
    /**
     * 推荐图文
     */
    public Result<List<PopSciContentSummaryVO>> getRecommendedArticleContents() {
        String contentType = PopSciContentTypeEnum.ARTICLE.name().toLowerCase();
        int targetCount = 12;
        // 1. 直接获取精简VO全量列表（底层已处理推荐逻辑+转精简VO）
        List<PopSciContentSummaryVO> allSummaryVO = getRecommendedContentByType(contentType, Integer.MAX_VALUE);
        // 2. 直接对精简VO随机抽取，无任何中间转换
        List<PopSciContentSummaryVO> resultVO = allSummaryVO.stream().limit(targetCount).collect(Collectors.toList());
//        List<PopSciContentSummaryVO> randomVO = randomSelect(allSummaryVO, targetCount);
        // 3. 直接返回精简VO，一步到位
        return Result.success(resultVO);
    }

    /**
     * 推荐视频
     */
    public Result<List<PopSciContentSummaryVO>> getRecommendedVideoContents() {
        String contentType = PopSciContentTypeEnum.VIDEO.name().toLowerCase();
        int targetCount = 8;
        // 1. 直接获取精简VO全量列表（底层已处理推荐逻辑+转精简VO）
        List<PopSciContentSummaryVO> allSummaryVO = getRecommendedContentByType(contentType, Integer.MAX_VALUE);
        // 2. 直接对精简VO随机抽取，无任何中间转换
        List<PopSciContentSummaryVO> resultVO = allSummaryVO.stream().limit(targetCount).collect(Collectors.toList());
//        List<PopSciContentSummaryVO> randomVO = randomSelect(allSummaryVO, targetCount);
        // 3. 直接返回精简VO，一步到位
        return Result.success(resultVO);
    }

    private <T> List<T> randomSelect(List<T> allList, int targetCount) {
        if (CollectionUtils.isEmpty(allList)) return new ArrayList<>();
        if (allList.size() <= targetCount) return allList;
        List<T> tempList = new ArrayList<>(allList);
        Collections.shuffle(tempList);
        return tempList.subList(0, targetCount);
    }
    private List<PopSciContentSummaryVO> getRecommendedContentByType(String contentType, int targetCount) {
        Long userId = BaseContext.getCurrentId();

        if (userId == null) {
            List<PopSciContent> contentList = popSciContentMapper.getRandomHighWeightContentByType(contentType, targetCount);
            return convertToPopSciContentSummaryVO(contentList);
        }
        List<Long> favoriteContentIds = popSciContentMapper.getFavoriteContentIdsByUserId(userId);
        if (CollectionUtils.isEmpty(favoriteContentIds)) {
            List<PopSciContent> contentList = popSciContentMapper.getRandomHighWeightContentByType(contentType, targetCount);
            return convertToPopSciContentSummaryVO(contentList);
        }

        List<Long> mushroomIds = popSciContentMapper.getFavoriteMushroomIds(favoriteContentIds);
        Map<Long, Long> mushroomFrequency = mushroomIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Long> sortedMushroomIds = mushroomFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<PopSciContent> recommendedContent = popSciContentMapper.getRecommendedByMushroomIdsAndTypeWithWeight(
                sortedMushroomIds, favoriteContentIds, contentType, 80);

        List<PopSciContent> finalRecommendations = limitSingleMushroomRatio(recommendedContent, targetCount);

        if (finalRecommendations.size() < targetCount) {
            int fillCount = targetCount - finalRecommendations.size();
            List<PopSciContent> fillContent = popSciContentMapper.getRandomHighWeightContentByType(contentType, fillCount);
            Set<Long> existingIds = finalRecommendations.stream().map(PopSciContent::getId).collect(Collectors.toSet());
            fillContent = fillContent.stream()
                    .filter(content -> !existingIds.contains(content.getId()))
                    .collect(Collectors.toList());
            finalRecommendations.addAll(fillContent.subList(0, Math.min(fillContent.size(), fillCount)));
        }

        return convertToPopSciContentSummaryVO(finalRecommendations);
    }

    private List<PopSciContentSummaryVO> convertToPopSciContentSummaryVO(List<PopSciContent> contentList) {
        if (CollectionUtils.isEmpty(contentList)) {
            return Collections.emptyList();
        }

        List<Long> contentIdList = contentList.stream().map(PopSciContent::getId).collect(Collectors.toList());
        List<Long> mushroomIdList = contentList.stream().map(PopSciContent::getMushroomId).collect(Collectors.toList());
        List<Long> authorIdList = contentList.stream().map(PopSciContent::getAuthorId).collect(Collectors.toList());

        Map<Long, List<Picture>> contentPictureMap = getContentPictureMap(contentIdList);

        List<MushroomInfo> mushroomList = mushroomMapper.selectBatchIds(mushroomIdList);
        Map<Long, MushroomInfo> mushroomMap = mushroomList.stream()
                .collect(Collectors.toMap(MushroomInfo::getId, Function.identity(), (k1, k2) -> k1));

        List<User> userList = userMapper.selectBatchIds(authorIdList);
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity(), (k1, k2) -> k1));

        return contentList.stream().map(content -> {
            PopSciContentSummaryVO summaryVO = new PopSciContentSummaryVO();
            summaryVO.setId(content.getId());
            summaryVO.setContentType(content.getContentType());
            summaryVO.setTitle(content.getTitle());
            summaryVO.setPublishDate(content.getPublishDate());
            summaryVO.setDescription(content.getDescription());

            // 补充作者名（非数据库字段）
            User user = userMap.get(content.getAuthorId());
            if (user != null) {
                summaryVO.setAuthorName(user.getUsername());
            }

            // 补充菌种名（非数据库字段）
            MushroomInfo mushroom = mushroomMap.get(content.getMushroomId());
            if (mushroom != null) {
                summaryVO.setMushroomName(mushroom.getMushroomName());
            }

            List<Picture> picList = contentPictureMap.getOrDefault(content.getId(), Collections.emptyList());
            if (!picList.isEmpty()) {
                String firstPicUrl = picList.get(0).getPicUrl();
                if (PopSciContentTypeEnum.ARTICLE.equals(content.getContentType())) {
                    summaryVO.setArticleCoverUrl(firstPicUrl);
                } else if (PopSciContentTypeEnum.VIDEO.equals(content.getContentType())) {
                    summaryVO.setVideoCoverUrl(firstPicUrl);
                }
            }

            return summaryVO;
        }).collect(Collectors.toList());
    }
    private List<PopSciContent> limitSingleMushroomRatio(List<PopSciContent> contentList, int targetCount) {
        if (CollectionUtils.isEmpty(contentList)) {
            return new ArrayList<>();
        }

        Map<Long, List<PopSciContent>> mushroomContentMap = contentList.stream()
                .collect(Collectors.groupingBy(PopSciContent::getMushroomId));

        int maxPerMushroom = (int) Math.floor(targetCount * 0.6);
        List<PopSciContent> result = new ArrayList<>();

        for (Map.Entry<Long, List<PopSciContent>> entry : mushroomContentMap.entrySet()) {
            if (result.size() >= targetCount) {
                break;
            }
            List<PopSciContent> mushroomContent = entry.getValue();
            int takeCount = Math.min(mushroomContent.size(), Math.min(maxPerMushroom, targetCount - result.size()));
            result.addAll(mushroomContent.subList(0, takeCount));
        }

        return result;
    }
    private List<PopSciContentVO> convertToPopSciContentVO(List<PopSciContent> contentList) {
        if (CollectionUtils.isEmpty(contentList)) {
            return Collections.emptyList();
        }

        List<Long> contentIdList = contentList.stream().map(PopSciContent::getId).collect(Collectors.toList());
        List<Long> mushroomIdList = contentList.stream().map(PopSciContent::getMushroomId).collect(Collectors.toList());
        List<Long> authorIdList = contentList.stream().map(PopSciContent::getAuthorId).collect(Collectors.toList());

        Map<Long, List<Picture>> contentPictureMap = getContentPictureMap(contentIdList);

        List<MushroomInfo> mushroomList = mushroomMapper.selectBatchIds(mushroomIdList);
        Map<Long, MushroomInfo> mushroomMap = mushroomList.stream()
                .collect(Collectors.toMap(MushroomInfo::getId, Function.identity(), (k1, k2) -> k1));

        List<User> userList = userMapper.selectBatchIds(authorIdList);
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity(), (k1, k2) -> k1));

        List<PopSciContentVO> voList = contentList.stream().map(content -> {
            PopSciContentVO vo = new PopSciContentVO();
            BeanUtils.copyProperties(content, vo);

            User user = userMap.get(content.getAuthorId());
            if (user != null) {
                vo.setAuthorName(user.getUsername());
            }

            MushroomInfo mushroom = mushroomMap.get(content.getMushroomId());
            if (mushroom != null) {
                vo.setMushroomName(mushroom.getMushroomName());
            }

            return vo;
        }).collect(Collectors.toList());

        batchSetPopSciContentPictureField(voList, contentList, contentPictureMap);

        return voList;
    }
    /**
     * 获取科普内容总数
     */
    @Override
    public Result<Long> getAllPopSciContentsCount(Long mushroomId, String contentType) {
        // 1. 构建科普内容查询条件
        QueryWrapper<PopSciContent> queryWrapper = new QueryWrapper<>();

        // 2. 按菌种ID筛选（精确匹配，直接使用传入的mushroomId）
        if (mushroomId != null) {
            queryWrapper.eq("mushroom_id", mushroomId); // 直接匹配科普内容表的mushroom_id字段
        }

        // 3. 按内容类型筛选（图文/视频，精确匹配）
        if (contentType != null && !contentType.isEmpty()) {
            queryWrapper.eq("content_type", contentType); // 数据库字段名需和实体对齐
        }

        // 4. 统计总数
        Long count = popSciContentMapper.selectCount(queryWrapper);
        // 5. 返回结果
        return Result.success(count);
    }
    /**
     * 科普内容点赞：点赞数+1（适配现有代码规范）
     */
    @Override
    @CacheEvict(allEntries = true) // 点赞数更新，清空科普内容缓存（和更新/删除逻辑一致）
    public Result praisePopSci(Long popsciId) {
        // 1. 参数非空校验
        if (popsciId == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }
        // 2. 校验科普内容是否存在
        PopSciContent existContent = popSciContentMapper.selectById(popsciId);
        if (existContent == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }
        // 3. 校验内容状态（仅审核通过的内容可点赞）
        if (!PopSciContentStatusEnum.PASSED.equals(existContent.getContentStatus())) {
            return Result.error("仅审核通过的科普内容可进行点赞操作");
        }

        try {
            // 4. 构建更新对象（仅更新点赞数，其他字段不改动）
            PopSciContent content = new PopSciContent();
            content.setId(popsciId);
            // 点赞数+1（空值则置为1）
            content.setPraiseCount(Optional.ofNullable(existContent.getPraiseCount()).orElse(0) + 1);
            // 5. 执行更新
            popSciContentMapper.updateById(content);
            log.info("科普内容点赞成功，ID：{}，当前点赞数：{}", popsciId, content.getPraiseCount());
            return Result.success(MessageConstant.LIKE + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("科普内容点赞失败，ID：{}", popsciId, e);
            return Result.error(MessageConstant.LIKE + MessageConstant.FAILED);
        }
    }

    /**
     * 科普内容取消点赞：点赞数-1，保底0（适配现有代码规范）
     */
    @Override
    @CacheEvict(allEntries = true) // 取消点赞也刷新缓存
    public Result cancelPraisePopSci(Long popsciId) {
        // 1. 参数非空校验
        if (popsciId == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.ID + MessageConstant.NOT_NULL);
        }
        // 2. 校验科普内容是否存在
        PopSciContent existContent = popSciContentMapper.selectById(popsciId);
        if (existContent == null) {
            return Result.error(MessageConstant.POPSCI_CONTENT + MessageConstant.NOT_EXIST);
        }

        try {
            // 3. 构建更新对象（仅更新点赞数）
            PopSciContent content = new PopSciContent();
            content.setId(popsciId);
            // 点赞数-1，保底0（避免负数）
            int newPraiseCount = Math.max(0, Optional.ofNullable(existContent.getPraiseCount()).orElse(0) - 1);
            content.setPraiseCount(newPraiseCount);
            // 4. 执行更新
            popSciContentMapper.updateById(content);
            log.info("科普内容取消点赞成功，ID：{}，当前点赞数：{}", popsciId, newPraiseCount);
            return Result.success("取消" + MessageConstant.LIKE + MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("科普内容取消点赞失败，ID：{}", popsciId, e);
            return Result.error("取消" + MessageConstant.LIKE + MessageConstant.FAILED);
        }
    }
    /**
     * 批量查询图片并按contentId分组
     */
    private Map<Long, List<Picture>> getContentPictureMap(List<Long> contentIdList) {
        if (CollectionUtils.isEmpty(contentIdList)) {
            return new HashMap<>();
        }
        List<Picture> pictureList = pictureMapper.selectList(
                new QueryWrapper<Picture>()
                        .in("content_id", contentIdList)
        );
        return pictureList.stream()
                .collect(Collectors.groupingBy(Picture::getContentId, Collectors.toList()));
    }

    private void setPopSciContentPictureField(PopSciContentVO vo, PopSciContent content, Map<Long, List<Picture>> contentPictureMap) {
        List<Picture> picList = contentPictureMap.getOrDefault(content.getId(), Collections.emptyList());
        vo.setPictureList(picList);
        if (!picList.isEmpty()) {
            String firstPicUrl = picList.get(0).getPicUrl();
            vo.setArticleCoverUrl(PopSciContentTypeEnum.ARTICLE.equals(content.getContentType()) ? firstPicUrl : vo.getArticleCoverUrl());
            vo.setVideoCoverUrl(PopSciContentTypeEnum.VIDEO.equals(content.getContentType()) ? firstPicUrl : vo.getVideoCoverUrl());
        }
    }

    /**
     * 批量设置图片字段
     */
    private void batchSetPopSciContentPictureField(List<PopSciContentVO> voList, List<PopSciContent> contentList, Map<Long, List<Picture>> contentPictureMap) {
        for (int i = 0; i < contentList.size(); i++) {
            PopSciContent content = contentList.get(i);
            PopSciContentVO vo = voList.get(i);
            setPopSciContentPictureField(vo, content, contentPictureMap);
        }
    }
    @Override
    public Result<PopSciContentVO> getPopSciContentById(Long contentId) {
        // 1. 校验ID合法性
        if (contentId == null || contentId <= 0) {
            log.error("查询科普内容详情失败，ID不合法：{}", contentId);
            return Result.error("科普内容ID不合法");
        }

        // 2. 根据ID查询实体
        PopSciContent popSciContent = popSciContentMapper.selectById(contentId);
        if (popSciContent == null) {
            log.error("查询科普内容详情失败，未找到该内容：{}", contentId);
            return Result.error("未查询到该科普内容");
        }

        // 3. 转换为完整VO（复用原有批量转换方法，封装为列表即可）
        List<PopSciContent> contentList = Collections.singletonList(popSciContent);
        List<PopSciContentVO> voList = convertToPopSciContentVO(contentList);

        // 4. 返回详情VO
        return Result.success(voList.get(0));
    }
    /**
     * 查询用户科普内容精简列表（交给前端区分笔记/收藏、图文/视频）
     * 适配前端：仅根据userId+mediaType+mushroomId查询，返回精简VO
     */
    public Result<PageResult<PopSciContentSummaryVO>> getUserContentList(UserContentQueryDTO queryDTO) {
        try {
            if (queryDTO.getUserId()==null) {
                return Result.error("当前用户ID不能为空");
            }
            // 计算分页offset
            int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

            // 1. 直接传DTO+offset到Mapper，查询笔记列表
            List<PopSciContentSummaryVO> summaryVOList = popSciContentMapper.selectUserContentSummary(queryDTO, offset);
            // 2. 查询总数
            Long total = popSciContentMapper.countUserContent(queryDTO);

            // 4. 封装结果
            PageResult<PopSciContentSummaryVO> pageResult = new PageResult<>(total, summaryVOList);
            return Result.success(pageResult);

        } catch (Exception e) {
            log.error("查询用户笔记列表失败：", e);
            return Result.error("查询用户内容失败");
        }
    }

    @Override
    public Result deletePicture(PictureDTO deletePictureDTO) {
        if (deletePictureDTO.getPicId() == null
                && deletePictureDTO.getContentId() == null
                && deletePictureDTO.getMushroomId() == null
                && deletePictureDTO.getDiseaseId() == null) {
            return Result.error("请至少提供一个删除条件");
        }

        try {
            // 2. 调用 Mapper 查询要删除的图片列表
            List<Picture> picList = pictureMapper.selectByDeleteDTO(deletePictureDTO);
            if (picList == null || picList.isEmpty()) {
                return Result.error("未找到要删除的图片");
            }

            // 3. 批量删除 MinIO 文件
            for (Picture pic : picList) {
                if (pic.getPicUrl() != null) {
                    minioService.deleteFile(pic.getPicUrl());
                }
            }

            // 4. 调用 Mapper 批量删除图片记录
            int deleteCount = pictureMapper.deleteByDeleteDTO(deletePictureDTO);

            return Result.success("成功删除 " + deleteCount + " 张图片");
        } catch (Exception e) {
            log.error("删除图片失败：", e);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }}

}