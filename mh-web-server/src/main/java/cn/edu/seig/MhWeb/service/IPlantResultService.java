package cn.edu.seig.MhWeb.service;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultAddDTO;
import cn.edu.seig.MhWeb.model.dto.plantPlan.PlantResultUpdateDTO;
import cn.edu.seig.MhWeb.model.entity.PlantResult;
import cn.edu.seig.MhWeb.model.vo.PlantResultVO;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 种植成果业务接口
 *
 * @author su
 */
public interface IPlantResultService extends IService<PlantResult> {

    /**
     * 根据计划ID查询成果（一对一）
     */
    PlantResultVO getResultByPlanId(Long planId);

    /**
     * 新增种植成果
     */
    Result<Void> addPlantResult(PlantResultAddDTO plantResultAddDTO);

    /**
     * 更新种植成果
     */
    Result<Void> updatePlantResult(PlantResultUpdateDTO plantResultUpdateDTO);

    /**
     * 删除单个种植成果，清空
     */
    Result<Void> deletePlantResult(Long resultId);


}