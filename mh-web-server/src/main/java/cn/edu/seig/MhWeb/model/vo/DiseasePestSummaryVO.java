package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 病虫害概要VO（列表展示专用，详情用原有DiseasePestVO+新增封面图）
 * @author su
 */
@Data
public class DiseasePestSummaryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 病虫害主键ID */
    private Long id;
    /** 病虫害名称 */
    private String diseaseName;
    /** 关联菌种 */
    private List<MushroomInfo> mushroomList;
    /** 病虫害简介 */
    private String brief;
    /** 类型（1=病害，2=虫害） */
    private DiseasePestTypeEnum itemType;
    /** 封面图（新增：列表展示用） */
    private String coverUrl;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}