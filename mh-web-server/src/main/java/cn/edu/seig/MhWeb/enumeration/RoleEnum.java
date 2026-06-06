package cn.edu.seig.MhWeb.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),

    POPSCI_USER("ROLE_POPSCI_USER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

}
