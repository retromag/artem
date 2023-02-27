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

    private static final String USDT_SYMBOL = "USDT";

    BinanceApi binanceApi;
    CoinService coinService;

    @Override
    public BigDecimal getResultPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals(USDT_SYMBOL) || firstSymbol.startsWith("USD")) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (secondSymbol.equals(USDT_SYMBOL) || secondSymbol.startsWith("USD")) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else if (isFiatCoin(firstSymbol, secondSymbol)) {
            return getPriceFirstInputFiat(amount, firstSymbol, secondSymbol);
        } else {
            return getPriceFirstInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getResultPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals(USDT_SYMBOL) || firstSymbol.startsWith("USD")) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (secondSymbol.equals(USDT_SYMBOL) || secondSymbol.startsWith("USD")) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else if (isFiatCoin(firstSymbol, secondSymbol)) {
            return getPriceSecondInputFiat(amount, firstSymbol, secondSymbol);
        } else {
            return getPriceSecondInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getPairPrice(String firstSymbol, String secondSymbol) {
        if (firstSymbol.equals(USDT_SYMBOL) || firstSymbol.startsWith("USD")) {
            BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);
            return new BigDecimal(1).divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
        } else if (secondSymbol.equals(USDT_SYMBOL) || secondSymbol.startsWith("USD")) {
            return getCoinPriceInUSDT(firstSymbol);
        } else if (isFiatCoin(firstSymbol, firstSymbol)) {
            return getPriceWhenFirstInputFiat(firstSymbol, secondSymbol);
        } else {
            BigDecimal firstCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
            BigDecimal secondCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

            if (isFiatCoin(secondSymbol, secondSymbol)) {
                return firstCoinInUSDT.multiply(secondCoinInUSDT).setScale(2, RoundingMode.HALF_DOWN);
            }

            return firstCoinInUSDT.divide(secondCoinInUSDT, 5, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getResultPriceIfFirstInputUSDT(BigDecimal amount, String symbol) {
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(symbol);
        BigDecimal amountOfTakenCoin = amount.divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
        BigDecimal marginOfTakenCoin = getCoinMargin(symbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);

        return amountOfTakenCoin.add(resultMargin);
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

        return amountOfTakenCoin.add(resultMargin);
    }

    private BigDecimal getPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenCoins(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMargin(amountOfTakenCoin, secondSymbol);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal getPriceFirstInputFiat(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenFiatInput(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMarginFiat(amountOfTakenCoin, secondSymbol);

        return amountOfTakenCoin.add(resultMargin);
    }

    private BigDecimal getPriceSecondInputFiat(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenFiatInput(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMarginFiat(amountOfTakenCoin, secondSymbol);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal calculateResultAmountTakenCoins(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal givenCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        BigDecimal priceWithAmount = givenCoinInUSDT.multiply(amount);

        return priceWithAmount.divide(takenCoinInUSDT, 7, RoundingMode.HALF_DOWN);
    }

    private BigDecimal calculateResultAmountTakenFiatInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal givenCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        if (isFiatCoin(firstSymbol, firstSymbol)) {
            return amount.divide(givenCoinInUSDT, 7, RoundingMode.HALF_DOWN).multiply(new BigDecimal(1).divide(takenCoinInUSDT, 7, RoundingMode.HALF_DOWN));
        }

        return givenCoinInUSDT.multiply(takenCoinInUSDT).multiply(amount);
    }

    private BigDecimal calculateResultMargin(BigDecimal amountOfTakenCoin, String secondSymbol) {
        BigDecimal marginOfTakenCoin = getCoinMargin(secondSymbol);
        return amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateResultMarginFiat(BigDecimal amountOfTakenCoin, String secondSymbol) {
        BigDecimal marginOfTakenCoin = getCoinMargin(secondSymbol);
        return amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN);
    }

    private BigDecimal getCoinMargin(String symbol) {
        return coinService.getCoinByCoinSymbol(symbol).getMargin();
    }

    private BigDecimal getPriceWhenFirstInputFiat(String firstSymbol, String secondSymbol) {
        BigDecimal firstSymbolUSD = getCoinPriceInUSDT(firstSymbol);
        BigDecimal secondSymbolInUSD = getCoinPriceInUSDT(secondSymbol);

        return new BigDecimal(1).divide(firstSymbolUSD, 7, RoundingMode.HALF_DOWN).multiply(new BigDecimal(1).divide(secondSymbolInUSD, 7, RoundingMode.HALF_DOWN));
    }

    private boolean isFiatCoin(String firstSymbol, String secondSymbol) {
        return firstSymbol.startsWith("UAH") || firstSymbol.startsWith("RUB")
            || secondSymbol.startsWith("UAH") || secondSymbol.startsWith("RUB") ;
    }

    @SneakyThrows
    private BigDecimal getCoinPriceInUSDT(String symbol) {
        if(symbol.startsWith("UAH") || symbol.startsWith("RUB")) {
            return binanceApi.pricesMap().get(USDT_SYMBOL + symbol.substring(0, 3));
        }

        return binanceApi.pricesMap().get(symbol + USDT_SYMBOL);
    }
}
