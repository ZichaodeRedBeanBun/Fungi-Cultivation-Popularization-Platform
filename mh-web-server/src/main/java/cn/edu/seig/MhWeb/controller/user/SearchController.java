package cn.edu.seig.MhWeb.controller.user;

import cn.edu.seig.MhWeb.model.dto.DiseasePest.DiseasePestPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopSciAuthorSearchDTO;
import cn.edu.seig.MhWeb.model.vo.*;
import cn.edu.seig.MhWeb.service.*;
import cn.edu.seig.MhWeb.model.dto.mushroom.*;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 菌种管理前端控制器
 *
 * @author su
 */
@RestController
@Slf4j
@RequestMapping("/user/search")
public class SearchController {

    @Autowired
    private IMushroomService mushroomService;
    @Autowired
    private IPictureService pictureService;
    @Autowired
    private IDiseasePestService diseasePestService;
    @Autowired
    private IPopSciContentService popSciContentService;
    @Autowired
    private IPopSciAuthorService popSciAuthorService;
    @Autowired
    private PopSciBannerService popSciBannerService;
    /**
     * 获取菌种分页列表
     *
     * @param mushroomPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @PostMapping("/getAllMushrooms")
    public Result<PageResult<MushroomVO>> getAllMushrooms(@RequestBody MushroomPageQueryDTO mushroomPageQueryDTO) {
        log.info("进入 getAllMushrooms 方法，参数：{}",mushroomPageQueryDTO);
        return mushroomService.getAllMushrooms(mushroomPageQueryDTO);
    }

    /**
     *获取科普内容列表
     * @param popSciContentPageQueryDTO
     * @return
     */
    @PostMapping("/getAllPopSciContents")
    public Result<PageResult<PopSciContentVO>> getAllPopSciContents(@RequestBody @Valid PopSciContentPageQueryDTO popSciContentPageQueryDTO) {
        return popSciContentService.getAllPopSciContents(popSciContentPageQueryDTO);
    }

    /**
     * 获取菌种概要VO分页列表（列表展示专用，少字段、高性能）
     *
     * @param mushroomPageQueryDTO 分页查询条件（与原有一致，无需新DTO）
     * @return 概要VO分页结果
     */
    @PostMapping("/getMushroomSummaryPage")
    public Result<PageResult<MushroomSummaryVO>> getMushroomSummaryPage(@RequestBody MushroomPageQueryDTO mushroomPageQueryDTO) {
        log.info("进入 getMushroomSummaryPage 方法，参数：{}", mushroomPageQueryDTO);
        return mushroomService.getMushroomSummaryPage(mushroomPageQueryDTO);
    }

    /**
     * 根据菌种ID和图片URL查询图片ID
     * @param mushroomId 菌种ID
     * @param picUrl 图片URL
     * @return 图片ID
     */
    @GetMapping("/getPicIdByUrl")
    public Result<Long> getPicIdByUrl(
            @RequestParam("mushroomId") Long mushroomId,
            @RequestParam("picUrl") String picUrl) {
        return pictureService.getPicIdByUrl(mushroomId, picUrl);
    }
    /**
     * 获取病虫害概要VO分页列表（列表展示专用，带封面图、少字段）
     *
     * @param diseasePestPageQueryDTO 分页查询条件（与原有一致，无需新DTO）
     * @return 概要VO分页结果（包含枚举类型编码/名称）
     */
    @PostMapping("/getDiseasePestSummaryPage")
    public Result<PageResult<DiseasePestSummaryVO>> getDiseasePestSummaryPage(@RequestBody DiseasePestPageQueryDTO diseasePestPageQueryDTO) {
        return diseasePestService.getDiseasePestSummaryPage(diseasePestPageQueryDTO);
    }
    /**
     * 获取病虫害分页列表
     *
     * @param diseasePestPageQueryDTO 分页查询条件
     * @return 分页结果（包含枚举类型编码/名称）
     */
    @PostMapping("/getAllDiseasePests")
    public Result<PageResult<DiseasePestVO>> getAllDiseasePests(@RequestBody DiseasePestPageQueryDTO diseasePestPageQueryDTO) {
        return diseasePestService.getAllDiseasePests(diseasePestPageQueryDTO);
    }
    /**
     * 根据病虫害ID查询单条病虫害详细信息
     *
     * @param diseasePestId 病虫害ID
     * @return 病虫害详细信息VO
     */
    @GetMapping("/getDiseasePestById")
    public Result<DiseasePestVO> getDiseasePestById(@RequestParam("diseasePestId") Long diseasePestId) {
        log.info("进入 getDiseasePestById 方法，病虫害ID：{}", diseasePestId);
        return diseasePestService.getDiseasePestById(diseasePestId);
    }
    /**
     * 根据菌种ID查询单条菌种详细信息
     *
     * @param mushroomId 菌种ID
     * @return 菌种详细信息VO
     */
    @GetMapping("/getMushroomById")
    public Result<MushroomVO> getMushroomById(@RequestParam("mushroomId") Long mushroomId) {
        log.info("进入 getMushroomById 方法，菌种ID：{}", mushroomId);
        return mushroomService.getMushroomById(mushroomId);
    }
    /**
     * 根据ID查询科普内容详情（返回完整VO）
     * @param contentId 科普内容主键ID
     * @return 完整科普内容详情
     */
    @GetMapping("/getByContentId")
    public Result<PopSciContentVO> getPopSciContentById(@RequestParam("contentId") Long contentId) {
        log.info("进入查询科普内容详情方法，内容ID：{}", contentId);
        return popSciContentService.getPopSciContentById(contentId);
    }
    /**
     * 获取科普号详情
     * 对应 getArtistDetail
     */
    @GetMapping("/getPopSciAuthorDetail/{id}")
    public Result<PopSciAuthorDetailVO> getPopSciAuthorDetail(@PathVariable("id") Long userId) {
        return popSciAuthorService.getPopSciAuthorDetail(userId);
    }
    /**
     * 获取随机科普号（数量10）
     * 对应 getRandomArtists
     */
    @GetMapping("/getRandomPopSciAuthors")
    public Result<List<PopSciAuthorVO>> getRandomPopSciAuthors() {
        return popSciAuthorService.getRandomPopSciAuthors();
    }
    /**
     * 分页搜索科普号列表
     * @param searchDTO 查询条件（pageNum/pageSize/username/userId）
     * @return 科普号分页列表
     */
    @PostMapping("/getPopSciAuthors")
    public Result<PageResult<PopSciAuthorNameVO>> searchPopSciAuthors(@RequestBody PopSciAuthorSearchDTO searchDTO) {
        return popSciAuthorService.searchPopSciAuthors(searchDTO);
    }
    /**
     * 用户端：获取展示的轮播图列表（复用科普内容的图片）
     */
    @GetMapping("/getBannerList")
    public Result<List<PopSciBannerVO>> getBannerList() {
        return popSciBannerService.getBannerList();
    }
}