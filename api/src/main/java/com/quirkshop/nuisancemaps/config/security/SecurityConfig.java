package com.quirkshop.nuisancemaps.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // whitelist routes that will skip AuthenticationFilter entirely
        // default is to check authentication - expect an api key
        OrRequestMatcher whiteList = new OrRequestMatcher(
                AntPathRequestMatcher.antMatcher(HttpMethod.HEAD, "/init"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/init"),
                AntPathRequestMatcher.antMatcher(HttpMethod.HEAD, "/categories"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/categories"),
                AntPathRequestMatcher.antMatcher(HttpMethod.HEAD, "/sources"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/sources"),
                AntPathRequestMatcher.antMatcher(HttpMethod.HEAD, "/data311s*"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/data311s*"),
                AntPathRequestMatcher.antMatcher(HttpMethod.HEAD, "/datacrimes*"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/datacrimes*"));

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
