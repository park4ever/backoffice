package com.youngwon.backoffice.common.auth;

import com.youngwon.backoffice.domain.user.UserRole;

import static com.youngwon.backoffice.domain.user.UserRole.*;

public record LoginUser(
        Long userId,
        Long shopId,
        UserRole role
) {
    public boolean isOwner() {
        return role == OWNER;
    }

    public boolean isStaff() {
        return role == STAFF;
    }
}