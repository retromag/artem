package com.example.cryptocurrencyexchanger.util.mail;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.event.OnRegistrationCompleteEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConstructEmail {

    MessageSource messages;

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

    private SimpleMailMessage constructEmail(String subject, String body, ExchangerUser user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("noreply@HELMETSWAP.com");
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        return email;
    }
}
