package cn.edu.seig.MhWeb.model.dto.DiseasePest;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 病虫害新增DTO
 *
 * @author su
 */
@Data
public class DiseasePestAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 病虫害名称（如：蘑菇褐斑病）
     */
    @NotBlank(message = "病虫害名称不能为空")
    private String diseaseName;

    /**
     * 病虫害简介
     */
    private String brief;
    /**
     * 关联菌种ID
     */
    private List<Long> mushroomIds;
    /**
     * 为害症状
     */
    private String symptom;

    /**
     * 病原
     */
    private String pathogen;

    /**
     * 侵染
     */
    private String infection;

    /**
     * 发生规律
     */
    private String occurrenceRule;

    /**
     * 防治
     */
    private String prevention;


    /**
     * 类型：仅支持 {@link DiseasePestTypeEnum} 中的ID值（1=病害，2=虫害）
     */
    @NotNull(message = "病虫害类型不能为空")
    private DiseasePestTypeEnum itemType;

    /**
     * 形态
     */
    private String morphology;

    /**
     * 习性
     */
    private String habits;

    /**
     * 关联的菌种列表
     * @return
     */
    private List<MushroomInfo> mushroomList;
    // 新增：转换为枚举的工具方法
}