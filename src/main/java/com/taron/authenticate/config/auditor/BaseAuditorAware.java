package com.taron.authenticate.config.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BaseAuditorAware implements AuditorAware<String> {

    public static final String ANONYMOUSE_USER = "anonymousUser";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUSE_USER.equals(authentication.getName())) {
            return Optional.empty();
        }

        String[] authorityAndUserId = authentication.getName().split("_");
        return Optional.of(authorityAndUserId[1]);
    }
}
