package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 科普号更新DTO（对应歌手的ArtistUpdateDTO）
 * @author su
 */
@Data
public class PopSciAuthorUpdateDTO {
    /**
     * 科普号ID（user_id）
     */
    private Long userId;

    /**
     * 科普号名称
     */
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    private String username;

    /**
     * 绑定邮箱
     */
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

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