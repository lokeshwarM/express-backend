package com.express.expressbackend.domain.wallet;

import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerType;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.user.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WithdrawalService(UserRepository userRepository,
                             WalletRepository walletRepository,
                             LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public double withdraw(String email, double amount) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.LISTENER) {
            throw new RuntimeException("Only listeners can withdraw");
        }

        if (amount <= 0) {
            throw new RuntimeException("Invalid withdrawal amount");
        }

        if (amount < 100) {
            throw new RuntimeException("Minimum withdrawal amount is ₹100");
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct from wallet
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        // Record ledger entry
        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setType(LedgerType.WITHDRAWAL);
        entry.setAmount(-amount);
        ledgerEntryRepository.save(entry);

        return wallet.getBalance();
    }
}