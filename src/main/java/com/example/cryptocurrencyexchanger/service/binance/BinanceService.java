package com.example.cryptocurrencyexchanger.service.binance;

import java.math.BigDecimal;

public interface BinanceService {
    BigDecimal getResultPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol);

    BigDecimal getResultPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol);

    BigDecimal getPairPrice(String firstSymbol, String secondSymbol);
}
