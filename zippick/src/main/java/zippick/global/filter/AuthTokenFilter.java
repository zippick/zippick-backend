package zippick.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import zippick.auth.dto.AuthTokenDTO;
import zippick.auth.mapper.AuthMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Component
public class AuthTokenFilter implements Filter {

    @Autowired
    private AuthMapper authMapper;

    private static final List<String> EXCLUDE_URL_PATTERNS = List.of(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/members/signup"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private boolean isExcluded(String path) {
        return EXCLUDE_URL_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (isExcluded(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.replace("Bearer", "").trim();
        AuthTokenDTO tokenInfo = authMapper.getToken(token);

        if (tokenInfo == null || tokenInfo.getExpiredAt().isBefore(LocalDateTime.now())) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }
}
