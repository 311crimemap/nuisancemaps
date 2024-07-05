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
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/init"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/categories"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/sources"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/data*"));

        NegatedRequestMatcher nRequestMatcher = new NegatedRequestMatcher(whiteList);

        http
                .securityMatcher(nRequestMatcher)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
