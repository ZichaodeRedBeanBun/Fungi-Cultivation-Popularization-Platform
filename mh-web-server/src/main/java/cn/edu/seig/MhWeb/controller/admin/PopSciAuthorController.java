package cn.edu.seig.MhWeb.controller.admin;


import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopSciAuthorAddDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopSciAuthorDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopSciAuthorUpdateDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopsciAuditDTO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorDetailVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorNameVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciAuthorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 科普号控制器（完全对齐歌手Controller结构）
 * @author su
 */
@RestController
@RequestMapping("/admin/popSciAuthor")
public class PopSciAuthorController {

    @Autowired
    private IPopSciAuthorService popSciAuthorService;

    /**
     * 获取所有科普号列表（分页+条件查询）
     * 对应 getAllArtists
     */
    @PostMapping("/getAllPopSciAuthors")
    public Result<PageResult<PopSciAuthorVO>> getAllPopSciAuthors(@RequestBody @Valid PopSciAuthorDTO popSciAuthorDTO) {
        return popSciAuthorService.getAllPopSciAuthors(popSciAuthorDTO);
    }
    /**
     * 获取所有科普号ID和名称
     * 对应 getAllArtistNames
     */
    @GetMapping("/getAllPopSciAuthorNames")
    public Result<List<PopSciAuthorNameVO>> getAllPopSciAuthorNames() {
        return popSciAuthorService.getAllPopSciAuthorNames();
    }

    /**
     * TODO 可能删掉
     * 获取随机科普号（数量10）
     * 对应 getRandomArtists
     */
    @GetMapping("/getRandomPopSciAuthors")
    public Result<List<PopSciAuthorVO>> getRandomPopSciAuthors() {
        return popSciAuthorService.getRandomPopSciAuthors();
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
     * 添加科普号
     * 对应 addArtist
     */

    @PostMapping("/addPopSciAuthor")
    public Result addPopSciAuthor(@RequestBody @Valid PopSciAuthorAddDTO popSciAuthorAddDTO) {
        return popSciAuthorService.addPopSciAuthor(popSciAuthorAddDTO);
    }

    /**
     * 更新科普号信息
     * 对应 updateArtist
     */
    @PostMapping("/updatePopSciAuthor")
    public Result updatePopSciAuthor(@RequestBody @Valid PopSciAuthorUpdateDTO popSciAuthorUpdateDTO) {
        return popSciAuthorService.updatePopSciAuthor(popSciAuthorUpdateDTO);
    }

    /**
     * 更新科普号头像
     * 对应 updateArtistAvatar
     */

    @PatchMapping("/updatePopSciAuthorAvatar")
    public Result updatePopSciAuthorAvatar(@RequestParam("userId") Long userId, @RequestParam("avatar") MultipartFile avatar ){// 参数名对齐为avatar
        return popSciAuthorService.updatePopSciAuthorAvatar(userId, avatar);
    }
    /**
     * 删除单个科普号
     * 对应 deleteArtist
     */
    @PostMapping("/deletePopSciAuthor")
    public Result deletePopSciAuthor(@RequestParam Long userId) {
        return popSciAuthorService.deletePopSciAuthor(userId);
    }

    /**
     * 批量删除科普号
     * 对应 deleteArtists
     */
    @PostMapping("/deletePopSciAuthors")
    public Result deletePopSciAuthors(@RequestBody List<Long> userIds) {
        return popSciAuthorService.deletePopSciAuthors(userIds);
    }
    /**
     * 管理员审核科普号认证申请
     */
    @PostMapping("/audit")
    public Result auditPopSciAuthor(@RequestBody @Valid PopsciAuditDTO auditDTO) {
        return popSciAuthorService.auditPopSciAuthor(auditDTO);
    }
}