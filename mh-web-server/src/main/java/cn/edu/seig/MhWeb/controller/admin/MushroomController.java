package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.service.IMushroomService;
import cn.edu.seig.MhWeb.model.dto.mushroom.*;
import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IMushroomService;
import cn.edu.seig.MhWeb.service.IPictureService;
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
@RequestMapping("/admin/mushroom")
public class MushroomController {

    @Autowired
    private IMushroomService mushroomService;
    @Autowired
    private IPictureService pictureService;

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
     * 新增菌种
     *
     * @param mushroomAddDTO 新增菌种参数
     * @return 操作结果
     */
    @PostMapping("/addMushroom")
    public Result addMushroom(@RequestBody @Valid MushroomAddDTO mushroomAddDTO) {
        return mushroomService.addMushroom(mushroomAddDTO);
    }

    /**
     * 编辑菌种
     *
     * @param mushroomUpdateDTO 编辑菌种参数
     * @return 操作结果
     */
    @PutMapping("/updateMushroom")
    public Result updateMushroom(@RequestBody @Valid MushroomUpdateDTO mushroomUpdateDTO) {
        return mushroomService.updateMushroom(mushroomUpdateDTO);
    }

    /**
     * 更新菌种封面
     *
     * @param id   菌种ID
     * @param file 封面文件
     * @return 操作结果
     */
    @PatchMapping("/updateMushroomCover/{id}")
    public Result updateMushroomCover(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        return mushroomService.updateMushroomCover(id, file);
    }

    /**
     * 删除单个菌种
     *
     * @param id 菌种ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteMushroom/{id}")
    public Result deleteMushroom(@PathVariable("id") Long id) {
        return mushroomService.deleteMushroom(id);
    }

    /**
     * 批量删除菌种
     *
     * @param ids 菌种ID列表
     * @return 操作结果
     */
    @DeleteMapping("/deleteMushrooms")
    public Result deleteMushrooms(@RequestBody List<Long> ids) {
        return mushroomService.deleteMushrooms(ids);
    }
    /**
     * 上传菌种详情图（多张）
     */
    @PostMapping("/uploadMushroomDetailPics/{mushroomId}")
    public Result uploadMushroomDetailPics(
            @PathVariable("mushroomId") Long mushroomId,
            @RequestParam("files") List<MultipartFile> files) {
        return mushroomService.uploadMushroomDetailPics(mushroomId, files);
    }

    /**
     * 删除单张详情图
     */
    @DeleteMapping("/deleteMushroomDetailPic/{picId}")
    public Result deleteMushroomDetailPic(@PathVariable("picId") Long picId) {
        return mushroomService.deleteMushroomDetailPic(picId);
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

}