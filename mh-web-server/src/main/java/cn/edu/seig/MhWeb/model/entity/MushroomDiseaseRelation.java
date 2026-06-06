package cn.edu.seig.MhWeb.model.entity;

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
 * 菌种-病虫害多对多关系表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_mushroom_disease_relation")
public class MushroomDiseaseRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id; // 原表是int unsigned，用Integer更贴合字段类型（参考里用Long是通用写法，也可替换为Long）

    /**
     * 菌种ID（关联mh_mushroom_info.id）
     */
    @TableField("mushroom_id")
    private Long mushroomId;

    /**
     * 病虫害ID（关联mh_disease_pest.id）
     */
    @TableField("disease_id")
    private Long diseaseId;

    /**
     * 分类ID（和病虫害表category_id一致）
     */
    @TableField("category_id")
    private Integer categoryId;

    /**
     * 分类名称（和病虫害表category_name一致）
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

}