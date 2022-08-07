package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.exception.ValidPasswordException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.LocaleResolver;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class ErrorControllerAdvice extends SimpleUrlAuthenticationFailureHandler {
    private MessageSource messages;

    private LocaleResolver localeResolver;

    @ExceptionHandler(ValidPasswordException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String oldPasswordInvalidError(final ValidPasswordException throwable, final Model model) {
        log.trace("Exception during execution of application", throwable);
        model.addAttribute("error", "Your old password is invalid");
        return "error";
    }
}
