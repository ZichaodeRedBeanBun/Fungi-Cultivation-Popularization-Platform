package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomAddDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import cn.edu.seig.MhWeb.model.vo.MushroomSummaryVO;
import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMushroomService extends IService<MushroomInfo> {

    Result<PageResult<MushroomVO>> getAllMushrooms(MushroomPageQueryDTO mushroomPageQueryDTO);

    Result addMushroom(MushroomAddDTO mushroomAddDTO);

    Result updateMushroom(MushroomUpdateDTO mushroomUpdateDTO);

    Result updateMushroomCover(Long id, MultipartFile file);

    Result deleteMushroom(Long id);

    Result deleteMushrooms(List<Long> ids);


    Result uploadMushroomDetailPics(Long mushroomId, List<MultipartFile> files);


    Result deleteMushroomDetailPic(Long picId);

    Result<Long> getAllMushroomsCount();

    Result<MushroomVO> getMushroomById(Long mushroomId);

    Result<PageResult<MushroomSummaryVO>> getMushroomSummaryPage(MushroomPageQueryDTO mushroomPageQueryDTO);
}
