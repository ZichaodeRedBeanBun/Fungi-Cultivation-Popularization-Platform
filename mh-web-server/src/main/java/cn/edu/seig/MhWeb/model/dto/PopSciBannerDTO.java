package cn.edu.seig.MhWeb.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class PopSciBannerDTO {
    private Integer pageNum; // 页码
    private Integer pageSize; // 页大小
    @JsonAlias("bannerStatus")
    private Integer status; // 可选：筛选状态（1-启用，0-禁用）
}