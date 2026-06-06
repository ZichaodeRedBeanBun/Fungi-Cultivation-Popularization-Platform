package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.model.entity.Picture;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菌种展示VO
 *
 * @author su
 */
@Data
public class MushroomVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 食用菌种名称
     */
    private String mushroomName;

    /**
     * 菌种基本信息
     */
    private String intro;


    /**
     * 形态特征（菌盖、菌柄、菌褶、孢子等形态描述）
     */
    private String contentMorphology;

    /**
     * 环境要求（温度、湿度、pH值、光照、通风、碳氮源等生长条件）
     */
    private String contentEnvironment;

    /**
     * 栽培技术（培养料配制、灭菌、接种、发菌、出菇管理、采收等）
     */
    private String contentCultivation;
    /**
     * 菌种其它信息
     */
    private String otherDetail;

    /**
     * 插入时间（格式化返回，便于前端展示）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间（格式化返回）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 封面图（单张，原有）
    private String coverUrl;

    // 新增：详情图列表（多张，type=详情图）
    private List<Picture> detailPicUrls; // 新写法
}