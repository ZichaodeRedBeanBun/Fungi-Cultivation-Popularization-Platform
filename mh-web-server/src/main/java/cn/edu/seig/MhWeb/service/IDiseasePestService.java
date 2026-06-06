package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.DiseasePest.*;
import cn.edu.seig.MhWeb.model.entity.DiseasePest;
import cn.edu.seig.MhWeb.model.vo.DiseasePestSummaryVO;
import cn.edu.seig.MhWeb.model.vo.DiseasePestVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDiseasePestService extends IService<DiseasePest> {


    Result<PageResult<DiseasePestVO>> getAllDiseasePests(DiseasePestPageQueryDTO diseasePestPageQueryDTO);

    Result addDiseasePest(DiseasePestAddDTO diseasePestAddDTO);

    Result updateDiseasePest(DiseasePestUpdateDTO diseasePestUpdateDTO);

    Result deleteDiseasePest(Long id);

    Result deleteDiseasePests(List<Long> ids);

    Result<Long> getAllDiseasePestsCount();

    Result<DiseasePestVO> getDiseasePestById(Long diseasePestId);

    Result<PageResult<DiseasePestSummaryVO>> getDiseasePestSummaryPage(DiseasePestPageQueryDTO diseasePestPageQueryDTO);

    Result updateDiseasePestCover(Long id, MultipartFile file);
}
