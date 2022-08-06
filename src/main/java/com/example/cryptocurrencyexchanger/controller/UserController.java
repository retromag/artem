package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.UserModel;
import com.example.cryptocurrencyexchanger.entity.user.VerificationToken;
import com.example.cryptocurrencyexchanger.event.OnRegistrationCompleteEvent;
import com.example.cryptocurrencyexchanger.service.coin.CoinService;
import com.example.cryptocurrencyexchanger.service.security.SecurityService;
import com.example.cryptocurrencyexchanger.service.token.TokenService;
import com.example.cryptocurrencyexchanger.service.user.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@Log4j2
@Controller
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    UserService userService;
    TokenService tokenService;
    CoinService coinService;
    MessageSource messages;
    JavaMailSender mailSender;
    SecurityService securityService;
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

        return "activationMailSent";
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

    @PostMapping("/reset/password")
    public String resetPassword(final HttpServletRequest request, final Model model, @RequestParam("email") final String userEmail) {
        ExchangerUser user = userService.findByEmail(userEmail);
        if (user == null) {
            model.addAttribute("message", messages.getMessage("message.userNotFound", null, request.getLocale()));
            return "redirect:/login";
        }

        final String token = UUID.randomUUID().toString();
        tokenService.createPasswordResetTokenForUser(user, token);
        try {
            final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            final SimpleMailMessage email = constructResetTokenEmail(appUrl, request.getLocale(), token, user);
            mailSender.send(email);
        } catch (final MailAuthenticationException e) {
            log.trace("MailAuthenticationException", e);
            return "redirect:/emailError";
        } catch (final Exception e) {
            log.trace(e.getLocalizedMessage(), e);
            model.addAttribute("message", e.getLocalizedMessage());
            return "redirect:/emailError";
        }

        return "resetPasswordEmailSent";
    }

    @GetMapping("/user/reset/password")
    public String showChangePasswordPage(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        String result = securityService.validatePasswordResetToken(token);
        if (result != null) {
            return "redirect:/login";
        } else {
            redirectAttributes.addAttribute("token", token);
            return "redirect:/update/password";
        }
    }

    @PostMapping("/user/save/password")
    public String savePassword(@RequestParam("password") final String password, @RequestParam("token") String token) {
        Optional<ExchangerUser> user = tokenService.getUserByPasswordResetToken(token);
        user.ifPresent(appUser -> userService.changeUserPassword(appUser, password));
        return "redirect:/login";
    }

    @PostMapping("/user/update/password")
    public String changeUserPassword(@RequestParam("password") String password,
                                     @RequestParam("oldpassword") String oldPassword) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!userService.checkIfValidOldPassword(user, oldPassword)) {
//            throw new ValidPasswordException("Old password is invalid");
        }
        userService.changeUserPassword(user, password);
        return "redirect:/login";
    }

    @GetMapping("/reserves")
    public String showAllReserves(Model model) {
        Set<Coin> coinList = coinService.getAllCoins();

        model.addAttribute("coins", coinList);

        return "reserves";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/reserves/add")
    public String addNewCoinPage() {
        return "add_coin";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/reserves/add")
    public String addNewCoin(Coin coin) {
        coinService.addNewCoin(coin);

        return "redirect:/reserves";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/reserves/update/{id}")
    public String updateCoinPage(@PathVariable("id") Long id, Model model) {
        Coin coin = coinService.findCoinById(id);
        System.out.println(coin.toString());

        model.addAttribute("coin", coin);

        return "update_coin";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/reserves/update/coin")
    public String updateCoin(@Valid Coin coin) {
        coinService.updateCoin(coin);

        return "redirect:/reserves";
    }


    @GetMapping("/forgot/password")
    public String showForgetPasswordPage() {
        return "forgot_password";
    }

    @GetMapping("/update/password")
    public String redirectToUpdatePasswordPage() {
        return "updatePassword";
    }

    private SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, ExchangerUser user) {
        final String url = contextPath + "/user/reset/password?token=" + token;
        final String message = messages.getMessage("message.resetPassword",
                null, locale);
        return constructEmail(message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String body, ExchangerUser user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("noreply@HELMETSWAP.com");
        email.setSubject("Reset Password");
        email.setText(body);
        email.setTo(user.getEmail());
        return email;
    }
}
