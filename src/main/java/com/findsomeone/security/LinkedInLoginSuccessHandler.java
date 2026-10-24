package com.findsomeone.security;

import com.findsomeone.entity.User;
import com.findsomeone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LinkedInLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendUrl;

    public LinkedInLoginSuccessHandler(UserRepository userRepository,
                                       JwtService jwtService,
                                       @Value("${app.frontend-url:http://localhost:5173}")
                                       String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
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
        String callback = frontendUrl.replaceAll("/+$", "")
                + "/auth/callback#access_token="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&token_type=Bearer&expires_in="
                + jwtService.getExpirationTimeSeconds()
                + "&user_id=" + user.getId();
        response.sendRedirect(callback);
    }
}
