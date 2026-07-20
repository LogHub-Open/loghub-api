package io.loghub.loghub_api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${loghub.api.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || requestApiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing API Key\", \"message\": \"Header X-API-KEY is required\"}");
            return;
        }

        if (!isValidApiKey(requestApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API Key\", \"message\": \"The provided API Key is not valid\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Constant-time comparison to avoid leaking the API key via response-time timing attacks.
     */
    private boolean isValidApiKey(String requestApiKey) {
        return MessageDigest.isEqual(
                validApiKey.getBytes(StandardCharsets.UTF_8),
                requestApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow CORS preflight requests (OPTIONS) without authentication
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow health check endpoints and H2 console without authentication
        return path.equals("/health")
                || path.equals("/")
                || path.startsWith("/h2-console");
    }
}

