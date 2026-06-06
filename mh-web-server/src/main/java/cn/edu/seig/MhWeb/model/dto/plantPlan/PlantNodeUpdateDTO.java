package cn.edu.seig.MhWeb.model.dto.plantPlan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 更新种植节点DTO
 *
 * @author su
 */
@Data
public class PlantNodeUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点主键ID（必填）
     */
    @NotNull(message = "节点ID不能为空")
    private Long nodeId;

    /**
     * 节点类型（备料/灭菌/接种/发菌/出菇/采收，可选）
     */
    private String nodeType;

    /**
     * 节点名称（可选）
     */
    private String nodeName;

    /**
     * 节点预计执行时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planTime;

    /**
     * 节点提醒时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    /**
     * 节点实际完成时间（可选，标记完成时填写）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime actualTime;

    /**
     * 节点状态：1=未完成 2=已完成 3=逾期未完成（可选）
     */
    private Integer status;

    /**
     * 节点备注（可选）
     */
    private String remark;

}