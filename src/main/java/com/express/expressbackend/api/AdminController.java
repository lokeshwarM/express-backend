package com.express.expressbackend.api;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.flag.FlagRecord;
import com.express.expressbackend.domain.flag.FlagRecordRepository;
import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.review.Review;
import com.express.expressbackend.domain.review.ReviewRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionStatus;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.user.UserRole;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.express.expressbackend.domain.ai.SessionEvaluationRepository;
import com.express.expressbackend.domain.ai.UserMemoryRepository;
import com.express.expressbackend.domain.ai.SessionEvaluation;
import com.express.expressbackend.domain.ai.UserMemory;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

        private final UserRepository userRepository;
        private final ListenerRepository listenerRepository;
        private final SessionRepository sessionRepository;
        private final FlagRecordRepository flagRecordRepository;
        private final LedgerEntryRepository ledgerEntryRepository;
        private final WalletRepository walletRepository;
        private final ReviewRepository reviewRepository;
        private final SessionEvaluationRepository evaluationRepository;
        private final UserMemoryRepository userMemoryRepository;

        public AdminController(UserRepository userRepository,
                                ListenerRepository listenerRepository,
                                SessionRepository sessionRepository,
                                FlagRecordRepository flagRecordRepository,
                                LedgerEntryRepository ledgerEntryRepository,
                                WalletRepository walletRepository,
                                ReviewRepository reviewRepository,
                                SessionEvaluationRepository evaluationRepository,
                                UserMemoryRepository userMemoryRepository
                        ) {
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.sessionRepository = sessionRepository;
        this.flagRecordRepository = flagRecordRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.walletRepository = walletRepository;
        this.reviewRepository = reviewRepository;
        this.evaluationRepository=evaluationRepository;
        this.userMemoryRepository=userMemoryRepository;

        }

        //  Guard — all endpoints check ADMIN role
        private void checkAdmin() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Access denied. Admin only.");
        }
        }

        // ──────────────────────────────────────────────────────────
        // DASHBOARD STATS
        // ──────────────────────────────────────────────────────────

        @GetMapping("/stats")
        public ApiResponse<Map<String, Object>> getDashboardStats() {
        checkAdmin();

        long totalUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.USER).count();
        long totalListeners = listenerRepository.count();
        long activeSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SessionStatus.STARTED).count();
        long totalSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED).count();
        long totalFlags = flagRecordRepository.count();
        long blacklistedListeners = listenerRepository.findAll().stream()
                .filter(Listener::isBlacklisted).count();

        double totalRevenue = ledgerEntryRepository.findAll().stream()
                .filter(e -> e.getType().name().equals("PLATFORM_COMMISSION"))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();

        double totalRecharges = ledgerEntryRepository.findAll().stream()
                .filter(e -> e.getType().name().equals("RECHARGE"))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();

        return new ApiResponse<>(Map.of(
                "totalUsers", totalUsers,
                "totalListeners", totalListeners,
                "activeSessions", activeSessions,
                "totalSessions", totalSessions,
                "totalFlags", totalFlags,
                "blacklistedListeners", blacklistedListeners,
                "totalRevenue", Math.round(totalRevenue * 100.0) / 100.0,
                "totalRecharges", Math.round(totalRecharges * 100.0) / 100.0
        ));
        }

        // ──────────────────────────────────────────────────────────
        // USER MANAGEMENT
        // ──────────────────────────────────────────────────────────

        @GetMapping("/users")
        public ApiResponse<List<Map<String, Object>>> getAllUsers() {
        checkAdmin();

        List<Map<String, Object>> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.USER)
                .map(u -> {
                        Wallet wallet = walletRepository.findByUserId(u.getId()).orElse(null);
                        return Map.of(
                                "id", (Object) u.getId().toString(),
                                "email", u.getEmail(),
                                "publicDisplayId", u.getPublicDisplayId(),
                                "emailVerified", u.isEmailVerified(),
                                "active", u.isActive(),
                                "balance", wallet != null ? wallet.getBalance() : 0.0,
                                "createdAt", u.getCreatedAt().toString()
                        );
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(users);
        }

        // ──────────────────────────────────────────────────────────
        // LISTENER MANAGEMENT
        // ──────────────────────────────────────────────────────────

        @GetMapping("/listeners")
        public ApiResponse<List<Map<String, Object>>> getAllListeners() {
        checkAdmin();

        List<Map<String, Object>> listeners = listenerRepository.findAll().stream()
                .map(l -> {
                        User u = l.getUser();
                        Wallet wallet = walletRepository.findByUserId(u.getId()).orElse(null);
                        long totalSessions = sessionRepository.findByListenerId(l.getId()).stream()
                                .filter(s -> s.getStatus() == SessionStatus.ENDED).count();
                        return Map.ofEntries(
                        Map.entry("id", (Object) l.getId().toString()),
                        Map.entry("userId", u.getId().toString()),
                        Map.entry("email", u.getEmail()),
                        Map.entry("publicDisplayId", u.getPublicDisplayId()),
                        Map.entry("available", l.isAvailable()),
                        Map.entry("blacklisted", l.isBlacklisted()),
                        Map.entry("flagCount", l.getRedFlagCount()),
                        Map.entry("rating", l.getAverageRating()),
                        Map.entry("totalSessions", totalSessions),
                        Map.entry("earnings", wallet != null ? wallet.getBalance() : 0.0),
                        Map.entry("createdAt", l.getCreatedAt().toString())
                        );
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(listeners);
        }

        @PostMapping("/listeners/{listenerId}/blacklist")
        public ApiResponse<String> blacklistListener(@PathVariable UUID listenerId) {
        checkAdmin();
        Listener listener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));
        listener.setBlacklisted(true);
        listener.setAvailable(false);
        listenerRepository.save(listener);
        return new ApiResponse<>("Listener blacklisted");
        }

        @PostMapping("/listeners/{listenerId}/unblacklist")
        public ApiResponse<String> unblacklistListener(@PathVariable UUID listenerId) {
        checkAdmin();
        Listener listener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));
        listener.setBlacklisted(false);
        listenerRepository.save(listener);
        return new ApiResponse<>("Listener unblacklisted");
        }

        @PostMapping("/listeners/{listenerId}/reset-flags")
        public ApiResponse<String> resetFlags(@PathVariable UUID listenerId) {
        checkAdmin();
        Listener listener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));
        listener.setRedFlagCount(0);
        listener.setBlacklisted(false);
        listenerRepository.save(listener);

        // Delete all flag records for this listener
        List<FlagRecord> flags = flagRecordRepository.findByListenerId(listenerId);
        flagRecordRepository.deleteAll(flags);

        return new ApiResponse<>("Flags reset");
        }

        // ──────────────────────────────────────────────────────────
        // FLAGS & ABUSE
        // ──────────────────────────────────────────────────────────

        @GetMapping("/flags")
        public ApiResponse<List<Map<String, Object>>> getAllFlags() {
        checkAdmin();

        List<Map<String, Object>> flags = flagRecordRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(f -> Map.of(
                        "id", (Object) f.getId().toString(),
                        "listenerId", f.getListener().getId().toString(),
                        "listenerEmail", f.getListener().getUser().getEmail(),
                        "reason", f.getReason(),
                        "sessionId", f.getSession().getId().toString(),
                        "createdAt", f.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(flags);
        }

        // ──────────────────────────────────────────────────────────
        // REVIEWS
        // ──────────────────────────────────────────────────────────

        @GetMapping("/reviews")
        public ApiResponse<List<Map<String, Object>>> getAllReviews() {
        checkAdmin();

        List<Map<String, Object>> reviews = reviewRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(r -> Map.of(
                        "id", (Object) r.getId().toString(),
                        "listenerEmail", r.getListener().getUser().getEmail(),
                        "userEmail", r.getUser().getEmail(),
                        "rating", r.getRating(),
                        "comment", r.getComment() != null ? r.getComment() : "",
                        "sessionId", r.getSession().getId().toString(),
                        "createdAt", r.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(reviews);
        }

        // ──────────────────────────────────────────────────────────
        // TRANSACTIONS
        // ──────────────────────────────────────────────────────────

        @GetMapping("/transactions")
        public ApiResponse<List<Map<String, Object>>> getAllTransactions(
                @RequestParam(required = false, defaultValue = "ALL") String filter) {
        checkAdmin();

        List<Map<String, Object>> txs = ledgerEntryRepository.findAll().stream()
                .filter(e -> {
                        if ("RECHARGE".equals(filter)) return e.getType().name().equals("RECHARGE");
                        if ("EARNINGS".equals(filter)) return e.getType().name().equals("LISTENER_CREDIT");
                        if ("WITHDRAWALS".equals(filter)) return e.getType().name().equals("WITHDRAWAL");
                        if ("DEDUCTIONS".equals(filter)) return e.getType().name().equals("SESSION_DEBIT");
                        return true; // ALL
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(e -> {
                        Wallet wallet = e.getWallet();
                        User user = wallet.getUser();
                        return Map.of(
                                "id", (Object) e.getId().toString(),
                                "type", e.getType().name(),
                                "amount", e.getAmount(),
                                "userEmail", user.getEmail(),
                                "userRole", user.getRole().name(),
                                "createdAt", e.getCreatedAt().toString()
                        );
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(txs);
        }

        // ──────────────────────────────────────────────────────────
        // SESSIONS
        // ──────────────────────────────────────────────────────────

        @GetMapping("/sessions")
        public ApiResponse<List<Map<String, Object>>> getAllSessions() {
        checkAdmin();

        List<Map<String, Object>> sessions = sessionRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(s -> {
                        long durationSeconds = 0;
                        if (s.getStartedAt() != null && s.getEndedAt() != null) {
                        durationSeconds = Duration.between(s.getStartedAt(), s.getEndedAt()).getSeconds();
                        }
                        return Map.of(
                                "id", (Object) s.getId().toString(),
                                "userEmail", s.getUser().getEmail(),
                                "listenerEmail", s.getListener().getUser().getEmail(),
                                "type", s.getType().name(),
                                "status", s.getStatus().name(),
                                "durationSeconds", durationSeconds,
                                "createdAt", s.getCreatedAt().toString()
                        );
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(sessions);
        }

        //  Phase 4 — Anomaly detection list
        @GetMapping("/anomalies")
        public ApiResponse<List<Map<String, Object>>> getAnomalies() {
        checkAdmin();

        List<Map<String, Object>> anomalies = evaluationRepository.findByAnomalyTrue()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(e -> Map.of(
                        "sessionId", (Object) e.getSession().getId().toString(),
                        "userEmail", e.getSession().getUser().getEmail(),
                        "listenerEmail", e.getSession().getListener().getUser().getEmail(),
                        "effectivenessScore", e.getEffectivenessScore(),
                        "durationSeconds", e.getDurationSeconds(),
                        "flagged", e.isFlagged(),
                        "engagementLevel", e.getEngagementLevel(),
                        "createdAt", e.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(anomalies);
        }

        //  Phase 5 — User memory overview for admin
        @GetMapping("/user-memories")
        public ApiResponse<List<Map<String, Object>>> getUserMemories() {
        checkAdmin();

        List<Map<String, Object>> memories = userMemoryRepository.findAll()
                .stream()
                .filter(m -> m.getTotalSessions() > 0)
                .sorted((a, b) -> Integer.compare(b.getTotalSessions(), a.getTotalSessions()))
                .map(m -> Map.of(
                        "userEmail", (Object) m.getUser().getEmail(),
                        "totalSessions", m.getTotalSessions(),
                        "avgSatisfactionScore", m.getAvgSatisfactionScore(),
                        "dominantEmotion", m.getDominantEmotion() != null ? m.getDominantEmotion() : "neutral",
                        "recurringStress", m.isRecurringStress(),
                        "emotionalTrend", m.getEmotionalTrend() != null ? m.getEmotionalTrend() : "stable",
                        "recurringTopics", m.getRecurringTopics() != null ? m.getRecurringTopics() : "",
                        "updatedAt", m.getUpdatedAt().toString()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(memories);
        }
}