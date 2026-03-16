package com.express.expressbackend.domain.wallet;

import org.springframework.web.bind.annotation.*;

import com.express.expressbackend.domain.common.ApiResponse;

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
    public ApiResponse<Double> getBalance(@PathVariable UUID userId) {
        return new ApiResponse<>(walletService.getBalance(userId));
    }

    // Recharge wallet
    @PostMapping("/{userId}/recharge")
    public ApiResponse<Double> recharge(@PathVariable UUID userId,
                                        @RequestParam double amount) {
        return new ApiResponse<>(walletService.recharge(userId, amount));
    }
}