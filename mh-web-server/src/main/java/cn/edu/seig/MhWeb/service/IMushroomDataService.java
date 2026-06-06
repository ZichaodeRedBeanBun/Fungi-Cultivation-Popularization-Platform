package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.entity.MultipleCategory; // 请替换为你实际的类名
import cn.edu.seig.MhWeb.model.vo.CategoryItemVO;
import cn.edu.seig.MhWeb.model.vo.NationalTrendVO;
import cn.edu.seig.MhWeb.model.vo.ProvinceCompareVO;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IMushroomDataService extends IService<MultipleCategory> { // 继承分类实体的 IService

    Result<List<CategoryItemVO>> getAllCategories();

    Result<NationalTrendVO> getNationalTrend(Long categoryId);

    Result<ProvinceCompareVO> getProvinceCompare(Long categoryId, Integer year);
}