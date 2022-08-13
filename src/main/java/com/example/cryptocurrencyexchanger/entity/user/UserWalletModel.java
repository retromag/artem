package com.example.cryptocurrencyexchanger.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletModel {
    private BigDecimal walletAmount;

    private BigDecimal userMargin;
}
