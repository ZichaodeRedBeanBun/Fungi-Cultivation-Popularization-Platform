package cn.edu.seig.MhWeb.controller.user;

import cn.edu.seig.MhWeb.model.dto.PictureDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.*;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentAddDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentAuditDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.UserContentQuery.UserContentQueryDTO;
import cn.edu.seig.MhWeb.model.vo.PictureVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciContentService;
import cn.edu.seig.MhWeb.service.MinioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 科普内容管理前端控制器
 *
 * @author su
 */
@RestController("userPopSciContentController")
@RequestMapping("/user/popsci")
@Slf4j
public class PopSciContentController {

    @Autowired
    private IPopSciContentService popSciContentService;
    @Autowired
    private MinioService minioService;
    /**
     * 获取推荐图文列表（返回12条）
     * 接口路径：GET /api/pop-sci/recommended/articles
     * 功能说明：
     *  - 未登录用户：返回图文类型的随机12条内容
     *  - 已登录用户：根据用户收藏的菌种偏好推荐，不足则用随机内容填充
     * @return Result<List<PopSciContentVO>> 推荐图文列表（12条）
     */
    @GetMapping("/recommended/articles")
    public Result<List<PopSciContentSummaryVO>> getRecommendedArticles() {
        // 调用Service层的推荐图文方法
        return popSciContentService.getRecommendedArticleContents();
    }

    /**
     * 获取推荐视频列表（返回6条）
     * 接口路径：GET /api/pop-sci/recommended/videos
     * 功能说明：
     *  - 未登录用户：返回视频类型的随机6条内容
     *  - 已登录用户：根据用户收藏的菌种偏好推荐，不足则用随机内容填充
     * @return Result<List<PopSciContentSummaryVO>> 推荐视频列表（6条）
     */
    @GetMapping("/recommended/videos")
    public Result<List<PopSciContentSummaryVO>> getRecommendedVideos() {
        // 调用Service层的推荐视频方法
        return popSciContentService.getRecommendedVideoContents();
    }

    /**
     * 新增科普内容
     *
     * @param addFileDTO 新增参数
     * @return 操作结果
     */
    @PostMapping("/addPopSciContent")
    public Result addPopSciContent(@RequestPart("addFileDTO") PopSciContentAddFileDTO addFileDTO,
                                   @RequestPart(value = "articleDetailFiles", required = false) List<MultipartFile> articleDetailFiles,
                                   @RequestPart(value = "videoCoverFile", required = false) MultipartFile videoCoverFile,
                                   @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        // 2. 直接调用业务层，所有文件上传+入库逻辑由业务层处理
        return popSciContentService.addPopSciContent(addFileDTO,articleDetailFiles,videoCoverFile,videoFile);
    }

    /**
     * 编辑科普内容
     *
     * @param addFileDTO 更新参数
     * @return 操作结果
     */
    @PostMapping("/updatePopSciContent")
    public Result updatePopSciContent(@RequestPart("addFileDTO") PopSciContentAddFileDTO addFileDTO,
                                      @RequestPart(value = "articleDetailFiles", required = false) List<MultipartFile> articleDetailFiles,
                                      @RequestPart(value = "videoCoverFile", required = false) MultipartFile videoCoverFile,
                                      @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        return popSciContentService.updatePopSciContent(addFileDTO,articleDetailFiles,videoCoverFile,videoFile);
    }
    /**
     * 上传图文详情图（纯工具接口，仅返回URL）
     */
    @PostMapping("/uploadArticleDetailPic")
    public Result<String> uploadArticleDetailPic(@RequestParam("file") MultipartFile file) {
        return popSciContentService.uploadArticleDetailPic(file);
    }

    /**
     * 上传视频封面（纯工具接口，仅返回URL）
     */
    @PostMapping("/uploadVideoCover")
    public Result<String> uploadVideoCover(@RequestParam("file") MultipartFile file) {
        return popSciContentService.uploadVideoCover(file);
    }

    /**
     * 上传视频文件（纯工具接口，仅返回URL）
     */
    @PostMapping("/uploadVideoFile")
    public Result<String> uploadVideoFile(@RequestParam("file") MultipartFile file) {
        return popSciContentService.uploadVideoFile(file);
    }
    /**
     * 删除单张图文详情图
     * @param picId 图片ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteArticleDetailPic/{picId}")
    public Result deleteArticleDetailPic(@PathVariable("picId") Long picId) {
        return popSciContentService.deleteArticleDetailPic(picId);
    }

    /**
     * 删除视频封面
     * @param picId 封面图片ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteVideoCover/{picId}")
    public Result deleteVideoCover(@PathVariable("picId") Long picId) {
        return popSciContentService.deleteVideoCover(picId);
    }
    /**
     * 通用删除图片（支持多种删除条件）
     * @param deletePictureDTO 删除图片DTO
     * @return 操作结果
     */
    @PostMapping("/deletePicture")
    public Result deletePicture(@RequestBody PictureDTO deletePictureDTO) {
        return popSciContentService.deletePicture(deletePictureDTO);
    }
    /**
     * 删除单个科普内容
     *
     * @param id 科普内容ID
     * @return 操作结果
     */
    @DeleteMapping("/deletePopSciContent/{id}")
    public Result deletePopSciContent(@PathVariable("id") Long id) {
        return popSciContentService.deletePopSciContent(id);
    }

    /**
     * 批量删除科普内容
     *
     * @param ids 科普内容ID列表
     * @return 操作结果
     */
    @DeleteMapping("/deletePopSciContents")
    public Result deletePopSciContents(@RequestBody List<Long> ids) {
        return popSciContentService.deletePopSciContents(ids);
    }
    /**
     * 科普内容点赞
     * 对齐收藏接口：Post请求 + RequestParam参数 + Valid校验
     */
    @PostMapping("/praise")
    public Result praisePopSci(
            @RequestParam @Valid Long popsciId) {
        log.info("接收科普内容点赞请求，内容ID：{}", popsciId);
        return popSciContentService.praisePopSci(popsciId);
    }

    /**
     * 科普内容取消点赞
     * 对齐取消收藏接口：Delete请求 + RequestParam参数 + Valid校验
     */
    @DeleteMapping("/cancelPraise")
    public Result cancelPraisePopSci(
            @RequestParam @Valid Long popsciId) {
        log.info("接收科普内容取消点赞请求，内容ID：{}", popsciId);
        return popSciContentService.cancelPraisePopSci(popsciId);
    }

    /**
     * 查询用户科普内容精简列表
     * @param queryDTO
     * @return
     */
    @PostMapping("/userContentList")
    public Result<PageResult<PopSciContentSummaryVO>> getUserContentList(@RequestBody UserContentQueryDTO queryDTO) {
        log.info("查询用户科普内容精简列表，参数：{}", queryDTO);
        return popSciContentService.getUserContentList(queryDTO);
    }
}