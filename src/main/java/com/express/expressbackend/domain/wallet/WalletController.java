package com.express.expressbackend.domain.wallet;

import org.springframework.web.bind.annotation.*;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerEntryResponse;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletController(WalletService walletService,
                             UserRepository userRepository,
                             WalletRepository walletRepository,
                             LedgerEntryRepository ledgerEntryRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping("/me/balance")
    public ApiResponse<Double> getMyBalance() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResponse<>(walletService.getBalance(user.getId()));
    }

    @PostMapping("/me/recharge")
    public ApiResponse<Double> rechargeMyWallet(@RequestParam double amount) {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResponse<>(walletService.recharge(user.getId(), amount));
    }

    // ✅ Returns all ledger entries for current user — newest first
    @GetMapping("/me/transactions")
    public ApiResponse<List<LedgerEntryResponse>> getMyTransactions() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        List<LedgerEntryResponse> entries = ledgerEntryRepository
                .findByWalletId(wallet.getId())
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(e -> new LedgerEntryResponse(
                        e.getId(),
                        e.getType(),
                        e.getAmount(),
                        e.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(entries);
    }
}