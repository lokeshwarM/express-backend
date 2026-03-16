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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    private static final double SESSION_FEE = 50.0;

    private static final double LISTENER_SHARE = 0.70;
    private static final double PLATFORM_SHARE = 0.30;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          ListenerRepository listenerRepository,
                          WalletRepository walletRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public SessionResponse createSession(String email, UUID listenerId, SessionType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listener listener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));

        if (!listener.isAvailable()) {
            throw new RuntimeException("Listener not available");
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

        // calculate duration
        long seconds = Duration.between(session.getStartedAt(), endTime).getSeconds();

        long minutes = Math.max(1, (long) Math.ceil(seconds / 60.0));

        double rate;

        if (session.getType() == SessionType.VOICE) {
            rate = 2.5;
        } else {
            rate = 3.0;
        }

        double amount = minutes * rate;
        double listenerAmount = amount * LISTENER_SHARE;
        double platformAmount = amount * PLATFORM_SHARE;

        // get wallet
        Wallet wallet = walletRepository.findByUserId(session.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // create ledger debit
        LedgerEntry debit = new LedgerEntry();
        debit.setWallet(wallet);
        debit.setAmount(-amount);
        debit.setType(LedgerType.SESSION_DEBIT);

        ledgerEntryRepository.save(debit);
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        //Listener Credit ledger and wallet
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

        //Platform commission
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

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getListener().getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}