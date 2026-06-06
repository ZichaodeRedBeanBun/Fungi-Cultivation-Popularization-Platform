package cn.edu.seig.MhWeb.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ChatResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private String reply;              // AI 回复内容
    
    // 图片识别结果字段（仅当上传图片时才有值）
    private String mushroomName;       // 蘑菇名称
    private Integer isPoison;          // 0 无毒 1 有毒
    private Double confidence;         // 置信度
    private String toxicity;           // 毒性分类标签
}