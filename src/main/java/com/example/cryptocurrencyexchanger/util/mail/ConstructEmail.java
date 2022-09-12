package com.example.cryptocurrencyexchanger.util.mail;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.event.OnRegistrationCompleteEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Component
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConstructEmail {

    MessageSource messages;
    JavaMailSender emailSender;
    SpringTemplateEngine templateEngine;

    public SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, ExchangerUser user) {
        final String url = contextPath + "/user/reset/password?token=" + token;
        final String subject = "Reset password";
        final String message = messages.getMessage("message.resetPassword",
                null, locale);

        return constructEmail(subject, message + " \r\n" + url, user);
    }

    public SimpleMailMessage constructRegistrationCompleteEmail(final OnRegistrationCompleteEvent event, final ExchangerUser user,
                                                                final String token) {
        final String subject = "Registration Confirmation";
        final String url = event.getAppUrl() + "/registration/confirm?token=" + token;
        final String message = messages.getMessage("message.regSuccessfulLink", null,
                "You registered successfully. To confirm your registration, please click on the below link.", event.getLocale());

        return constructEmail(subject, message + "\r\n" + url, user);
    }

    public void sendEmail(Mail mail) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        Context context = new Context();
        context.setVariables(mail.getProps());

        final String template = "inlined-css-template";
        String html = templateEngine.process(template, context);

        helper.setTo(mail.getMailTo());
        helper.setText(html, true);
        helper.setSubject(mail.getSubject());
        helper.setFrom(mail.getFrom());

        emailSender.send(message);
    }

    private SimpleMailMessage constructEmail(String subject, String body, ExchangerUser user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("hungryswap@gmail.com");
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        return email;
    }
}
