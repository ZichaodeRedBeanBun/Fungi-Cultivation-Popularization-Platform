package cn.edu.seig.MhWeb.controller.user;

import cn.edu.seig.MhWeb.model.dto.DiseasePest.DiseasePestPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomPageQueryDTO;
import cn.edu.seig.MhWeb.model.vo.DiseasePestVO;
import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IDiseasePestService;
import cn.edu.seig.MhWeb.service.IMushroomService;
import cn.edu.seig.MhWeb.service.IPictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user/picture")
public class pictureController {

    @Autowired
    private IPictureService pictureService;


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
