package com.findsomeone.service;

import com.findsomeone.dto.SafetyReportRequest;
import com.findsomeone.dto.SafetyReportResponse;
import com.findsomeone.dto.UserBlockResponse;
import com.findsomeone.entity.*;
import com.findsomeone.repository.ConnectionRequestRepository;
import com.findsomeone.repository.SafetyReportRepository;
import com.findsomeone.repository.UserBlockRepository;
import com.findsomeone.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SafetyService {

    private final UserRepository userRepository;
    private final UserBlockRepository blockRepository;
    private final SafetyReportRepository reportRepository;
    private final ConnectionRequestRepository requestRepository;

    public SafetyService(UserRepository userRepository,
                         UserBlockRepository blockRepository,
                         SafetyReportRepository reportRepository,
                         ConnectionRequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.reportRepository = reportRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public UserBlockResponse block(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot block yourself");
        }
        User blocker = findUser(blockerId);
        User blocked = findUser(blockedId);

        UserBlock block = blockRepository.findByBlockerIdAndBlockedId(
                blockerId, blockedId).orElseGet(() -> {
            UserBlock created = new UserBlock();
            created.setBlocker(blocker);
            created.setBlocked(blocked);
            return blockRepository.save(created);
        });

        requestRepository.findBetweenUsersWithStatus(
                blockerId, blockedId, ConnectionRequestStatus.PENDING)
                .forEach(request -> request.setStatus(ConnectionRequestStatus.CANCELLED));
        return toBlockResponse(block);
    }

    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .ifPresent(blockRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<UserBlockResponse> blocks(Long blockerId) {
        return blockRepository.findByBlockerIdOrderByCreatedAtDesc(blockerId)
                .stream().map(this::toBlockResponse).toList();
    }

    @Transactional
    public SafetyReportResponse report(Long reporterId, SafetyReportRequest input) {
        if (reporterId.equals(input.reportedUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot report yourself");
        }
        User reporter = findUser(reporterId);
        User reported = findUser(input.reportedUserId());

        SafetyReport report = new SafetyReport();
        report.setReporter(reporter);
        report.setReportedUser(reported);
        report.setReason(input.reason());
        report.setDetails(input.details() == null || input.details().isBlank()
                ? null : input.details().trim());
        return toReportResponse(reportRepository.save(report));
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserBlockResponse toBlockResponse(UserBlock block) {
        return new UserBlockResponse(block.getBlocked().getId(),
                block.getBlocked().getName(), block.getCreatedAt());
    }

    private SafetyReportResponse toReportResponse(SafetyReport report) {
        return new SafetyReportResponse(report.getId(),
                report.getReportedUser().getId(), report.getReason(),
                report.getStatus(), report.getCreatedAt());
    }
}
