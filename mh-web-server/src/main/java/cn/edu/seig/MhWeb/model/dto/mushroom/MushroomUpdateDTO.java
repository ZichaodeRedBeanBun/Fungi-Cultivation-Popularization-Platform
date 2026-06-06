package cn.edu.seig.MhWeb.model.dto.mushroom;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编辑菌种DTO
 *
 * @author su
 */
@Data
public class MushroomUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（必填，定位待编辑的菌种）
     */
    @NotNull(message = "菌种ID不能为空")
    @JsonAlias("mushroomId")
    private Long id;

    /**
     * 食用菌种名称（可选，若传值则校验非空）
     */
    private String mushroomName;
    /**
     * 菌种基本信息
     */
    @NotBlank(message = "菌种基本信息不能为空")
    private String intro;
    /**
     * 菌种详细信息（可选，若传值则校验非空）
     */
    private String otherDetail;

    /**
     * 形态特征（可选）
     */
    @JsonAlias("morphologicalCharacteristics")
    private String contentMorphology;

    /**
     * 环境要求（可选）
     */
    @JsonAlias("environmentalRequirements")
    private String contentEnvironment;

    /**
     * 栽培技术（可选）
     */
    @JsonAlias("cultivationTechnology")
    private String contentCultivation;

}