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
 * 食用菌种信息表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_mushroom_info")
public class MushroomInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 食用菌种名称（唯一约束，避免重复入库）
     */
    @TableField("mushroom_name")
    private String mushroomName;
    /**
     * 菌种基本信息
     */
    @TableField("intro")
    private String intro;
    /**
     * 菌种详细信息
     */
    @TableField("other_detail")
    private String otherDetail;

    /**
     * 形态特征（菌盖、菌柄、菌褶、孢子等形态描述）
     */
    @TableField("content_morphology")
    private String contentMorphology;

    /**
     * 环境要求（温度、湿度、pH值、光照、通风、碳氮源等生长条件）
     */
    @TableField("content_environment")
    private String contentEnvironment;

    /**
     * 栽培技术（培养料配制、灭菌、接种、发菌、出菇管理、采收等）
     */
    @TableField("content_cultivation")
    private String contentCultivation;

    /**
     * 插入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间，记录数据最后更新的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;


}