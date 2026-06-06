package cn.edu.seig.MhWeb.model.dto.mushroom;

        import com.fasterxml.jackson.annotation.JsonAlias;
        import jakarta.validation.constraints.NotNull;
        import lombok.Data;

        import java.io.Serial;
        import java.io.Serializable;

/**
 * 菌种分页查询DTO
 *
 * @author su
 */
@Data
public class MushroomPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码（必填）
     */
    @NotNull(message = "页码不能为空")
    private Integer pageNum;

    /**
     * 每页数量（必填）
     */
    @NotNull(message = "每页数量不能为空")
    private Integer pageSize;

    /**
     * 食用菌种名称（模糊查询，可选）
     */
    @JsonAlias("mushroomName")
    private String mushroomName;

}