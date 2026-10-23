package com.findsomeone.controller;

import com.findsomeone.dto.*;
import com.findsomeone.entity.ConnectionPurpose;
import com.findsomeone.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/requests")
    public ResponseEntity<ConnectionRequestResponse> sendRequest(
            @Valid @RequestBody SendConnectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionService.sendRequest(currentUserId(), request));
    }

    @GetMapping("/requests/incoming")
    public List<ConnectionRequestResponse> incomingRequests() {
        return connectionService.incomingRequests(currentUserId());
    }

    @GetMapping("/requests/outgoing")
    public List<ConnectionRequestResponse> outgoingRequests() {
        return connectionService.outgoingRequests(currentUserId());
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ConnectionRequestResponse accept(@PathVariable Long requestId) {
        return connectionService.accept(requestId, currentUserId());
    }

    @PatchMapping("/requests/{requestId}/decline")
    public ConnectionRequestResponse decline(@PathVariable Long requestId) {
        return connectionService.decline(requestId, currentUserId());
    }

    @DeleteMapping("/requests/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long requestId) {
        connectionService.cancel(requestId, currentUserId());
    }

    @GetMapping
    public List<ConnectionResponse> connections() {
        return connectionService.connections(currentUserId());
    }

    @PostMapping("/{connectionId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long connectionId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionService.sendMessage(
                        connectionId, currentUserId(), request));
    }

    @GetMapping("/{connectionId}/messages")
    public List<MessageResponse> messages(
            @PathVariable Long connectionId,
            @RequestParam(required = false) ConnectionPurpose purpose,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return connectionService.messages(
                connectionId, currentUserId(), purpose, page, size);
    }

    private Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
