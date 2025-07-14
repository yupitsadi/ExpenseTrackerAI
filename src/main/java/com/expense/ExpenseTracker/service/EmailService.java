package com.expense.ExpenseTracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String to, String token){
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String text = "Hi,\n\nClick the link to reset your password:\n" + resetLink + "\n\nThis link will expire in 15 minutes.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("adityaraj98.a@gmail.com");

        mailSender.send(message);
    }


}
