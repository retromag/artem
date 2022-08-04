package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.ExchangerUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<ExchangerUser, Long> {
    ExchangerUser findByEmail(String email);
}
