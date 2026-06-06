package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.PopSciBannerDTO;
import cn.edu.seig.MhWeb.model.dto.admin.AdminDTO;
import cn.edu.seig.MhWeb.model.entity.Admin;
import cn.edu.seig.MhWeb.model.entity.PopSciBanner;
import cn.edu.seig.MhWeb.model.entity.PopSciContent;
import cn.edu.seig.MhWeb.model.vo.PopSciBannerVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 服务类
 *
 * @author su
 */
public interface PopSciBannerService extends IService<PopSciBanner> {

    Result<PageResult<PopSciBannerVO>> getAllBanners(PopSciBannerDTO bannerDTO);

    Result addBanner(Long contentId);

    Result updateBanner(Long id, Integer sort, Integer status);

    Result deleteBanner(Long id);

    Result deleteBanners(List<Long> ids);

    Result<List<PopSciBannerVO>> getBannerList();
}
