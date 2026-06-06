package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataPageQueryDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantDataUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.PlantData;
import cn.edu.seig.MhWeb.model.vo.PlantDataVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 种植数据业务接口
 *
 * @author su
 */
public interface IPlantDataService extends IService<PlantData> {

    /**
     * 获取种植数据分页列表
     */
    Result<PageResult<PlantDataVO>> getPlantDataPage(PlantDataPageQueryDTO plantDataPageQueryDTO);

    /**
     * 新增种植数据
     */
    Result<Void> addPlantData(PlantDataAddDTO plantDataAddDTO);

    /**
     * 更新种植数据
     */
    Result<Void> updatePlantData(PlantDataUpdateDTO plantDataUpdateDTO);

    /**
     * 删除种植数据
     */
    Result<Void> deletePlantData(Long dataId);
    /**
     * 根据ID查询种植数据详情
     */
    Result<PlantDataVO> getPlantDataById(Long dataId);

    /**
     * 批量删除种植数据
     */
    Result<Void> deletePlantDatas(List<Long> dataIds);
}