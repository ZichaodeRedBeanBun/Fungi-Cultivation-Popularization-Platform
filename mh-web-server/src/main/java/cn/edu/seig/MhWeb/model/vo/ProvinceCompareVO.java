package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProvinceCompareVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String categoryName;

    private String unit;

    private Integer year;

    private List<String> provinces;

    private List<BigDecimal> values;
}