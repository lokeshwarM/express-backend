package com.express.expressbackend.domain.auth;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.otp.EmailService;
import com.express.expressbackend.domain.otp.OtpRecord;
import com.express.expressbackend.domain.otp.OtpRepository;
import com.express.expressbackend.domain.otp.OtpType;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.user.UserRole;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;
    private final ListenerRepository listenerRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ListenerRepository listenerRepository,
                       JwtService jwtService,
                       WalletRepository walletRepository,
                       OtpRepository otpRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.listenerRepository = listenerRepository;
        this.jwtService = jwtService;
        this.walletRepository = walletRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    // ✅ Step 1 of new signup — just send OTP, don't create user yet
    public void sendSignupOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered. Please login.");
        }
        sendOtp(email, OtpType.EMAIL_VERIFY);
    }

    // ✅ Step 2 of new signup — verify OTP + password + role → create user
    @Transactional
    public AuthResponse completeSignup(CompleteSignupRequest request) {

        // Re-check email not taken
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // Validate OTP
        validateOtp(request.getEmail(), request.getOtp(), OtpType.EMAIL_VERIFY);

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setActive(true);
        user.setEmailVerified(true); // Already verified via OTP above

        String displayId = request.getRole() + System.currentTimeMillis();
        user.setPublicDisplayId(displayId);

        User saved = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(0.0);
        walletRepository.save(wallet);

        if (saved.getRole() == UserRole.LISTENER) {
            Listener listener = new Listener();
            listener.setUser(saved);
            listener.setAvailable(false);
            listener.setBlacklisted(false);
            listener.setRedFlagCount(0);
            listenerRepository.save(listener);
        }

        String token = jwtService.generateToken(saved.getEmail());
        return new AuthResponse(saved.getId(), saved.getEmail(), token, saved.getRole().name());
    }

    // Legacy signup — kept for compatibility
    @Transactional
    public SignupInitResponse signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setActive(true);
        user.setEmailVerified(false);

        String displayId = request.getRole() + System.currentTimeMillis();
        user.setPublicDisplayId(displayId);

        User saved = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(0.0);
        walletRepository.save(wallet);

        if (saved.getRole() == UserRole.LISTENER) {
            Listener listener = new Listener();
            listener.setUser(saved);
            listener.setAvailable(false);
            listener.setBlacklisted(false);
            listener.setRedFlagCount(0);
            listenerRepository.save(listener);
        }

        sendOtp(saved.getEmail(), OtpType.EMAIL_VERIFY);
        return new SignupInitResponse(saved.getEmail(), true);
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyOtpRequest request) {
        validateOtp(request.getEmail(), request.getOtp(), OtpType.EMAIL_VERIFY);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), user.getEmail(), token, user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEmailVerified()) {
            sendOtp(user.getEmail(), OtpType.EMAIL_VERIFY);
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), user.getEmail(), token, user.getRole().name());
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getCredential());
            if (idToken == null) throw new RuntimeException("Invalid Google token");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            Optional<User> existing = userRepository.findByEmail(email);
            User user;

            if (existing.isPresent()) {
                user = existing.get();
            } else {
                String role = request.getRole() != null ? request.getRole() : "USER";

                user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole(UserRole.valueOf(role));
                user.setActive(true);
                user.setEmailVerified(true);
                user.setPublicDisplayId(role + System.currentTimeMillis());

                User saved = userRepository.save(user);

                Wallet wallet = new Wallet();
                wallet.setUser(saved);
                wallet.setBalance(0.0);
                walletRepository.save(wallet);

                if (saved.getRole() == UserRole.LISTENER) {
                    Listener listener = new Listener();
                    listener.setUser(saved);
                    listener.setAvailable(false);
                    listener.setBlacklisted(false);
                    listener.setRedFlagCount(0);
                    listenerRepository.save(listener);
                }

                user = saved;
            }

            String token = jwtService.generateToken(user.getEmail());
            return new AuthResponse(user.getId(), user.getEmail(), token, user.getRole().name());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));
        sendOtp(email, OtpType.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validateOtp(request.getEmail(), request.getOtp(), OtpType.PASSWORD_RESET);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void resendOtp(String email, OtpType type) {
        sendOtp(email, type);
    }

    private void sendOtp(String email, OtpType type) {
        otpRepository.deleteByEmailAndType(email, type);

        String code = String.format("%06d", new Random().nextInt(1000000));

        OtpRecord record = new OtpRecord();
        record.setEmail(email);
        record.setOtp(code);
        record.setType(type);
        record.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        otpRepository.save(record);

        String subject = type == OtpType.EMAIL_VERIFY
                ? "Verify your Express account"
                : "Reset your Express password";

        String purpose = type == OtpType.EMAIL_VERIFY
                ? "Use the OTP below to verify your email address."
                : "Use the OTP below to reset your password.";

        emailService.sendOtpEmail(email, code, subject, purpose);
    }

    private void validateOtp(String email, String code, OtpType type) {
        OtpRecord record = otpRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new one."));

        if (record.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!record.getOtp().equals(code)) {
            throw new RuntimeException("Invalid OTP");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }
}