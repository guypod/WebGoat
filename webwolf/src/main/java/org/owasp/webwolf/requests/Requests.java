package org.owasp.webwolf.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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
        Stream<Trace> traceStream = getUserCookie().stream().map(c -> traceRepository.findTraceForCookie(c)).flatMap(l -> l.stream());
        List<Tracert> traces = traceStream.map(t -> new Tracert(t.getTimestamp(), path(t), toJsonString(t))).collect(Collectors.toList());
        m.addObject("traces", traces);
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
        return "No request found";
    }

    private List<Cookie> getUserCookie() {
        WebGoatUser user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Lists.newArrayList(new Cookie("1"));
    }

}
