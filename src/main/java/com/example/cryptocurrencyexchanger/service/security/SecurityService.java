package com.example.cryptocurrencyexchanger.service.security;

public interface SecurityService {
    String validatePasswordResetToken(String token);
}
