package org.owasp.webwolf.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.owasp.webwolf.WebGoatUser;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Controller for fetching all the HTTP requests from WebGoat to WebWolf for a specific
 * user.
 *
 * @author nbaars
 * @since 8/13/17.
 */
@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping(value = "/requests")
public class Requests {

    private final WebWolfTraceRepository traceRepository;
    private final ObjectMapper objectMapper;
    private final HazelcastInstance hazelcastInstance;

    @AllArgsConstructor
    @Getter
    private class Tracert {
        private final Date date;
        private final String path;
        private final String json;
    }

    @GetMapping
    public ModelAndView get(HttpServletRequest request) {
        ModelAndView m = new ModelAndView("requests");
        getUserCookie().ifPresent(c -> {
            List<Tracert> traces = traceRepository.findTraceForCookie(c).stream().map(t -> new Tracert(t.getTimestamp(), path(t), toJsonString(t))).collect(toList());
            m.addObject("traces", traces);
        });
        return m;
    }

    private String path(Trace t) {
        return (String) t.getInfo().getOrDefault("path", "");
    }

    private String toJsonString(Trace t) {
        try {
            return objectMapper.writeValueAsString(t.getInfo());
        } catch (JsonProcessingException e) {
            log.error("Unable to create json", e);
        }
        return "No request(s) found";
    }

    private Optional<Cookie> getUserCookie() {
        WebGoatUser user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cookieValue = (String) hazelcastInstance.getMap("userSessions").get(user.getUsername());
        if (cookieValue != null) {
            return of(new Cookie(cookieValue));
        }
        return empty();
    }

}
