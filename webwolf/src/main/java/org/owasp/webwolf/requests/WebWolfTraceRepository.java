package org.owasp.webwolf.requests;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.boot.actuate.trace.TraceRepository;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Keep track of all the incoming requests, we are only keeping track of request originating from
 * WebGoat and only if there is a cookie (otherwise we can never relate it back to a user).
 *
 * @author nbaars
 * @since 8/13/17.
 */
@Slf4j
public class WebWolfTraceRepository implements TraceRepository {

    private final String webGoatPort;
    private final Map<Cookie, ConcurrentLinkedDeque<Trace>> cookieTraces;

    public WebWolfTraceRepository(String webGoatPort, HazelcastInstance hazelcastInstance) {
        this.webGoatPort = webGoatPort;
        this.cookieTraces = hazelcastInstance.getMap("cookieTraces");
    }

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
        if (host.isPresent() && host.get().contains(webGoatPort)) {
            Optional<Cookie> cookie = getFromHeaders("cookie", map).map(c -> of(new Cookie(c))).orElse(empty());
            cookie.ifPresent(c -> {
                ConcurrentLinkedDeque<Trace> traces = this.cookieTraces.getOrDefault(c, new ConcurrentLinkedDeque<>());
                traces.addFirst(new Trace(new Date(), map));
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
