package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.model.dto.PopSciContent.*;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentAddDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentAuditDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.PopSciContentPageQueryDTO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科普内容管理前端控制器
 *
 * @author su
 */
@RestController("adminPopSciContentController")
@RequestMapping("/admin/popScicont")
public class PopSciContentController {

    @Autowired
    private IPopSciContentService popSciContentService;

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
     * 审核科普内容
     *
     * @param popSciContentAuditDTO 审核参数
     * @return 操作结果
     */
    @PutMapping("/auditPopSciContent")
    public Result auditPopSciContent(@RequestBody @Valid PopSciContentAuditDTO popSciContentAuditDTO) {
        return popSciContentService.auditPopSciContent(popSciContentAuditDTO);
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

}