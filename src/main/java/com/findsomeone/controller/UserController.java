package com.findsomeone.controller;

import com.findsomeone.dto.UpdateProfileRequest;
import com.findsomeone.dto.UserProfileResponse;
import com.findsomeone.entity.ConnectionPurpose;
import com.findsomeone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public List<UserProfileResponse> getDiscoverableUsers(
            @RequestParam(required = false) ConnectionPurpose purpose,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city) {
        if (query != null && query.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "query must be 100 characters or fewer");
        }
        if (city != null && city.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "city must be 100 characters or fewer");
        }
        return userService.getDiscoverableUsers(currentUserId(), purpose, query, city);
    }

    @GetMapping("/{id}")
    public UserProfileResponse getUser(@PathVariable Long id) {
        return userService.getDiscoverableUser(id, currentUserId());
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
