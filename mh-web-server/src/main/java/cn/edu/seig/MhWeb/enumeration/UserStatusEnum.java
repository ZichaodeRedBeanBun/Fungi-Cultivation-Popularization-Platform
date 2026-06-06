package cn.edu.seig.MhWeb.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ENABLE(1, "启用"),
    DISABLE(2, "禁用");

    @EnumValue
    private final Integer id;
    private final String userStatus;

    UserStatusEnum(Integer id, String userStatus) {
        this.id = id;
        this.userStatus = userStatus;
    }
    // 辅助：根据code找枚举（方便数据库值转枚举）
    public static UserStatusEnum getByCode(Integer code) {
        for (UserStatusEnum e : values()) {
            if (e.getId().equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("无效的用户状态：" + code);
    }

}
