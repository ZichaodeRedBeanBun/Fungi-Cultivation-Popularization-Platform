package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.mapper.MultipleCategoryMapper; // 请替换为你实际的 Mapper
import cn.edu.seig.MhWeb.mapper.ProductionNationalMapper;
import cn.edu.seig.MhWeb.mapper.ProductionProvinceMapper;
import cn.edu.seig.MhWeb.model.entity.MultipleCategory; // 请替换为你实际的类名
import cn.edu.seig.MhWeb.model.entity.ProductionNational;
import cn.edu.seig.MhWeb.model.entity.ProductionProvince;
import cn.edu.seig.MhWeb.model.vo.CategoryItemVO;
import cn.edu.seig.MhWeb.model.vo.NationalTrendVO;
import cn.edu.seig.MhWeb.model.vo.ProvinceCompareVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IMushroomDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MushroomDataServiceImpl extends ServiceImpl<MultipleCategoryMapper, MultipleCategory> implements IMushroomDataService { // 修正泛型

    @Autowired
    private MultipleCategoryMapper multipleCategoryMapper; // 修正 Mapper 名

    @Autowired
    private ProductionNationalMapper productionNationalMapper; // 修正 Mapper 名

    @Autowired
    private ProductionProvinceMapper productionProvinceMapper; // 修正 Mapper 名

    @Override
    public Result<List<CategoryItemVO>> getAllCategories() {
        List<MultipleCategory> categories = multipleCategoryMapper.selectList( // 修正类名
                new LambdaQueryWrapper<MultipleCategory>()
                        .orderByAsc(MultipleCategory::getId)
        );

        List<CategoryItemVO> voList = categories.stream().map(category -> {
            CategoryItemVO vo = new CategoryItemVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<NationalTrendVO> getNationalTrend(Long categoryId) {
        MultipleCategory category = multipleCategoryMapper.selectById(categoryId); // 修正类名
        if (category == null) {
            return Result.error(MessageConstant.NATIONAL_DATA+MessageConstant.NOT_EXIST);
        }

        List<ProductionNational> nationalList = productionNationalMapper.selectList( // 修正类名
                new LambdaQueryWrapper<ProductionNational>()
                        .eq(ProductionNational::getCategoryId, categoryId)
                        .orderByAsc(ProductionNational::getYear)
        );

        NationalTrendVO vo = new NationalTrendVO();
        // 注意：这里需要确保 MultipleCategory 实体中有 categoryName 和 unit 字段
        // 如果字段名不同，请使用 getter 手动设置
        BeanUtils.copyProperties(category, vo); 

        if (!CollectionUtils.isEmpty(nationalList)) {
            vo.setYears(nationalList.stream().map(ProductionNational::getYear).collect(Collectors.toList()));
            vo.setValues(nationalList.stream().map(ProductionNational::getValue).collect(Collectors.toList()));
        }

        return Result.success(vo);
    }

    @Override
    public Result<ProvinceCompareVO> getProvinceCompare(Long categoryId, Integer year) {
        MultipleCategory category = multipleCategoryMapper.selectById(categoryId); // 修正类名
        if (category == null) {
            return Result.error(MessageConstant.PROVINCE_DATA+MessageConstant.NOT_EXIST);
        }

        List<ProductionProvince> provinceList = productionProvinceMapper.selectList( // 修正类名
                new LambdaQueryWrapper<ProductionProvince>()
                        .eq(ProductionProvince::getCategoryId, categoryId)
                        .eq(ProductionProvince::getYear, year)
                        .orderByDesc(ProductionProvince::getValue)
        );

        ProvinceCompareVO vo = new ProvinceCompareVO();
        // 同上，注意字段映射
        BeanUtils.copyProperties(category, vo);
        vo.setYear(year);

        if (!CollectionUtils.isEmpty(provinceList)) {
            vo.setProvinces(provinceList.stream().map(ProductionProvince::getProvince).collect(Collectors.toList()));
            vo.setValues(provinceList.stream().map(ProductionProvince::getValue).collect(Collectors.toList()));
        }

        return Result.success(vo);
    }
}