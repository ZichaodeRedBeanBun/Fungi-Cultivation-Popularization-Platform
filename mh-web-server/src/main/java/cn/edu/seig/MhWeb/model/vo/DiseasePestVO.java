package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import cn.edu.seig.MhWeb.model.entity.Picture;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 病虫害VO（返回前端视图对象）
 *
 * @author su
 */
@Data
public class DiseasePestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 病虫害主键ID
     */
    private Long id;

    /**
     * 病虫害名称（如：蘑菇褐斑病）
     */
    private String diseaseName;

    /**
     * 关联菌种
     */
    private List<MushroomInfo> mushroomList;

    /**
     * 病虫害简介
     */
    private String brief;

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
     * 类型ID（1=病害，2=虫害）
     */
    private DiseasePestTypeEnum itemType;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 形态
     */
    private String morphology;

    /**
     * 习性
     */
    private String habits;

    /**
     * 病虫害封面图
     */
    private String coverUrl;
}