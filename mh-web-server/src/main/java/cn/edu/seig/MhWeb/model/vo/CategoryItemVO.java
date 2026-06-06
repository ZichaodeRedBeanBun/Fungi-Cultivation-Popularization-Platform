package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CategoryItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryName;

    private String categoryType;

    private String unit;
}