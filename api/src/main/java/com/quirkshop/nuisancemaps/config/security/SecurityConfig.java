package com.quirkshop.nuisancemaps.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // whitelist routes that will skip AuthenticationFilter entirely
        // default is to check authentication - expect an api key
        //
        // NB: "/** " matches both /, /aa, and /aa/bb - see doc
        //
        OrRequestMatcher whiteList = new OrRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/actuator/**"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/actuator/**"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/init"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/init"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/categories"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/categories"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/locales"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/locales"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/data311s*"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/data311s*"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.HEAD, "/datacrimes*"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/datacrimes*"));

        NegatedRequestMatcher nRequestMatcher = new NegatedRequestMatcher(whiteList);

        AuthenticationFilter authenticationFilter = new AuthenticationFilter();

        http
                .securityMatcher(nRequestMatcher)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        (authorize) -> authorize
                                .requestMatchers(whiteList).permitAll()
                                .anyRequest().authenticated())
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
