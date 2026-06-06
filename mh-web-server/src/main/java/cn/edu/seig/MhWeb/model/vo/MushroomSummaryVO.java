package cn.edu.seig.MhWeb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菌种概要VO（列表展示专用，详情用原有MushroomVO）
 * @author su
 */
@Data
public class MushroomSummaryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID（跳转详情用） */
    private Long id;
    /** 食用菌种名称 */
    private String mushroomName;
    /** 菌种基本信息 */
    private String intro;
    /** 插入时间（格式化） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 修改时间（格式化） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /** 封面图（单张，列表核心展示） */
    private String coverUrl;
}