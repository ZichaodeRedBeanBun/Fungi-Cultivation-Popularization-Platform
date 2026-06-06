package cn.edu.seig.MhWeb.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.data.annotation.Id;
@Getter
public enum PopSciContentStatusEnum {

    PENDING(1, "PENDING", "待审核"),
    PASSED(2, "PASSED", "审核通过"),
    REJECTED(3, "REJECTED", "审核驳回");

    private final Integer id;
    private final String code;           // 新增code字段，作为字符串编码
    private final String contentStatus;  // 中文描述

    PopSciContentStatusEnum(Integer id, String code, String contentStatus) {
        this.id = id;
        this.code = code;
        this.contentStatus = contentStatus;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static PopSciContentStatusEnum fromCode(String code) {
        for (PopSciContentStatusEnum status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    public static PopSciContentStatusEnum getById(Integer id) {
        for (PopSciContentStatusEnum status : values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}