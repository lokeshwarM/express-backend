package com.express.expressbackend.domain.payment;

import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.ledger.LedgerEntry;
import com.express.expressbackend.domain.ledger.LedgerEntryRepository;
import com.express.expressbackend.domain.ledger.LedgerType;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public PaymentService(UserRepository userRepository,
                          WalletRepository walletRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    // Creates a Razorpay order — amount in paise (₹1 = 100 paise)
    public CreateOrderResponse createOrder(double amountInRupees) throws RazorpayException {

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        long amountInPaise = Math.round(amountInRupees * 100);

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "receipt_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        return new CreateOrderResponse(
                order.get("id"),
                amountInPaise,
                "INR",
                keyId
        );
    }

    // Verifies Razorpay signature and credits wallet
    @Transactional
    public double verifyAndCredit(String email, VerifyPaymentRequest request) throws RazorpayException {

        // HMAC-SHA256 verification — prevents fake payment callbacks
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

        boolean valid = Utils.verifyPaymentSignature(
                new JSONObject()
                        .put("razorpay_order_id", request.getRazorpayOrderId())
                        .put("razorpay_payment_id", request.getRazorpayPaymentId())
                        .put("razorpay_signature", request.getRazorpaySignature()),
                keySecret
        );

        if (!valid) {
            throw new RuntimeException("Payment verification failed — invalid signature");
        }

        // Credit wallet
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setType(LedgerType.RECHARGE);
        entry.setAmount(request.getAmount());
        ledgerEntryRepository.save(entry);

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        return wallet.getBalance();
    }
}