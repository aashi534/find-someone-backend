package com.findsomeone.service;

import com.findsomeone.dto.ConnectionRequestResponse;
import com.findsomeone.dto.ConnectionResponse;
import com.findsomeone.dto.MessageResponse;
import com.findsomeone.dto.SendConnectionRequest;
import com.findsomeone.dto.SendMessageRequest;
import com.findsomeone.entity.*;
import com.findsomeone.repository.ConnectionMessageRepository;
import com.findsomeone.repository.ConnectionRepository;
import com.findsomeone.repository.ConnectionRequestRepository;
import com.findsomeone.repository.UserBlockRepository;
import com.findsomeone.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ConnectionService {

    private final UserRepository userRepository;
    private final ConnectionRequestRepository requestRepository;
    private final ConnectionRepository connectionRepository;
    private final ConnectionMessageRepository messageRepository;
    private final UserBlockRepository blockRepository;

    public ConnectionService(
            UserRepository userRepository,
            ConnectionRequestRepository requestRepository,
            ConnectionRepository connectionRepository,
            ConnectionMessageRepository messageRepository,
            UserBlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.connectionRepository = connectionRepository;
        this.messageRepository = messageRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public ConnectionRequestResponse sendRequest(Long requesterId,
                                                SendConnectionRequest input) {
        User requester = findUser(requesterId);
        User recipient = findUser(input.recipientId());
        if (requesterId.equals(input.recipientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot connect with yourself");
        }
        if (blockRepository.existsBetweenUsers(requesterId, input.recipientId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!recipient.isDiscoverable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!recipient.getConnectionPurposes().contains(input.purpose())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This user is not currently open to that connection purpose");
        }

        if (requestRepository.existsByRequesterIdAndRecipientIdAndStatus(
                requesterId, input.recipientId(), ConnectionRequestStatus.PENDING)
                || requestRepository.existsByRequesterIdAndRecipientIdAndStatus(
                input.recipientId(), requesterId, ConnectionRequestStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A pending request already exists for these users");
        }

        long lowId = Math.min(requesterId, input.recipientId());
        long highId = Math.max(requesterId, input.recipientId());
        Connection existingConnection = connectionRepository.findPair(lowId, highId)
                .orElse(null);
        if (existingConnection != null
                && existingConnection.getPurposes().contains(input.purpose())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You are already connected for this purpose");
        }

        ConnectionRequest request = new ConnectionRequest();
        request.setRequester(requester);
        request.setRecipient(recipient);
        request.setPurpose(input.purpose());
        request.setNote(input.note() == null ? null : input.note().trim());
        request.setStatus(ConnectionRequestStatus.PENDING);
        return toRequest(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> incomingRequests(Long userId) {
        Set<Long> blockedUserIds = Set.copyOf(
                blockRepository.findAllBlockedUserIds(userId));
        return requestRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().filter(request -> !blockedUserIds.contains(
                        request.getRequester().getId()))
                .map(this::toRequest).toList();
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> outgoingRequests(Long userId) {
        Set<Long> blockedUserIds = Set.copyOf(
                blockRepository.findAllBlockedUserIds(userId));
        return requestRepository.findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream().filter(request -> !blockedUserIds.contains(
                        request.getRecipient().getId()))
                .map(this::toRequest).toList();
    }

    @Transactional
    public ConnectionRequestResponse accept(Long requestId, Long userId) {
        ConnectionRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Connection request not found"));
        if (!request.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the recipient can accept this request");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending requests can be accepted");
        }
        if (blockRepository.existsBetweenUsers(request.getRequester().getId(),
                request.getRecipient().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This request is no longer available");
        }

        User requester = request.getRequester();
        User recipient = request.getRecipient();
        long lowId = Math.min(requester.getId(), recipient.getId());
        long highId = Math.max(requester.getId(), recipient.getId());
        Connection connection = connectionRepository.findPair(lowId, highId)
                .orElseGet(() -> {
                    Connection created = new Connection();
                    created.setUserOne(lowId == requester.getId() ? requester : recipient);
                    created.setUserTwo(highId == requester.getId() ? requester : recipient);
                    return created;
                });
        connection.addPurpose(request.getPurpose());
        connectionRepository.save(connection);

        request.setStatus(ConnectionRequestStatus.ACCEPTED);
        return toRequest(requestRepository.save(request));
    }

    @Transactional
    public ConnectionRequestResponse decline(Long requestId, Long userId) {
        ConnectionRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Connection request not found"));
        if (!request.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the recipient can decline this request");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending requests can be declined");
        }
        request.setStatus(ConnectionRequestStatus.DECLINED);
        return toRequest(requestRepository.save(request));
    }

    @Transactional
    public void cancel(Long requestId, Long userId) {
        ConnectionRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Connection request not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the requester can cancel this request");
        }
        if (request.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending requests can be cancelled");
        }
        request.setStatus(ConnectionRequestStatus.CANCELLED);
        requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponse> connections(Long userId) {
        Set<Long> blockedUserIds = Set.copyOf(
                blockRepository.findAllBlockedUserIds(userId));
        return connectionRepository.findAllForUser(userId).stream()
                .filter(connection -> !blockedUserIds.contains(
                        connection.getUserOne().getId().equals(userId)
                                ? connection.getUserTwo().getId()
                                : connection.getUserOne().getId()))
                .map(connection -> toConnection(connection, userId)).toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long connectionId, Long senderId,
                                       SendMessageRequest input) {
        Connection connection = findConnectionForUser(connectionId, senderId);
        if (!connection.getPurposes().contains(input.purpose())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This connection is not active for that purpose");
        }
        User sender = findUser(senderId);
        ConnectionMessage message = new ConnectionMessage();
        message.setConnection(connection);
        message.setSender(sender);
        message.setBody(input.body().trim());
        message.setPurpose(input.purpose());
        return toMessage(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> messages(Long connectionId, Long userId,
                                          ConnectionPurpose purpose,
                                          int page, int size) {
        findConnectionForUser(connectionId, userId);
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be >= 0 and size must be between 1 and 100");
        }
        var messagePage = purpose == null
                ? messageRepository.findByConnectionIdOrderByCreatedAtDesc(
                        connectionId, PageRequest.of(page, size))
                : messageRepository.findByConnectionIdAndPurposeOrderByCreatedAtDesc(
                        connectionId, purpose, PageRequest.of(page, size));
        List<MessageResponse> result = new ArrayList<>(messagePage.stream()
                .map(this::toMessage).toList());
        java.util.Collections.reverse(result);
        return result;
    }

    private Connection findConnectionForUser(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Connection not found"));
        if (!connection.getUserOne().getId().equals(userId)
                && !connection.getUserTwo().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not a participant in this connection");
        }
        Long otherUserId = connection.getUserOne().getId().equals(userId)
                ? connection.getUserTwo().getId() : connection.getUserOne().getId();
        if (blockRepository.existsBetweenUsers(userId, otherUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Connection not found");
        }
        return connection;
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ConnectionRequestResponse toRequest(ConnectionRequest request) {
        return new ConnectionRequestResponse(
                request.getId(), request.getRequester().getId(),
                request.getRequester().getName(), request.getRecipient().getId(),
                request.getRecipient().getName(), request.getPurpose(), request.getNote(),
                request.getStatus(), request.getCreatedAt());
    }

    private ConnectionResponse toConnection(Connection connection, Long userId) {
        User other = connection.getUserOne().getId().equals(userId)
                ? connection.getUserTwo() : connection.getUserOne();
        return new ConnectionResponse(connection.getId(), other.getId(), other.getName(),
                Set.copyOf(connection.getPurposes()), connection.getCreatedAt());
    }

    private MessageResponse toMessage(ConnectionMessage message) {
        return new MessageResponse(message.getId(), message.getSender().getId(),
                message.getSender().getName(), message.getBody(),
                message.getPurpose(), message.getCreatedAt());
    }
}
