package com.example.cryptocurrencyexchanger.service.user;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.UserModel;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    ExchangerUser findByEmail(final String email);

    ExchangerUser saveNewUser(final UserModel userModel);

    void activateUser(final ExchangerUser user);

    void changeUserPassword(final ExchangerUser user, final String password);

    boolean checkIfValidOldPassword(ExchangerUser user, String oldPassword);
}
