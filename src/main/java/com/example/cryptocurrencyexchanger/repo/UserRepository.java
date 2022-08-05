package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<ExchangerUser, Long> {
    ExchangerUser findByEmail(String email);
}
