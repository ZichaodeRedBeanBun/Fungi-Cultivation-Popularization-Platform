package cn.edu.seig.MhWeb.enumeration;

import lombok.Getter;

/**
 * 科普轮播图状态枚举
 */
@Getter
public enum PopSciBannerStatusEnum {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final Integer id;
    private final String desc;

    PopSciBannerStatusEnum(Integer id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public Integer getId() {
        return id;
    }
}