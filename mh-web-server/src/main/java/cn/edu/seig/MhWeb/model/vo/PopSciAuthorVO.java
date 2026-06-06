package cn.edu.seig.MhWeb.model.vo;

import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科普号列表VO（对应歌手的ArtistVO）
 * @author su
 */
@Data
public class PopSciAuthorVO {
    /**
     * 科普号ID
     */
    private Long userId;

    /**
     * 科普号名称
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 科普号认证号码（仅待认证/已认证科普号填写）
     */
    private String certificateNum;

    /**
     * 科普号类型：2=待认证，3=已认证
     */
    private UserTypeEnum userType;

    /**
     * 所属单位
     */
    private String affiliateUnit;

    /**
     * 账号状态：1=正常，2=封禁
     */
    private UserStatusEnum userStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}