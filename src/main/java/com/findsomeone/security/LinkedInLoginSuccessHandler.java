package com.findsomeone.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findsomeone.dto.LoginTokenResponse;
import com.findsomeone.entity.User;
import com.findsomeone.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LinkedInLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public LinkedInLoginSuccessHandler(UserRepository userRepository,
                                       JwtService jwtService,
                                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        OidcUser principal = (OidcUser) authentication.getPrincipal();
        User user = userRepository.findByLinkedinSubject(principal.getSubject())
                .orElseThrow(() -> new ServletException(
                        "LinkedIn account was not provisioned"));

        String accessToken = jwtService.generateToken(user.getId());
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new LoginTokenResponse(
                accessToken, "Bearer", jwtService.getExpirationTimeSeconds(),
                user.getId()));
    }
}
