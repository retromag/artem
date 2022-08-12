package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import com.example.cryptocurrencyexchanger.entity.review.Review;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.UserModel;
import com.example.cryptocurrencyexchanger.entity.user.VerificationToken;
import com.example.cryptocurrencyexchanger.event.OnRegistrationCompleteEvent;
import com.example.cryptocurrencyexchanger.exception.ValidPasswordException;
import com.example.cryptocurrencyexchanger.service.amazon.AmazonService;
import com.example.cryptocurrencyexchanger.service.coin.CoinService;
import com.example.cryptocurrencyexchanger.service.exchange.ExchangeService;
import com.example.cryptocurrencyexchanger.service.review.ReviewService;
import com.example.cryptocurrencyexchanger.service.security.SecurityService;
import com.example.cryptocurrencyexchanger.service.token.TokenService;
import com.example.cryptocurrencyexchanger.service.user.UserService;
import com.example.cryptocurrencyexchanger.util.mail.ConstructEmail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Controller
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    UserService userService;
    TokenService tokenService;
    CoinService coinService;
    ExchangeService exchangeService;
    AmazonService amazonService;
    ReviewService reviewService;
    MessageSource messages;
    JavaMailSender mailSender;
    SecurityService securityService;
    ApplicationEventPublisher eventPublisher;
    ConstructEmail constructEmail;

    private static final int FIRST_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 5;

    @GetMapping("/")
    public String mainPage(Model model) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUser != null) {
            ExchangeOrder note = new ExchangeOrder();
            note.setUser(userService.findByEmail(currentUser));
            model.addAttribute("note", note);
        }

        List<Review> titleReviews = reviewService.getReviewsForTitlePage();
        model.addAttribute("reviews", titleReviews);

        return "index";
    }

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
    public String registerUserAccount(@Valid @ModelAttribute("user") UserModel userModel, BindingResult result,
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
            final SimpleMailMessage email = constructEmail.constructResetTokenEmail(appUrl, request.getLocale(), token, user);
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

    @PostMapping("/user/reset/password")
    public String savePassword(@RequestParam("password") final String password, @RequestParam("token") String token) {
        Optional<ExchangerUser> user = tokenService.getUserByPasswordResetToken(token);
        user.ifPresent(appUser -> userService.changeUserPassword(appUser, password));
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
    public String addNewCoin(Coin coin, @RequestParam(value = "image", required = false) MultipartFile image,
                             @RequestParam(value = "qrcode", required = false) MultipartFile qrcode) {

        coinService.addNewCoin(coin);
        if (!image.isEmpty()) {
            amazonService.uploadImage(image, coin.getSymbol());
        }
        if (!qrcode.isEmpty()) {
            amazonService.uploadQRCode(qrcode, coin.getSymbol());
        }

        return "redirect:/reserves";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/reserves/update/{id}")
    public String updateCoinPage(@PathVariable("id") Long id, Model model) {
        Coin coin = coinService.findCoinById(id);

        model.addAttribute("coin", coin);

        return "update_coin";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/reserves/update/coin")
    public String updateCoin(@Valid Coin coin, @RequestParam(value = "image", required = false) MultipartFile image,
                             @RequestParam(value = "qrcode", required = false) MultipartFile qrcode) {
        coinService.updateCoin(coin);
        if (!image.isEmpty()) {
            amazonService.uploadImage(image, coin.getSymbol());
        }
        if (!qrcode.isEmpty()) {
            amazonService.uploadQRCode(qrcode, coin.getSymbol());
        }

        return "redirect:/reserves";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/reserves/delete/{id}")
    public String deleteCoin(@PathVariable("id") Long id) {
        coinService.deleteCoin(id);

        return "redirect:/reserves";
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/account/history")
    public String viewUserExchangesHistory(Model model) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangerUser user = userService.findByEmail(currentUser);
        List<ExchangeOrder> orders = exchangeService.getAllExchangeOrders(user);

        model.addAttribute("orders", orders);

        return "account_history";
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/account/settings")
    public String viewUserAccountSettings(Model model) {
        model.addAttribute("user", new UserModel());

        return "account_settings";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/account/lock/{email}")
    public String blockUserAccount(@PathVariable("email") String email, HttpServletRequest request) {
        ExchangerUser user = userService.findByEmail(email);
        userService.lockUser(user);

        return getPreviousPageByRequest(request).orElse("/");
    }

    @SneakyThrows
    @PostMapping("/user/update/password")
    public String changeUserPassword(@RequestParam("confirmPassword") String password,
                                     @RequestParam("oldPassword") String oldPassword,
                                     @Valid @ModelAttribute("user") UserModel userModel) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!userService.checkIfValidOldPassword(user, oldPassword)) {
            throw new ValidPasswordException("Old password is invalid");
        }

        userService.changeUserPassword(user, password);

        return "redirect:/login";
    }

    @PostMapping("/exchange/create")
    public String completeOrder(@Valid @ModelAttribute("note") ExchangeOrder order, Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            order.setUser(user);
        }

        String code = UUID.randomUUID().toString();
        order.setUniqCode(code);
        exchangeService.makeAnExchange(order);

        model.addAttribute("order", exchangeService.findOrderByCode(code));
        model.addAttribute("ownerWallet", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getWallet());
        model.addAttribute("givenCoin", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getName());
        model.addAttribute("takenCoin", coinService.getCoinByCoinSymbol(order.getTakenCoin()).getName());

        return "orderPage";
    }

    @PostMapping("/exchange/pay")
    public String payOrder(@Valid @ModelAttribute("order") ExchangeOrder order, Model model) {
        exchangeService.payForExchange(order);

        model.addAttribute("order", exchangeService.findOrderById(order.getId()));
        model.addAttribute("givenCoin", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getName());
        model.addAttribute("takenCoin", coinService.getCoinByCoinSymbol(order.getTakenCoin()).getName());

        return "checkPage";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/order/confirm/{id}")
    public String confirmOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        exchangeService.completeExchange(exchangeService.findOrderById(id));

        return getPreviousPageByRequest(request).orElse("/");
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        exchangeService.cancelExchange(exchangeService.findOrderById(id));

        return getPreviousPageByRequest(request).orElse("/");
    }

    @PostMapping("/order/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        exchangeService.deleteExchange(exchangeService.findOrderById(id));

        return getPreviousPageByRequest(request).orElse("/");
    }

    @GetMapping("/review/all")
    public String showAllReviews(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(FIRST_PAGE);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        Page<Review> reviewPage = reviewService.getAllReviews(PageRequest.of(currentPage - 1, pageSize));
        int totalPages = reviewPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("review", new Review());
        model.addAttribute("reviews", reviewPage);

        return "reviewsPage";
    }

    @PostMapping("/review/new")
    public String createNewReview(@Valid @ModelAttribute("review") Review review, HttpServletRequest request) {
        reviewService.saveNewReview(review);

        return getPreviousPageByRequest(request).orElse("/");
    }

    @GetMapping("/forgot/password")
    public String showForgetPasswordPage() {
        return "forgot_password";
    }

    @GetMapping("/rules")
    public String showRulesPage() {
        return "rules";
    }

    @GetMapping("/aml")
    public String showAMLPage() {
        return "aml";
    }

    @GetMapping("/privacy-policy")
    public String showPrivacyPolicyPage() {
        return "privacy_policy";
    }

    @GetMapping("/contacts")
    public String showContactsPage() {
        return "supportPage";
    }

    @GetMapping("/update/password")
    public String showUpdatePasswordPage() {
        return "update_password";
    }

    private Optional<String> getPreviousPageByRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer")).map(requestUrl -> "redirect:" + requestUrl);
    }
}
