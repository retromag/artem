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
import java.math.RoundingMode;
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
import java.math.BigDecimal;
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
            ExchangerUser user = userService.findByEmail(currentUser);
            note.setUser(user);
            model.addAttribute("note", note);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("walletAmount", user.getWalletAmount());
                model.addAttribute("userMargin", user.getUserMargin());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("coupon", user.getCoupon());
            }
        }

        Set<Coin> coinList = coinService.getAllCoins();
        List<Review> titleReviews = reviewService.getReviewsForTitlePage();

        if (coinList != null) {
            model.addAttribute("coins", coinList);
        }
        if (titleReviews != null) {
            model.addAttribute("reviews", titleReviews);
        }

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

        if (!userService.checkConfirmPassword(userModel)) {
            result.rejectValue("confirmPassword", null, "Confirm password must be the same");
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

        model.addAttribute("walletAmount", user.getWalletAmount());
        model.addAttribute("userMargin", user.getUserMargin());

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
    public String showChangePasswordPage(@RequestParam("token") String token, RedirectAttributes redirectAttributes, Model model) {
        String result = securityService.validatePasswordResetToken(token);

        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

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

        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        model.addAttribute("coins", coinList);

        return "reserves";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/reserves/add")
    public String addNewCoinPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

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

        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

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
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }
        if (orders != null) {
            model.addAttribute("orders", orders);
        }

        return "account_history";
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/account/settings")
    public String viewUserAccountSettings(Model model) {
        model.addAttribute("user", new UserModel());
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "account_settings";
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
            order.setEmail(user.getEmail());
            order.setCoupon(user.getCoupon());
        }

        String code = UUID.randomUUID().toString();
        order.setUniqCode(code);
        exchangeService.makeAnExchange(order);

        model.addAttribute("order", exchangeService.findOrderByCode(code));
        model.addAttribute("ownerWallet", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getWallet());
        model.addAttribute("givenCoin", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getSymbol());
        model.addAttribute("takenCoin", coinService.getCoinByCoinSymbol(order.getTakenCoin()).getSymbol());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "orderPage";
    }

    @PostMapping("/exchange/pay")
    public String payOrder(@RequestParam("uniqcode") String code, Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        ExchangeOrder order = exchangeService.findOrderByCode(code);
        exchangeService.payForExchange(order);

        model.addAttribute("order", order);
        model.addAttribute("givenCoin", coinService.getCoinByCoinSymbol(order.getGivenCoin()).getSymbol());
        model.addAttribute("takenCoin", coinService.getCoinByCoinSymbol(order.getTakenCoin()).getSymbol());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "checkPage";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/order/confirm/{id}")
    public String confirmOrder(@PathVariable("id") Long id, HttpServletRequest request) {

        exchangeService.completeExchange(exchangeService.findOrderById(id));

        return getPreviousPageByRequest(request).orElse("/");
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/order/freeze/{id}")
    public String freezeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        exchangeService.freezeExchange(exchangeService.findOrderById(id));

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

        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
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

    @Secured("ROLE_ADMIN")
    @PostMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, HttpServletRequest request) {
        reviewService.deleteReviewById(id);

        return getPreviousPageByRequest(request).orElse("/");
    }


    @Secured("ROLE_ADMIN")
    @GetMapping("/account/users")
    public String showUsersPage(Model model) {
        List<ExchangerUser> users = userService.getAllUsers();
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        model.addAttribute("users", users);


        return "userPage";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/user/update/wallet")
    public String setUserWalletAmount(@RequestParam("walletAmount") String amount,
                                      @RequestParam("userMargin") String margin,
                                      @RequestParam("email") String email, HttpServletRequest request) {
        ExchangerUser user = userService.findByEmail(email);
        if (amount != null) {
            userService.changeUserWalletAmount(user, new BigDecimal(amount));
        }
        if (margin != null) {
            userService.changeUserMargin(user, new BigDecimal(margin));
        }

        return getPreviousPageByRequest(request).orElse("/");
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/user/lock")
    public String lockUser(@RequestParam("email") String email, HttpServletRequest request) {
        ExchangerUser user = userService.findByEmail(email);
        userService.lockUser(user);

        return getPreviousPageByRequest(request).orElse("/");
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/user/unlock")
    public String unLockUser(@RequestParam("email") String email, HttpServletRequest request) {
        ExchangerUser user = userService.findByEmail(email);
        userService.unLockUser(user);

        return getPreviousPageByRequest(request).orElse("/");
    }

    @GetMapping("/forgot/password")
    public String showForgetPasswordPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "forgot_password";
    }

    @GetMapping("/rules")
    public String showRulesPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "rules";
    }

    @GetMapping("/aml")
    public String showAMLPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "aml";
    }

    @GetMapping("/privacy-policy")
    public String showPrivacyPolicyPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "privacy_policy";
    }

    @GetMapping("/contacts")
    public String showContactsPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "supportPage";
    }

    @GetMapping("/update/password")
    public String showUpdatePasswordPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "update_password";
    }

    @GetMapping("/about-us")
    public String showAboutUsPage(Model model) {
        ExchangerUser user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            model.addAttribute("walletAmount", user.getWalletAmount());
            model.addAttribute("userMargin", user.getUserMargin());
        }

        return "about_us";
    }

    private Optional<String> getPreviousPageByRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer")).map(requestUrl -> "redirect:" + requestUrl);
    }
}
