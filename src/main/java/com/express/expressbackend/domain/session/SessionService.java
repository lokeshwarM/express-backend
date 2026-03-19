package com.express.expressbackend.domain.session;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerType;

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

    private static final double SESSION_FEE = 50.0;
    private static final double LISTENER_SHARE = 0.70;
    private static final double PLATFORM_SHARE = 0.30;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          ListenerRepository listenerRepository,
                          WalletRepository walletRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ✅ One-shot: balance check + create + start + notify
    @Transactional
    public SessionResponse initiateCall(String email, SessionType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Session> activeSession =
                sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.STARTED);
        if (activeSession.isPresent()) {
            throw new RuntimeException("You already have an active session");
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        double balance = ledgerEntryRepository.findByWalletId(wallet.getId())
                .stream()
                .mapToDouble(LedgerEntry::getAmount)
                .sum();

        if (balance < SESSION_FEE) {
            throw new RuntimeException("Insufficient balance. Please recharge.");
        }

        Listener listener = listenerRepository.findRandomAvailableListener();
        if (listener == null) {
            throw new RuntimeException("No listeners available right now. Try again shortly.");
        }

        Session session = new Session();
        session.setUser(user);
        session.setListener(listener);
        session.setType(type);
        session.setStatus(SessionStatus.STARTED);
        session.setStartedAt(Instant.now());
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

    public SessionResponse createSession(String email, SessionType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listener listener = listenerRepository.findRandomAvailableListener();
        if (listener == null) {
            throw new RuntimeException("No listeners available");
        }

        Optional<Session> activeSession =
                sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.STARTED);
        if (activeSession.isPresent()) {
            throw new RuntimeException("User already has an active session");
        }

        Session session = new Session();
        session.setUser(user);
        session.setListener(listener);
        session.setType(type);
        session.setStatus(SessionStatus.CREATED);
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

        User user = session.getUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        double balance = ledgerEntryRepository.findByWalletId(wallet.getId())
                .stream()
                .mapToDouble(LedgerEntry::getAmount)
                .sum();

        if (balance < SESSION_FEE) {
            throw new RuntimeException("Insufficient balance");
        }

        session.setStatus(SessionStatus.STARTED);
        session.setStartedAt(Instant.now());

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional
    public SessionResponse endSession(UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.STARTED) {
            throw new RuntimeException("Session cannot be ended");
        }

        Instant endTime = Instant.now();
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(endTime);

        long seconds = Duration.between(session.getStartedAt(), endTime).getSeconds();
        long minutes = Math.max(1, (long) Math.ceil(seconds / 60.0));

        double rate = session.getType() == SessionType.VOICE ? 2.5 : 3.0;
        double amount = minutes * rate;
        double listenerAmount = amount * LISTENER_SHARE;
        double platformAmount = amount * PLATFORM_SHARE;

        Wallet wallet = walletRepository.findByUserId(session.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        LedgerEntry debit = new LedgerEntry();
        debit.setWallet(wallet);
        debit.setAmount(-amount);
        debit.setType(LedgerType.SESSION_DEBIT);
        ledgerEntryRepository.save(debit);
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        Wallet listenerWallet = walletRepository
                .findByUserId(session.getListener().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Listener wallet not found"));
        LedgerEntry credit = new LedgerEntry();
        credit.setWallet(listenerWallet);
        credit.setAmount(listenerAmount);
        credit.setType(LedgerType.LISTENER_CREDIT);
        ledgerEntryRepository.save(credit);
        listenerWallet.setBalance(listenerWallet.getBalance() + listenerAmount);
        walletRepository.save(listenerWallet);

        User platformUser = userRepository
                .findByPublicDisplayId("PLATFORM")
                .orElseThrow(() -> new RuntimeException("Platform user not found"));
        Wallet platformWallet = walletRepository
                .findByUserId(platformUser.getId())
                .orElseThrow(() -> new RuntimeException("Platform wallet not found"));
        LedgerEntry platformEntry = new LedgerEntry();
        platformEntry.setWallet(platformWallet);
        platformEntry.setAmount(platformAmount);
        platformEntry.setType(LedgerType.PLATFORM_COMMISSION);
        ledgerEntryRepository.save(platformEntry);
        platformWallet.setBalance(platformWallet.getBalance() + platformAmount);
        walletRepository.save(platformWallet);

        Listener listener = session.getListener();
        listener.setAvailable(true);
        listenerRepository.save(listener);

        Session savedSession = sessionRepository.save(session);
        return toResponse(savedSession);
    }

    // ✅ Returns all ENDED sessions for the current user
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