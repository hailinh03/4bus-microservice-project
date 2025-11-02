package com.mss.project.user_service.enums;

import lombok.Getter;

@Getter
public enum UserSortField {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    USERNAME("username"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),
    ROLE("role");

    private String fieldName;

    UserSortField(String fieldName) {
        this.fieldName = fieldName;
    }

}
