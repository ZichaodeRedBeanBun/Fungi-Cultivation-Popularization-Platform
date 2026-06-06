package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 科普号新增DTO（对应歌手的ArtistAddDTO）
 * @author su
 */
@Data
public class PopSciAuthorAddDTO {
    /**
     * 科普号用户名（4-16位字母/数字/下划线/连字符）
     */
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;


    /**
     * 科普号类型：2=待认证，3=已认证
     */
    private UserTypeEnum userType;


    /**
     * 所属单位
     */
    private String affiliateUnit;

    /**
     * 认证号码
     */
    private String certificateNum;
}