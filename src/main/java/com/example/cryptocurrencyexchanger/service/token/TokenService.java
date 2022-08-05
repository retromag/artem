package com.example.cryptocurrencyexchanger.service.token;

import com.example.cryptocurrencyexchanger.entity.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.VerificationToken;

public interface TokenService {
    VerificationToken getVerificationToken(String token);

    void createVerificationTokenForUser(ExchangerUser user, String token);

    ExchangerUser getUserByVerificationToken(String verificationToken);
}
