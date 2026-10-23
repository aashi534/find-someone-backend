package com.findsomeone.controller;

import com.findsomeone.dto.UpdateProfileRequest;
import com.findsomeone.dto.UserProfileResponse;
import com.findsomeone.entity.User;
import com.findsomeone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Public profile directory; only members who opted into discovery are returned. */
    @GetMapping
    public List<UserProfileResponse> getDiscoverableUsers() {
        return userService.getDiscoverableUsers();
    }

    @GetMapping("/{id}")
    public UserProfileResponse getUser(@PathVariable Long id) {
        return userService.getDiscoverableUser(id);
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile() {
        return userService.getProfile(currentUserId());
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUserId(), request);
    }

    private Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
