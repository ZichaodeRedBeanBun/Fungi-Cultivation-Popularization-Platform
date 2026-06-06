package cn.edu.seig.MhWeb.model.entity;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.handler.DiseasePestTypeEnumHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 病虫害信息主表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_disease_pest")
public class DiseasePest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 病虫害主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 病虫害名称（如：蘑菇褐斑病）
     */
    @TableField("disease_name")
    private String diseaseName;

    /**
     * 病虫害简介
     */
    @TableField("brief")
    private String brief;

    /**
     * 为害症状
     */
    @TableField("symptom")
    private String symptom;

    /**
     * 病原
     */
    @TableField("pathogen")
    private String pathogen;

    /**
     * 侵染
     */
    @TableField("infection")
    private String infection;

    /**
     * 发生规律
     */
    @TableField("occurrence_rule")
    private String occurrenceRule;

    /**
     * 防治
     */
    @TableField("prevention")
    private String prevention;


    /**
     * 类型：1为病害，2为虫害
     */
    @TableField(value="item_type",typeHandler = DiseasePestTypeEnumHandler.class)
    private DiseasePestTypeEnum itemType;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 形态
     */
    @TableField("morphology")
    private String morphology;

    /**
     * 习性
     */
    @TableField("habits")
    private String habits;

}