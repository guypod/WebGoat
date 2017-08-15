package org.owasp.webwolf.requests;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.boot.actuate.trace.TraceRepository;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.Optional.of;

/**
 * Keep track of all the incoming requests, we are only keeping track of request originating from
 * WebGoat and only if there is a cookie (otherwise we can never relate it back to a user).
 *
 * @author nbaars
 * @since 8/13/17.
 */
@Slf4j
@AllArgsConstructor
public class WebWolfTraceRepository implements TraceRepository {

    private static final int MAX_REQUESTS = 100;
    private final int webGoatPort;


    private final Map<Cookie, ConcurrentLinkedDeque<Trace>> cookieTraces = Maps.newConcurrentMap();

    @Override
    public List<Trace> findAll() {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("nice", "Great you found the standard Spring Boot tracing endpoint!");
        Trace trace = new Trace(new Date(), map);
        return Lists.newArrayList(trace);
    }

    public List<Trace> findTraceForCookie(Cookie cookie) {
        return Lists.newArrayList(cookieTraces.getOrDefault(cookie, new ConcurrentLinkedDeque<>()));
    }

    @Override
    public void add(Map<String, Object> map) {
        Optional<String> host = getFromHeaders("host", map);
        if (host.isPresent() && host.get().contains("8080")) {
            Optional<Cookie> cookie = getFromHeaders("cookie", map).map(c -> of(new Cookie(c))).orElse(of(new Cookie("1")));
            cookie.ifPresent(c -> {
                ConcurrentLinkedDeque<Trace> traces = this.cookieTraces.getOrDefault(c, new ConcurrentLinkedDeque<>());
                traces.addFirst(new Trace(new Date(), map));
                if (traces.size() >= MAX_REQUESTS) {
                    traces.removeLast();
                }
                cookieTraces.put(c, traces);
            });
        }
        log.trace("Host not from WebGoat but: {}, skipping...", host);
    }


    private Optional<String> getFromHeaders(String header, Map<String, Object> map) {
        Map<String, Object> headers = (Map<String, Object>) map.get("headers");
        if (headers != null) {
            Map<String, Object> request = (Map<String, Object>) headers.get("request");
            if (request != null) {
                return Optional.ofNullable((String) request.get(header));
            }
        }
        return Optional.empty();
    }
}
