package com.express.expressbackend.domain.session;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.express.expressbackend.domain.ai.SmartMatchingService;
import com.express.expressbackend.domain.ai.UserTagRepository;
import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerType;
import com.express.expressbackend.domain.ai.SessionIntelligenceService;

import com.express.expressbackend.domain.ai.SmartMatchingService;
import com.express.expressbackend.domain.ai.UserTag;
import com.express.expressbackend.domain.ai.UserTagRepository;
import com.express.expressbackend.domain.ai.UserMood;
import com.express.expressbackend.domain.ai.UserMoodRepository;

import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionIntelligenceService sessionIntelligenceService;

    private static final double MIN_BALANCE = 10.0;
    private static final long GRACE_PERIOD_SECONDS = 20;
    private static final double LISTENER_SHARE = 0.70;
    private static final double PLATFORM_SHARE = 0.30;

    private final SmartMatchingService smartMatchingService;
    private final UserTagRepository userTagRepository;
    private final UserMoodRepository userMoodRepository;


    public SessionService(SessionRepository sessionRepository,
                        UserRepository userRepository,
                        ListenerRepository listenerRepository,
                        WalletRepository walletRepository,
                        LedgerEntryRepository ledgerEntryRepository,
                        SimpMessagingTemplate messagingTemplate,
                        SessionIntelligenceService sessionIntelligenceService,
                        SmartMatchingService smartMatchingService,
                        UserTagRepository userTagRepository,
                        UserMoodRepository userMoodRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.messagingTemplate = messagingTemplate;
        this.sessionIntelligenceService = sessionIntelligenceService;
        this.smartMatchingService = smartMatchingService;
        this.userTagRepository = userTagRepository;
        this.userMoodRepository = userMoodRepository;
    }

    @Transactional
    public SessionResponse initiateCall(String email, SessionType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Session> activeSession =
                sessionRepository.findTopByUserIdAndStatusIn(
                        user.getId(), List.of(SessionStatus.STARTED, SessionStatus.CREATED));
        if (activeSession.isPresent()) {
            throw new RuntimeException("You already have an active session");
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        double rate = type == SessionType.VOICE ? 2.5 : 3.0;
        if (wallet.getBalance() < rate) {
            throw new RuntimeException(
                "Insufficient balance. Minimum ₹" + rate + " required to start a session."
            );
        }
        if (wallet.getBalance() < MIN_BALANCE) {
            throw new RuntimeException(
                "Insufficient balance. Please recharge at least ₹" + MIN_BALANCE + " to start a session."
            );
        }

        //  Smart matching — uses user tags + mood instead of random
        List<String> userTags = userTagRepository.findByUserId(user.getId())
                .stream()
                .map(com.express.expressbackend.domain.ai.UserTag::getTag)
                .collect(java.util.stream.Collectors.toList());

        String userMood = userMoodRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(com.express.expressbackend.domain.ai.UserMood::getMood)
                .orElse("neutral");

        Listener listener = smartMatchingService.findBestMatch(userTags, userMood);

        if (listener == null) {
            throw new RuntimeException("No listeners available right now. Try again shortly.");
        }

        Session session = new Session();
        session.setUser(user);
        session.setListener(listener);
        session.setType(type);
        session.setStatus(SessionStatus.STARTED);
        session.setStartedAt(null);
        session.setCreatedAt(OffsetDateTime.now());

        listener.setAvailable(false);
        listenerRepository.save(listener);

        Session saved = sessionRepository.save(session);

        messagingTemplate.convertAndSend(
                "/topic/listener/" + listener.getUser().getId(),
                Map.of(
                        "type", "incoming_call",
                        "sessionId", saved.getId().toString(),
                        "callType", type.name()
                )
        );

        return toResponse(saved);
    }

    //  Called when WebRTC actually connects — resets billing clock to real connection time
    @Transactional
    public void markConnected(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == SessionStatus.STARTED) {
            session.setStartedAt(Instant.now());
            sessionRepository.save(session);
        }
    }

    public SessionResponse createSession(String email, SessionType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listener listener = listenerRepository.findRandomAvailableListener();
        if (listener == null) {
            throw new RuntimeException("No listeners available");
        }

        Optional<Session> activeSession =
                sessionRepository.findTopByUserIdAndStatusIn(
                        user.getId(), List.of(SessionStatus.STARTED, SessionStatus.CREATED));
        if (activeSession.isPresent()) {
            throw new RuntimeException("User already has an active session");
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < MIN_BALANCE) {
            throw new RuntimeException(
                "Insufficient balance. Please recharge at least ₹" + MIN_BALANCE + " to start a session."
            );
        }

        Session session = new Session();
        session.setUser(user);
        session.setListener(listener);
        session.setType(type);
        session.setStatus(SessionStatus.CREATED);
        session.setStartedAt(null);
        session.setCreatedAt(OffsetDateTime.now());

        listener.setAvailable(false);
        listenerRepository.save(listener);

        Session saved = sessionRepository.save(session);

        messagingTemplate.convertAndSend(
                "/topic/listener/" + listener.getUser().getId(),
                Map.of(
                        "type", "incoming_call",
                        "sessionId", saved.getId().toString(),
                        "callType", type.name()
                )
        );

        return toResponse(saved);
    }

    @Transactional
    public SessionResponse startSession(UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new RuntimeException("Session cannot be started");
        }

        Wallet wallet = walletRepository.findByUserId(session.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < MIN_BALANCE) {
            throw new RuntimeException("Insufficient balance");
        }

        session.setStatus(SessionStatus.STARTED);
        session.setStartedAt(null); // Will be set when WebRTC connects
        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional
    public SessionResponse endSession(UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.STARTED
                && session.getStatus() != SessionStatus.CREATED) {
            throw new RuntimeException("Session is already ended");
        }

        Instant endTime = Instant.now();
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(endTime);
        sessionRepository.save(session);

        //  Only bill if startedAt was set (WebRTC actually connected)
        if (session.getStartedAt() != null) {

            long totalSeconds = Duration.between(session.getStartedAt(), endTime).getSeconds();

            // Grace period — under 20s is FREE
            if (totalSeconds >= GRACE_PERIOD_SECONDS) {

                long completedMinutes = totalSeconds / 60;
                long remainingSeconds = totalSeconds % 60;

                long billableMinutes;
                if (completedMinutes == 0) {
                    billableMinutes = 1;
                } else if (remainingSeconds >= 20) {
                    billableMinutes = completedMinutes + 1;
                } else {
                    billableMinutes = completedMinutes;
                }

                double rate = session.getType() == SessionType.VOICE ? 2.5 : 3.0;
                double amount = billableMinutes * rate;
                double listenerAmount = amount * LISTENER_SHARE;
                double platformAmount = amount * PLATFORM_SHARE;

                // Debit user
                Wallet userWallet = walletRepository.findByUserId(session.getUser().getId())
                        .orElseThrow(() -> new RuntimeException("User wallet not found"));
                double actualDeduction = Math.min(amount, userWallet.getBalance());
                LedgerEntry debit = new LedgerEntry();
                debit.setWallet(userWallet);
                debit.setAmount(-actualDeduction);
                debit.setType(LedgerType.SESSION_DEBIT);
                ledgerEntryRepository.save(debit);
                userWallet.setBalance(userWallet.getBalance() - actualDeduction);
                walletRepository.save(userWallet);

                double actualListenerAmount = actualDeduction * LISTENER_SHARE;
                double actualPlatformAmount = actualDeduction * PLATFORM_SHARE;

                // Credit listener
                Wallet listenerWallet = walletRepository
                        .findByUserId(session.getListener().getUser().getId())
                        .orElseThrow(() -> new RuntimeException("Listener wallet not found"));
                LedgerEntry credit = new LedgerEntry();
                credit.setWallet(listenerWallet);
                credit.setAmount(actualListenerAmount);
                credit.setType(LedgerType.LISTENER_CREDIT);
                ledgerEntryRepository.save(credit);
                listenerWallet.setBalance(listenerWallet.getBalance() + actualListenerAmount);
                walletRepository.save(listenerWallet);

                // Credit platform
                User platformUser = userRepository
                        .findByPublicDisplayId("PLATFORM")
                        .orElseThrow(() -> new RuntimeException("Platform user not found"));
                Wallet platformWallet = walletRepository
                        .findByUserId(platformUser.getId())
                        .orElseThrow(() -> new RuntimeException("Platform wallet not found"));
                LedgerEntry platformEntry = new LedgerEntry();
                platformEntry.setWallet(platformWallet);
                platformEntry.setAmount(actualPlatformAmount);
                platformEntry.setType(LedgerType.PLATFORM_COMMISSION);
                ledgerEntryRepository.save(platformEntry);
                platformWallet.setBalance(platformWallet.getBalance() + actualPlatformAmount);
                walletRepository.save(platformWallet);
            }
        }
        // If startedAt was null (never actually connected) — NO billing at all ✅

        // Free up listener
        Listener listener = session.getListener();
        listener.setAvailable(true);
        listenerRepository.save(listener);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                Map.of("type", "session_ended", "sessionId", sessionId.toString())
        );
        sessionIntelligenceService.evaluateSession(session);
        return toResponse(session);
    }

    public List<SessionResponse> getMyHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByUserId(user.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED)
                .sorted((a, b) -> {
                    if (a.getStartedAt() == null) return 1;
                    if (b.getStartedAt() == null) return -1;
                    return b.getStartedAt().compareTo(a.getStartedAt());
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getListener().getId(),
                session.getStatus(),
                session.getType(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}