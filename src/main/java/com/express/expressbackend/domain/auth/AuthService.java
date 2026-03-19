package com.express.expressbackend.domain.auth;

import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.express.expressbackend.domain.user.UserRole;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;
    private final ListenerRepository listenerRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ListenerRepository listenerRepository,
                       JwtService jwtService,
                       WalletRepository walletRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.listenerRepository = listenerRepository;
        this.jwtService = jwtService;
        this.walletRepository = walletRepository;
    }

    public AuthResponse signup(SignupRequest request) {

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setActive(true);

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

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!valid) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(user.getId(), user.getEmail(), token, user.getRole().name());
    }
}