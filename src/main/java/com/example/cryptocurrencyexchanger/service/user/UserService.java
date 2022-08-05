package com.example.cryptocurrencyexchanger.service.user;

import com.example.cryptocurrencyexchanger.entity.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.UserModel;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    ExchangerUser findByEmail(String email);

    ExchangerUser saveNewUser(UserModel userModel);

    void activateUser(ExchangerUser user);
}
