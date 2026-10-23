package com.findsomeone.service;

import com.findsomeone.dto.UpdateProfileRequest;
import com.findsomeone.dto.UserProfileResponse;
import com.findsomeone.entity.User;
import com.findsomeone.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(Long id) {
        return toProfile(findUser(id));
    }

    public UserProfileResponse getDiscoverableUser(Long id) {
        User user = findUser(id);
        if (!user.isDiscoverable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return toProfile(user);
    }

    public List<UserProfileResponse> getDiscoverableUsers() {
        return userRepository.findByDiscoverableTrueOrderByNameAsc()
                .stream().map(this::toProfile).toList();
    }

    public UserProfileResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findUser(id);
        if (request.name() != null) user.setName(request.name().trim());
        if (request.bio() != null) user.setBio(request.bio().trim());
        if (request.connectionPurposes() != null) {
            user.setConnectionPurposes(request.connectionPurposes());
        }
        if (request.discoverable() != null) user.setDiscoverable(request.discoverable());
        return toProfile(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(), user.getName(), user.getBio(),
                user.getConnectionPurposes(), user.getIdentityVerificationStatus(),
                user.isEmailVerified());
    }
}
