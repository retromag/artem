package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.entity.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.UserModel;
import com.example.cryptocurrencyexchanger.entity.VerificationToken;
import com.example.cryptocurrencyexchanger.event.OnRegistrationCompleteEvent;
import com.example.cryptocurrencyexchanger.service.token.TokenService;
import com.example.cryptocurrencyexchanger.service.user.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Controller
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {
    UserService userService;
    TokenService tokenService;
    MessageSource messages;
    ApplicationEventPublisher eventPublisher;

    @GetMapping("/login")
    public ModelAndView login(@RequestParam("error") final Optional<String> error, ModelMap model) {
        error.ifPresent(e -> model.addAttribute("error", e));

        return new ModelAndView("login", model);
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new UserModel());

        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserModel userModel, BindingResult result,
                                      HttpServletRequest request, Model model) {
        ExchangerUser existing = userService.findByEmail(userModel.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            return "registration";
        }

        userService.saveNewUser(userModel);
        final ExchangerUser user = userService.findByEmail(userModel.getEmail());
        final String token = UUID.randomUUID().toString();

        final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl, token));
        model.addAttribute("token", token);

        return "mailSent";
    }

    @Transactional
    @GetMapping("/registration/confirm")
    public String confirmRegistration(final HttpServletRequest request, final Model model, @RequestParam("token") final String token) {
        final Locale locale = request.getLocale();

        final VerificationToken verificationToken = tokenService.getVerificationToken(token);
        if (verificationToken == null) {
            final String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "badUser";
        }

        final ExchangerUser user = verificationToken.getUser();

        if (user == null) {
            final String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "badUser";
        }

        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            model.addAttribute("message", messages.getMessage("auth.message.expired", null, locale));
            model.addAttribute("expired", true);
            model.addAttribute("token", token);
            return "badUser";
        }

        userService.activateUser(user);

        return "accApproved";
    }
}
