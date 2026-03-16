package com.express.expressbackend.domain.auth;

import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.express.expressbackend.domain.user.UserRole;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;
    

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       WalletRepository walletRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletRepository = walletRepository;

    }

    public AuthResponse signup(SignupRequest request) {

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        String displayId = "USER" + System.currentTimeMillis();
        user.setPublicDisplayId(displayId);
        user.setRole(UserRole.USER);

        User saved = userRepository.save(user);
        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(0.0);

        walletRepository.save(wallet);

        String token = jwtService.generateToken(saved.getEmail());

        return new AuthResponse(saved.getId(), saved.getEmail(), token);
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

        return new AuthResponse(user.getId(), user.getEmail(), token);
    }
}