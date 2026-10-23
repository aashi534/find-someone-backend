package com.findsomeone.service;

import com.findsomeone.dto.NotificationResponse;
import com.findsomeone.entity.AppNotification;
import com.findsomeone.entity.NotificationType;
import com.findsomeone.entity.User;
import com.findsomeone.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void create(User recipient, User actor, NotificationType type,
                       String title, String message, String referenceType,
                       Long referenceId) {
        AppNotification notification = new AppNotification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId, int page, int size) {
        validatePage(page, size);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(page, size))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        AppNotification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId, Instant.now());
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be >= 0 and size must be between 1 and 100");
        }
    }

    private NotificationResponse toResponse(AppNotification notification) {
        User actor = notification.getActor();
        return new NotificationResponse(notification.getId(),
                notification.getType(), notification.getTitle(),
                notification.getMessage(), actor == null ? null : actor.getId(),
                actor == null ? null : actor.getName(),
                notification.getReferenceType(), notification.getReferenceId(),
                notification.getCreatedAt(), notification.getReadAt());
    }
}
