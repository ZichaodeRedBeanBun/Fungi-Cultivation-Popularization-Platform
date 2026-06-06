package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.model.dto.DiseasePest.*;
import cn.edu.seig.MhWeb.model.vo.DiseasePestVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IDiseasePestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 病虫害管理前端控制器
 *
 * @author su
 */
@RestController
@RequestMapping("/admin/diseasePest")
public class DiseasePestController {

    @Autowired
    private IDiseasePestService diseasePestService;
    /**
     * 更新病虫害封面
     *
     * @param id   病虫害ID
     * @param file 封面文件
     * @return 操作结果
     */
    @PatchMapping("/updateDiseasePestCover/{id}")
    public Result updateDiseasePestCover(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        return diseasePestService.updateDiseasePestCover(id, file);
    }

    /**
     * 获取病虫害分页列表
     *
     * @param diseasePestPageQueryDTO 分页查询条件
     * @return 分页结果（包含枚举类型编码/名称）
     */
    @PostMapping("/getAllDiseasePests")
    public Result<PageResult<DiseasePestVO>> getAllDiseasePests(@RequestBody @Valid DiseasePestPageQueryDTO diseasePestPageQueryDTO) {
        return diseasePestService.getAllDiseasePests(diseasePestPageQueryDTO);
    }

    /**
     * 新增病虫害
     *
     * @param diseasePestAddDTO 新增参数（itemType为枚举ID）
     * @return 操作结果
     */
    @PostMapping("/addDiseasePest")
    public Result addDiseasePest(@RequestBody @Valid DiseasePestAddDTO diseasePestAddDTO) {
        return diseasePestService.addDiseasePest(diseasePestAddDTO);
    }

    /**
     * 编辑病虫害
     *
     * @param diseasePestUpdateDTO 编辑参数（itemType为枚举ID）
     * @return 操作结果
     */
    @PutMapping("/updateDiseasePest")
    public Result updateDiseasePest(@RequestBody @Valid DiseasePestUpdateDTO diseasePestUpdateDTO) {
        return diseasePestService.updateDiseasePest(diseasePestUpdateDTO);
    }

    /**
     * 删除单个病虫害
     *
     * @param id 病虫害ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteDiseasePest/{id}")
    public Result deleteDiseasePest(@PathVariable("id") Long id) {
        return diseasePestService.deleteDiseasePest(id);
    }

    /**
     * 批量删除病虫害
     *
     * @param ids 病虫害ID列表
     * @return 操作结果
     */
    @DeleteMapping("/deleteDiseasePests")
    public Result deleteDiseasePests(@RequestBody List<Long> ids) {
        return diseasePestService.deleteDiseasePests(ids);
    }
}