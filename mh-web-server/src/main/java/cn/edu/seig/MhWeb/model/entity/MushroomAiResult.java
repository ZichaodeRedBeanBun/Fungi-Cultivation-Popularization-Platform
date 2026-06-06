package cn.edu.seig.MhWeb.model.entity;

import lombok.Data;

@Data
public class MushroomAiResult {
    private String mushroomName;   // 蘑菇名称（仅当置信度高时有值）
    private Integer isPoison;       // 0无毒 1有毒
    private Double confidence;      // 置信度（仅当有毒时有效）
    private String toxicity;        // 毒性分类原始标签（poisonous/edible/conditionally_edible）
}