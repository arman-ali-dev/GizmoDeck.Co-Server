package com.example.ecommerce.services.impl;

import com.example.ecommerce.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    final private JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Override
    public void sendVerificationOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");

            message.setTo(toEmail);
            message.setSubject("Verification OTP");
            message.setText("Your OTP is: " + otp + "\nThis code will expire in 2 minutes.");

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email");
        }
    }
}
