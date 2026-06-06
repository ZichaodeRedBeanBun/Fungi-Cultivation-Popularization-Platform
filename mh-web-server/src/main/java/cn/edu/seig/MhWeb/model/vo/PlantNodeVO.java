package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 种植节点展示VO
 *
 * @author su
 */
@Data
public class PlantNodeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点主键ID
     */
    private Long nodeId;

    /**
     * 关联种植计划ID
     */
    private Long planId;

    /**
     * 节点类型（备料/灭菌/接种/发菌/出菇/采收）
     */
    private String nodeType;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点预计执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planTime;

    /**
     * 节点提醒时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    /**
     * 节点实际完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualTime;

    /**
     * 节点状态：1=未完成 2=已完成 3=逾期未完成
     */
    private Integer status;

    /**
     * 节点状态名称（前端展示用）
     */
    private String statusName;

    /**
     * 节点备注
     */
    private String remark;

}