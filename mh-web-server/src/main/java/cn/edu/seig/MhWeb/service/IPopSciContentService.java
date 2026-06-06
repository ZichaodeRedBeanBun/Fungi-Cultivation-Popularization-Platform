package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.PictureDTO;
import cn.edu.seig.MhWeb.model.dto.PopSciContent.*;
import cn.edu.seig.MhWeb.model.dto.UserContentQuery.UserContentQueryDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomAddDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.mushroom.MushroomUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import cn.edu.seig.MhWeb.model.entity.PopSciContent;
import cn.edu.seig.MhWeb.model.vo.MushroomVO;
import cn.edu.seig.MhWeb.model.vo.PictureVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentSummaryVO;
import cn.edu.seig.MhWeb.model.vo.PopSciContentVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPopSciContentService extends IService<PopSciContent> {

    Result<PageResult<PopSciContentVO>> getAllPopSciContents(PopSciContentPageQueryDTO popSciContentPageQueryDTO);

    Result addPopSciContent(PopSciContentAddFileDTO addFileDTO,List<MultipartFile> articleDetailFiles,
                            MultipartFile videoCoverFile,
                            MultipartFile videoFile);

    Result updatePopSciContent(PopSciContentAddFileDTO addFileDTO,List<MultipartFile> articleDetailFiles,
                               MultipartFile videoCoverFile,
                               MultipartFile videoFile);

    Result auditPopSciContent(PopSciContentAuditDTO popSciContentAuditDTO);

    Result deletePopSciContent(Long id);

    Result deletePopSciContents(List<Long> ids);

    Result deleteVideoCover(Long picId);

    Result deleteArticleDetailPic(Long picId);

    Result<String> uploadVideoFile(MultipartFile file);

    Result<String> uploadVideoCover(MultipartFile file);

    Result<String> uploadArticleDetailPic(MultipartFile file);

    Result<List<PopSciContentSummaryVO>> getRecommendedArticleContents();

    Result<List<PopSciContentSummaryVO>> getRecommendedVideoContents();


    Result<Long> getAllPopSciContentsCount(Long mushroomId, String contentType);

    Result praisePopSci(Long popsciId);

    Result cancelPraisePopSci(Long popsciId);
    /**
     * 根据ID查询科普内容详情（返回完整VO）
     * @param contentId 科普内容ID
     * @return 完整科普内容VO
     */
    Result<PopSciContentVO> getPopSciContentById(Long contentId);
    /**
     * 查询用户科普内容精简列表（交给前端区分笔记/收藏、图文/视频）
     */
    Result<PageResult<PopSciContentSummaryVO>> getUserContentList(UserContentQueryDTO queryDTO);

    Result deletePicture(PictureDTO deletePictureDTO);
}
