package com.xxxx.votting_spring.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String verificationCode) {
        System.out.println("Sending verification email from thread: " + Thread.currentThread().getName());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Account Verification - Voting System");

            String content = "<h3>Hello " + fullName + ",</h3>"
                    + "<p>Thank you for registering. Please use the code below to verify your account:</p>"
                    + "<h2 style='color:blue;'>" + verificationCode + "</h2>"
                    + "<p>Or verify via API: /api/auth/verify?email=" + toEmail + "&code=" + verificationCode + "</p>"
                    + "<br><p>Best regards,<br>Voting System Team</p>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email");
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Voting System");

            String content = "<h3>Hello " + fullName + ",</h3>"
                    + "<p>You requested a password reset. Please use the code below to reset your password:</p>"
                    + "<h2 style='color:red;'>" + verificationCode + "</h2>"
                    + "<p>If you did not request this, please ignore this email.</p>"
                    + "<br><p>Best regards,<br>Voting System Team</p>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    @Async
    public void sendPollInvitation(String toEmail, String pollTitle, String pollLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Invitation to Vote: " + pollTitle);

            String content = "<h3>You have been invited to vote!</h3>"
                    + "<p>You have been invited to participate in the poll: <strong>" + pollTitle + "</strong></p>"
                    + "<p>Click the link below to cast your vote:</p>"
                    + "<a href='" + pollLink
                    + "' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; border-radius: 5px;'>"
                    + "Vote Now"
                    + "</a>"
                    + "<p>Or copy this link: " + pollLink + "</p>"
                    + "<br><p>Best regards,<br>Voting System Team</p>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Don't throw exception here to avoid failing poll creation if one email fails
            System.err.println("Failed to send invitation to " + toEmail + ": " + e.getMessage());
        }
    }
}
