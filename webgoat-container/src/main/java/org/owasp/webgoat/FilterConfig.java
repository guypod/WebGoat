package org.owasp.webgoat;

import com.hazelcast.core.HazelcastInstance;
import org.owasp.webgoat.users.CookieFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nbaars
 * @since 8/17/17.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean cookieFilter(HazelcastInstance hazelcastInstance) {
        final FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(new CookieFilter(hazelcastInstance));
        filterRegBean.addUrlPatterns("/*");
        filterRegBean.setEnabled(Boolean.TRUE);
        return filterRegBean;
    }
}
