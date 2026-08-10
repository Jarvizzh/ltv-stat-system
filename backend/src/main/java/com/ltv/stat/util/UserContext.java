package com.ltv.stat.util;

import com.ltv.stat.dto.TokenInfo;

public class UserContext {

    private static final ThreadLocal<TokenInfo> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(TokenInfo tokenInfo) {
        CURRENT_USER.set(tokenInfo);
    }

    public static TokenInfo getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
