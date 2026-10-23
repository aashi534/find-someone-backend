package com.findsomeone.security;

import com.findsomeone.entity.User;
import com.findsomeone.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkedInOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public LinkedInOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        Boolean emailVerified = oidcUser.getAttribute("email_verified");
        if (subject == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_USER_INFO,
                    "LinkedIn did not return a member ID",
                    null));
        }

        User user = userRepository.findByLinkedinSubject(subject).orElseGet(() -> {
            User existingByEmail = email == null || !Boolean.TRUE.equals(emailVerified)
                    ? null : userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (existingByEmail != null) {
                if (existingByEmail.getLinkedinSubject() != null) {
                    throw new OAuth2AuthenticationException(new OAuth2Error(
                            OAuth2ErrorCodes.INVALID_USER_INFO,
                            "This email is already linked to another LinkedIn account",
                            null));
                }
                existingByEmail.setLinkedinSubject(subject);
                return existingByEmail;
            }
            User created = new User();
            created.setEmail(email);
            created.setLinkedinSubject(subject);
            return created;
        });

        if (email != null && Boolean.TRUE.equals(emailVerified)) {
            User emailOwner = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (emailOwner != null && !emailOwner.getId().equals(user.getId())) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_USER_INFO,
                        "This verified email is already linked to another account",
                        null));
            }
            user.setEmail(email);
            user.setEmailVerified(true);
        }
        String name = oidcUser.getFullName();
        if (name != null && !name.isBlank()) user.setName(name);
        user.setLinkedinSubject(subject);
        userRepository.save(user);
        return oidcUser;
    }
}
