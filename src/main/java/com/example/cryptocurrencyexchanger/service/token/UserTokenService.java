package com.example.cryptocurrencyexchanger.service.token;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.PasswordResetToken;
import com.example.cryptocurrencyexchanger.entity.user.VerificationToken;
import com.example.cryptocurrencyexchanger.repo.PasswordResetTokenRepository;
import com.example.cryptocurrencyexchanger.repo.VerificationTokenRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserTokenService implements TokenService {

    VerificationTokenRepository verificationTokenRepository;
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public ExchangerUser getUserByVerificationToken(String verificationToken) {
        final VerificationToken token = verificationTokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getUser();
        }
        return null;
    }

    @Override
    public void createPasswordResetTokenForUser(ExchangerUser user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
    }

    @Override
    public Optional<ExchangerUser> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }
    @Override
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void createVerificationTokenForUser(ExchangerUser user, String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }
}
