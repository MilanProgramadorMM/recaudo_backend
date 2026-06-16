package com.recaudo.api.config;

import com.recaudo.api.domain.model.version.AppVersionConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class VersionFilter extends OncePerRequestFilter {

    private final AppVersionConfig appVersionConfig;

    public VersionFilter(AppVersionConfig appVersionConfig) {
        this.appVersionConfig = appVersionConfig;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        /* Endpoints excluidos
        if (path.contains("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

         */

        String clientVersion =
                request.getHeader("X-App-Version");

        if (clientVersion == null) {

            response.setStatus(HttpStatus.UPGRADE_REQUIRED.value());

            response.setContentType("application/json");

            response.getWriter().write("""
            {
              "message":"Versión no enviada"
            }
            """);

            return;
        }

        if (!appVersionConfig.getVersion().equals(clientVersion)) {

            response.setStatus(HttpStatus.UPGRADE_REQUIRED.value());

            response.setContentType("application/json");

            response.getWriter().write("""
            {
              "message":"Debe actualizar la aplicacion"
            }
            """);

            return;
        }

        filterChain.doFilter(request, response);
    }
}