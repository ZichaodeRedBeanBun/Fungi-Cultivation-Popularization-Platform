package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.PopSciUser.*;
import cn.edu.seig.MhWeb.model.entity.DiseasePest;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorDetailVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorNameVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPopSciAuthorService extends IService<User> {
    Result<List<PopSciAuthorVO>> getRandomPopSciAuthors();

    Result<PageResult<PopSciAuthorVO>> getAllPopSciAuthors(PopSciAuthorDTO popSciAuthorDTO);

    Result<PopSciAuthorDetailVO> getPopSciAuthorDetail(Long userId);

    Result addPopSciAuthor(PopSciAuthorAddDTO popSciAuthorAddDTO);

    Result updatePopSciAuthor(PopSciAuthorUpdateDTO popSciAuthorUpdateDTO);

    Result updatePopSciAuthorAvatar(Long userId, MultipartFile avatar);

    Result deletePopSciAuthor(Long userId);

    Result deletePopSciAuthors(List<Long> userIds);

    Result<List<PopSciAuthorNameVO>> getAllPopSciAuthorNames();


    Result auditPopSciAuthor(PopsciAuditDTO auditDTO);

    Result<PageResult<PopSciAuthorNameVO>> searchPopSciAuthors(PopSciAuthorSearchDTO searchDTO);

    Result applyPopSciAuthor(PopsciApplyDTO popsciApplyDTO);
}
