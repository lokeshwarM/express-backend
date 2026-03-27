package com.express.expressbackend.domain.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp, String subject, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String html = """
                    <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                        <h2 style="color: #111; margin-bottom: 8px;">Express</h2>
                        <p style="color: #555; margin-bottom: 24px;">%s</p>
                        <div style="background: #f5f5f5; border-radius: 12px; padding: 24px; text-align: center;">
                            <p style="color: #888; font-size: 13px; margin: 0 0 8px;">Your OTP</p>
                            <h1 style="font-size: 42px; letter-spacing: 8px; color: #111; margin: 0;">%s</h1>
                        </div>
                        <p style="color: #888; font-size: 12px; margin-top: 20px;">
                            This OTP is valid for 10 minutes. Do not share it with anyone.
                        </p>
                    </div>
                    """.formatted(purpose, otp);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}