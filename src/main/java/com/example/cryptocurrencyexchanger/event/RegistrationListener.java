package com.example.cryptocurrencyexchanger.event;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.service.token.TokenService;
import com.example.cryptocurrencyexchanger.service.user.UserService;
import com.example.cryptocurrencyexchanger.util.mail.ConstructEmail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.JavaMailSender;

@Log4j2
@Component
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    UserService userService;
    TokenService tokenService;
    MessageSource messages;
    JavaMailSender mailSender;

    ConstructEmail constructEmail;

    @Override
    public void onApplicationEvent(@NonNull OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        ExchangerUser user = event.getUser();
        tokenService.createVerificationTokenForUser(user, event.getToken());

        final SimpleMailMessage email = constructEmail.constructRegistrationCompleteEmail(event, user, event.getToken());
        mailSender.send(email);
    }
}
