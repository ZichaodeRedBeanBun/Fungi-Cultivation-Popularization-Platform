package cn.edu.seig.MhWeb.model.dto.plantPlan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新增种植节点DTO
 *
 * @author su
 */
@Data
public class PlantNodeAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联种植计划ID（必填）
     */
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    /**
     * 节点类型（备料/灭菌/接种/发菌/出菇/采收，必填）
     */
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    /**
     * 节点名称（如香菇接种后7天翻堆，必填）
     */
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /**
     * 节点预计执行时间（必填）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planTime;

    /**
     * 节点提醒时间（默认提前1天，可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    /**
     * 节点备注（可选）
     */
    private String remark;

}