package com.express.expressbackend.api;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.payment.CreateOrderRequest;
import com.express.expressbackend.domain.payment.CreateOrderResponse;
import com.express.expressbackend.domain.payment.PaymentService;
import com.express.expressbackend.domain.payment.VerifyPaymentRequest;
import com.razorpay.RazorpayException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Step 1 — frontend calls this to get orderId
    @PostMapping("/create-order")
    public ApiResponse<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            return new ApiResponse<>(paymentService.createOrder(request.getAmount()));
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    // Step 2 — frontend calls this after Razorpay modal success
    @PostMapping("/verify")
    public ApiResponse<Double> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        try {
            String email = AuthUtil.getCurrentUserEmail();
            double newBalance = paymentService.verifyAndCredit(email, request);
            return new ApiResponse<>(newBalance);
        } catch (RazorpayException e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }
}