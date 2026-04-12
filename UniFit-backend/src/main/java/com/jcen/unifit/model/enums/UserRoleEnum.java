package com.jcen.unifit.model.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    STUDENT("student"),
    ADMIN("admin"),
    TEACHER("teacher"),
    BAN("ban");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.value.equals(value)) {
                return roleEnum;
            }
        }
        return null;
    }
}
