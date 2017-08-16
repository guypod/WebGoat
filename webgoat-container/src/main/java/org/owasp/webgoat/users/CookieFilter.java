package org.owasp.webgoat.users;

import com.hazelcast.core.HazelcastInstance;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Keep a mapping between a user and the cookie. We need this to find out based on the cookie
 * who the user is in WebWolf.
 *
 * @author nbaars
 * @since 8/15/17.
 */
@AllArgsConstructor
public class CookieFilter extends OncePerRequestFilter {

    private final HazelcastInstance hazelcastInstance;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && principal instanceof WebGoatUser) {
            WebGoatUser user = (WebGoatUser) principal;
            Optional<Cookie> sessionCookie = findSessionCookie(httpServletRequest.getCookies());
            sessionCookie.ifPresent(c -> {
                hazelcastInstance.getMap("userSessions").put(user.getUsername(), c.getName() + "=" + c.getValue());
            });
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }


    private Optional<Cookie> findSessionCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }
}
