package com.example.cryptocurrencyexchanger.event;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.service.token.TokenService;
import com.example.cryptocurrencyexchanger.util.mail.ConstructEmail;
import com.example.cryptocurrencyexchanger.util.mail.Mail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    TokenService tokenService;
    JavaMailSender mailSender;

    ConstructEmail constructEmail;

    @Override
    public void onApplicationEvent(@NonNull OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    @SneakyThrows
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        ExchangerUser user = event.getUser();
        tokenService.createVerificationTokenForUser(user, event.getToken());

        final SimpleMailMessage email = constructEmail.constructRegistrationCompleteEmail(event, user, event.getToken());
        mailSender.send(email);

//        Mail mail = new Mail();
//        mail.setFrom("helmetswap@gmail.com");
//        mail.setMailTo(user.getEmail());
//
//        Map<String, Object> model = new HashMap<>();
//        model.put("token", "United States");
//        mail.setProps(model);
//
//        sendInlinedCssEmail(mail);
    }

    private void sendInlinedCssEmail(Mail mail) throws MessagingException {
        mail.setSubject("Email with Inlined CSS Responsive Thymeleaf Template!");

        Map<String, Object> model = new HashMap<>();
        model.put("name", "Peter Milanovich!");
        model.put("address", "Company Inc, 3 Abbey Road, San Francisco CA 94102");
        model.put("sign", "JavaByDeveloper");
        model.put("type", "TRANSACTIONAL");
        mail.setProps(model);

        constructEmail.sendEmail(mail);
    }
}
