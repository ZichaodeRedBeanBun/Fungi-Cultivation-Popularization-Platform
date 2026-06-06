package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IDiseasePestService;
import cn.edu.seig.MhWeb.service.IMushroomService;
import cn.edu.seig.MhWeb.service.IPopSciContentService;
import cn.edu.seig.MhWeb.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台统计接口控制器
 */
@RestController
@RequestMapping("/admin/count")
public class dataController {

    @Autowired
    private IMushroomService mushroomService;
    @Autowired
    private IDiseasePestService diseasePestService;
    @Autowired
    private IPopSciContentService popSciContentService;
    @Autowired
    private IUserService userService;


    /**
     * 获取用户总数（支持按状态筛选）
     */
    @GetMapping("/getAllUsersCount")
    public Result<Long> getAllUsersCount(@RequestParam(required = false) Integer user_type) {
        return userService.getAllUsersCount(user_type);
    }

    /**
     * 获取菌种卡片总数（支持按菌种ID/状态筛选）
     */
    @GetMapping("/getAllMushroomsCount")
    public Result<Long> getAllMushroomsCount() {
        return mushroomService.getAllMushroomsCount();
    }

    /**
     * 获取科普内容总数（支持按菌种ID/内容类型筛选）
     */
    @GetMapping("/getAllPopSciContentsCount")
    public Result<Long> getAllPopSciContentsCount(
            @RequestParam(required = false) Long mushroomId,
            @RequestParam(required = false) String contentType) {
        return popSciContentService.getAllPopSciContentsCount(mushroomId, contentType);
    }

    /**
     * 获取病虫害总数（支持按菌种ID/状态筛选）
     */
    @GetMapping("/getAllDiseasePestsCount")
    public Result<Long> getAllDiseasePestsCount() {
        return diseasePestService.getAllDiseasePestsCount();
    }
}