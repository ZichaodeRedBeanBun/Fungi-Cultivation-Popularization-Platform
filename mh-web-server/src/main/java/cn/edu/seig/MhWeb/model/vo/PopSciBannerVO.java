package cn.edu.seig.MhWeb.model.vo;

import lombok.Data;

@Data
public class PopSciBannerVO {
    private Long id; // 轮播图ID（管理员操作用）
    private Long contentId; // 科普内容ID（前端点击查详情用）
    private String contentTitle; // 科普内容标题
    private String contentType; // 内容类型（article/video）
    private String bannerUrl; // 轮播图URL（图文详情图/视频封面）
    private Integer sort; // 排序优先级
    private Integer status; // 状态（仅管理员端展示）
}