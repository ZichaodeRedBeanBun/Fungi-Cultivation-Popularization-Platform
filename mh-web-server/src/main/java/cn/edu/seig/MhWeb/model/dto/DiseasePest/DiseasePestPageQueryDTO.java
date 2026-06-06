package cn.edu.seig.MhWeb.model.dto.DiseasePest;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 病虫害分页查询DTO
 *
 * @author su
 */
@Data
public class DiseasePestPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize;

    /**
     * 病虫害名称（模糊查询）
     */
    private String diseaseName;

    /**
     * 类型：仅支持 {@link DiseasePestTypeEnum} 中的ID值（1=病害，2=虫害）
     */
    private Integer itemType;


    // 新增：转换为枚举的工具方法（供Service层调用）
    public DiseasePestTypeEnum getItemTypeEnum() {
        return DiseasePestTypeEnum.getById(this.itemType);
    }
}