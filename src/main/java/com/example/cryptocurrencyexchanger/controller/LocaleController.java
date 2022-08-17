package com.example.cryptocurrencyexchanger.controller;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@AllArgsConstructor
public class LocaleController {

    private final MessageSource messageSource;

    @GetMapping(value = "/international")
    public String changeLocale(@RequestParam(name = "lang") String lang, HttpSession session, HttpServletRequest request) {
        session.setAttribute("locale", lang);

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
}
