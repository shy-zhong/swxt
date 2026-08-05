package com.swxt.manager.Utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    public static String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    public static Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
    public static String getRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
