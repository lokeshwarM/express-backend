package com.express.expressbackend;

import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.user.UserRole;
import com.express.expressbackend.domain.wallet.Wallet;
import com.express.expressbackend.domain.wallet.WalletRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = "com.express.expressbackend.domain")
public class ExpressBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpressBackendApplication.class, args);
    }

    // Seeds the platform wallet user on every startup if it doesn't exist.
    // endSession() looks up publicDisplayId="PLATFORM" — without this it throws and sessions never end.
    @Bean
    public ApplicationRunner seedPlatformUser(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.findByPublicDisplayId("PLATFORM").isEmpty()) {
                User platform = new User();
                platform.setEmail("platform@express.internal");
                platform.setPublicDisplayId("PLATFORM");
                platform.setRole(UserRole.PLATFORM);
                platform.setActive(true);
                platform.setEmailVerified(true);
                platform.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

                User saved = userRepository.save(platform);

                Wallet wallet = new Wallet();
                wallet.setUser(saved);
                wallet.setBalance(0.0);
                walletRepository.save(wallet);

                System.out.println("✅ Platform user seeded.");
            } else {
                System.out.println("✅ Platform user already exists.");
            }
        };
    }
}