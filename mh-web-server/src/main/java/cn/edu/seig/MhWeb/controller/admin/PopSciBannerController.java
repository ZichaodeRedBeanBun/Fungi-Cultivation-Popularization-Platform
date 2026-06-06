package cn.edu.seig.MhWeb.controller.admin;


import cn.edu.seig.MhWeb.model.dto.PopSciBannerDTO;
import cn.edu.seig.MhWeb.model.vo.PopSciBannerVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.PopSciBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 科普内容轮播图控制器（独立管理轮播图列表）
 */
@RestController
@RequestMapping("/admin/popSciBanner")
public class PopSciBannerController {

    @Autowired
    private PopSciBannerService popSciBannerService;

    /**
     * 管理员端：分页查询轮播图列表（支持筛选状态）
     */
    @PostMapping("/getAllBanners")
    public Result<PageResult<PopSciBannerVO>> getAllBanners(@RequestBody PopSciBannerDTO bannerDTO) {
        return popSciBannerService.getAllBanners(bannerDTO);
    }

    /**
     * 管理员端：添加轮播图（关联科普内容）
     */
    @PostMapping("/addBanner")
    public Result addBanner(@RequestParam("contentId") Long contentId) {
        return popSciBannerService.addBanner(contentId);
    }

    /**
     * 管理员端：更新轮播图排序/状态
     */
    @PatchMapping("/updateBanner/{id}")
    public Result updateBanner(@PathVariable("id") Long id,
                               @RequestParam(value = "sort", required = false) Integer sort,
                               @RequestParam(value = "status", required = false) Integer status) {
        return popSciBannerService.updateBanner(id, sort, status);
    }

    /**
     * 管理员端：删除单张轮播图（仅删轮播关系，不删科普内容/图片）
     */
    @DeleteMapping("/deleteBanner/{id}")
    public Result deleteBanner(@PathVariable("id") Long id) {
        return popSciBannerService.deleteBanner(id);
    }

    /**
     * 管理员端：批量删除轮播图
     */
    @DeleteMapping("/deleteBanners")
    public Result deleteBanners(@RequestBody List<Long> ids) {
        return popSciBannerService.deleteBanners(ids);
    }


}