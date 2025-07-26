package zippick.global.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zippick.global.filter.AuthTokenFilter;

@Configuration
public class FilterConfig {

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public FilterRegistrationBean<AuthTokenFilter> registerAuthFilter() {
        FilterRegistrationBean<AuthTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authTokenFilter);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
