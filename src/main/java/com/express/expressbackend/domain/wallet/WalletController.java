package com.express.expressbackend.domain.wallet;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Get balance
    @GetMapping("/{userId}/balance")
    public double getBalance(@PathVariable UUID userId) {
        return walletService.getBalance(userId);
    }

    // Recharge wallet
    @PostMapping("/{userId}/recharge")
    public double recharge(@PathVariable UUID userId,
                           @RequestParam double amount) {
        return walletService.recharge(userId, amount);
    }
}