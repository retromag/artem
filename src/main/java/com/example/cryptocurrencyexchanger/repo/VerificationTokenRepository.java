package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
}
