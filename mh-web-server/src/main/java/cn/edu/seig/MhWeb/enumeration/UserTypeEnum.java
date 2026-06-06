package cn.edu.seig.MhWeb.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

// 新增：用户身份枚举（对应数据库user_type）
@Getter
public enum UserTypeEnum {
    ORDINARY_USER(1, "普通用户"),
    PENDING_POPSCI(2, "待认证科普号"),
    AUTHED_POPSCI(3, "已认证科普号");
    @EnumValue
    private final Integer code;
    private final String desc;

    UserTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}