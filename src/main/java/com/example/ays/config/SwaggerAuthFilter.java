package com.example.ays.config;

import com.example.ays.entity.AppUser;
import com.example.ays.enums.UserRole;
import com.example.ays.service.CurrentUserService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class SwaggerAuthFilter implements Filter {

    private final CurrentUserService currentUserService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            String token = null;

            if (req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if ("ays_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
            }

            if (token == null) {
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token != null) {
                try {
                    AppUser user = currentUserService.fromAuthorization("Bearer " + token);
                    if (user.getRole() == UserRole.ADMIN) {
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (Exception e) {
                    // Ignore, proceed to 403
                }
            }

            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required for API Docs");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }
}
