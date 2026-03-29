package com.express.expressbackend.api;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.wallet.WithdrawalRequest;
import com.express.expressbackend.domain.wallet.WithdrawalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping("/me/withdraw")
    public ApiResponse<Double> withdraw(@RequestBody WithdrawalRequest request) {
        String email = AuthUtil.getCurrentUserEmail();
        double newBalance = withdrawalService.withdraw(email, request.getAmount());
        return new ApiResponse<>(newBalance);
    }
}