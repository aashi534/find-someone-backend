package com.findsomeone.service;

import com.findsomeone.dto.UpdateProfileRequest;
import com.findsomeone.dto.UserProfileResponse;
import com.findsomeone.entity.User;
import com.findsomeone.entity.ConnectionPurpose;
import com.findsomeone.repository.UserRepository;
import com.findsomeone.repository.UserBlockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserBlockRepository blockRepository;

    public UserService(UserRepository userRepository, UserBlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
    }

    public UserProfileResponse getProfile(Long id) {
        return toProfile(findUser(id));
    }

    public UserProfileResponse getDiscoverableUser(Long id, Long viewerId) {
        User user = findUser(id);
        if (!user.isDiscoverable()
                || (!id.equals(viewerId)
                && blockRepository.existsBetweenUsers(viewerId, id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return toProfile(user);
    }

    public List<UserProfileResponse> getDiscoverableUsers(
            Long viewerId, ConnectionPurpose purpose, String query, String city) {
        java.util.Set<Long> blockedUserIds = java.util.Set.copyOf(
                blockRepository.findAllBlockedUserIds(viewerId));
        String normalizedQuery = query == null || query.isBlank()
                ? null : query.trim();
        String normalizedCity = city == null || city.isBlank()
                ? null : city.trim();
        return userRepository.searchDiscoverableUsers(purpose, normalizedQuery, normalizedCity)
                .stream().filter(user -> !user.getId().equals(viewerId)
                        && !blockedUserIds.contains(user.getId()))
                .map(this::toProfile).toList();
    }

    public UserProfileResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findUser(id);
        if (request.name() != null) user.setName(request.name().trim());
        if (request.bio() != null) user.setBio(request.bio().trim());
        if (request.city() != null) user.setCity(request.city().trim());
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
                user.getCity(),
                user.getConnectionPurposes(), user.getIdentityVerificationStatus(),
                user.isEmailVerified());
    }
}
