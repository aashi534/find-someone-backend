package com.findsomeone.controller;

import com.findsomeone.dto.SafetyReportRequest;
import com.findsomeone.dto.SafetyReportResponse;
import com.findsomeone.dto.MessageSafetyReportRequest;
import com.findsomeone.dto.UserBlockResponse;
import com.findsomeone.service.SafetyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/safety")
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/blocks/{userId}")
    public ResponseEntity<UserBlockResponse> block(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(safetyService.block(currentUserId(), userId));
    }

    @GetMapping("/blocks")
    public List<UserBlockResponse> blocks() {
        return safetyService.blocks(currentUserId());
    }

    @DeleteMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable Long userId) {
        safetyService.unblock(currentUserId(), userId);
    }

    @PostMapping("/reports")
    public ResponseEntity<SafetyReportResponse> report(
            @Valid @RequestBody SafetyReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(safetyService.report(currentUserId(), request));
    }

    @PostMapping("/messages/{messageId}/reports")
    public ResponseEntity<SafetyReportResponse> reportMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageSafetyReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(safetyService.reportMessage(currentUserId(), messageId, request));
    }

    private Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
