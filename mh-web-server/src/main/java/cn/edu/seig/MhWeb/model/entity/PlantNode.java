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
 * 种植计划节点表实体
 *
 * @author su
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_plant_node")
public class PlantNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点主键ID
     */
    @TableId(value = "node_id", type = IdType.AUTO)
    private Long nodeId;

    /**
     * 关联种植计划ID（mh_plant_plan的plan_id）
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 节点类型（备料/灭菌/接种/发菌/出菇/采收）
     */
    @TableField("node_type")
    private String nodeType;

    /**
     * 节点名称（如香菇接种后7天翻堆）
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点预计执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("plan_time")
    private LocalDateTime planTime;

    /**
     * 节点提醒时间（默认提前1天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("remind_time")
    private LocalDateTime remindTime;

    /**
     * 节点实际完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("actual_time")
    private LocalDateTime actualTime;

    /**
     * 节点状态：1=未完成 2=已完成 3=逾期未完成
     */
    @TableField("status")
    private Integer status;

    /**
     * 节点备注（如翻堆时菌丝长势良好）
     */
    @TableField("remark")
    private String remark;

}