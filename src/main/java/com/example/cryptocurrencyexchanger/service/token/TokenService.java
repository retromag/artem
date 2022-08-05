package com.example.cryptocurrencyexchanger.service.token;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.VerificationToken;

import java.util.Optional;

public interface TokenService {
    VerificationToken getVerificationToken(String token);

    void createVerificationTokenForUser(ExchangerUser user, String token);

    ExchangerUser getUserByVerificationToken(String verificationToken);

    void createPasswordResetTokenForUser(ExchangerUser user, String token);

    Optional<ExchangerUser> getUserByPasswordResetToken(final String token);
}
