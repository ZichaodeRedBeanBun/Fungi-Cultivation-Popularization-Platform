package cn.edu.seig.MhWeb.model.dto.mushroom;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增菌种DTO
 *
 * @author su
 */
@Data
public class MushroomAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 食用菌种名称（必填，数据库唯一约束）
     */
    @NotBlank(message = "菌种名称不能为空")
    private String mushroomName;
    /**
     * 菌种基本信息
     */
    @NotBlank(message = "菌种基本信息不能为空")
    private String intro;
    /**
     * 菌种详细信息（必填）
     */
    private String otherDetail;
    /**
     * 形态特征（可选）（菌盖、菌柄、菌褶、孢子等形态描述，可选）
     */
    @JsonAlias("morphologicalCharacteristics")
    private String contentMorphology;

    /**
     * 环境要求（可选）
     */
    @JsonAlias("environmentalRequirements")
    private String contentEnvironment;

    /**
     * 栽培技术（可选）（培养料配制、灭菌、接种、发菌、出菇管理、采收等，可选）
     */
    @JsonAlias("cultivationTechnology")
    private String contentCultivation;

}