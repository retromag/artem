package com.example.cryptocurrencyexchanger.service.binance;

import com.example.cryptocurrencyexchanger.service.coin.CoinService;
import com.webcerebrium.binance.api.BinanceApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BinanceRestService implements BinanceService {

    BinanceApi binanceApi;
    CoinService coinService;

    @Override
    public BigDecimal getResultPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals("USDT")) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (secondSymbol.equals("USDT")) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else {
            return getPriceFirstInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getResultPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals("USDT")) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (secondSymbol.equals("USDT")) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else {
            return getPriceSecondInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getPairPrice(String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals("USDT")) {
            BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);
            return new BigDecimal(1).divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
        } else if (secondSymbol.equals("USDT")) {
            return getCoinPriceInUSDT(firstSymbol);
        } else {
            BigDecimal firstCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
            BigDecimal secondCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

            return firstCoinInUSDT.divide(secondCoinInUSDT, 5, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getResultPriceIfFirstInputUSDT(BigDecimal amount, String symbol) {
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(symbol);
        BigDecimal amountOfTakenCoin = amount.divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
        BigDecimal marginOfTakenCoin = getCoinMargin(symbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal getResultPriceIfSecondInputUSDT(BigDecimal amount, String symbol) {
        BigDecimal coinPriceInUSDT = getCoinPriceInUSDT(symbol);
        BigDecimal amountOfTakenCoin = amount.multiply(coinPriceInUSDT);
        BigDecimal marginOfTakenCoin = getCoinMargin(symbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal getPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenCoins(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMargin(amountOfTakenCoin, secondSymbol);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal getPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenCoins(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMargin(amountOfTakenCoin, secondSymbol);

        return amountOfTakenCoin.add(resultMargin);
    }

    private BigDecimal calculateResultAmountTakenCoins(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal givenCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        BigDecimal priceWithAmount = givenCoinInUSDT.multiply(amount);

        return priceWithAmount.divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateResultMargin(BigDecimal amountOfTakenCoin, String secondSymbol) {
        BigDecimal marginOfTakenCoin = getCoinMargin(secondSymbol);
        return amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);
    }

    private BigDecimal getCoinMargin(String symbol) {
        return coinService.getCoinByCoinSymbol(symbol).getMargin();
    }

    @SneakyThrows
    private BigDecimal getCoinPriceInUSDT(String symbol) {
        return binanceApi.pricesMap().get(symbol + "USDT");
    }
}
