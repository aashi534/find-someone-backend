package com.findsomeone.config;

import com.findsomeone.security.JwtAuthenticationFilter;
import com.findsomeone.security.LinkedInLoginSuccessHandler;
import com.findsomeone.security.LinkedInOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LinkedInOidcUserService linkedInOidcUserService;
    private final LinkedInLoginSuccessHandler linkedInLoginSuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          LinkedInOidcUserService linkedInOidcUserService,
                          LinkedInLoginSuccessHandler linkedInLoginSuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.linkedInOidcUserService = linkedInOidcUserService;
        this.linkedInLoginSuccessHandler = linkedInLoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        ))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/oauth2/**", "/login/oauth2/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(linkedInOidcUserService))
                        .successHandler(linkedInLoginSuccessHandler))

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
