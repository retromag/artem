package com.example.cryptocurrencyexchanger.service.binance;

import java.math.BigDecimal;

public interface BinanceService {
    BigDecimal getResultPrice(BigDecimal amount, String firstSymbol, String secondSymbol);

    BigDecimal getPairPrice(String firstSymbol, String secondSymbol);
}
