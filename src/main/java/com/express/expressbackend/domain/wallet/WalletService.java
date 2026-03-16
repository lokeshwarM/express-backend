package com.express.expressbackend.domain.wallet;

import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerRepository;

    public WalletService(WalletRepository walletRepository,
                         LedgerEntryRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public double getBalance(UUID userId) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return wallet.getBalance();
    }

    public double recharge(UUID userId, double amount) {

        if (amount <= 0) {
            throw new RuntimeException("Invalid recharge amount");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setType(LedgerType.RECHARGE);
        entry.setAmount(amount);

        ledgerRepository.save(entry);

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        return getBalance(userId);
    }
}