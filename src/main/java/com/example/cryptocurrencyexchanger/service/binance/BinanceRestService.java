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
        BigDecimal givenCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        BigDecimal priceWithAmount = givenCoinInUSDT.multiply(amount);
        BigDecimal amountOfTakenCoin = priceWithAmount.divide(takenCoinInUSDT, 5, RoundingMode.HALF_UP);
        BigDecimal marginOfTakenCoin = getCoinMargin(secondSymbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100) , 5, RoundingMode.HALF_UP);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    // TODO: fix logic of calculating magrin if user enter how much he want receive
    @Override
    public BigDecimal getResultPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal priceBetweenPair = getPairPrice(firstSymbol, secondSymbol);

        BigDecimal resultWithoutMargin = amount.divide(priceBetweenPair, 5, RoundingMode.HALF_UP);
        BigDecimal marginOfTakenCoin = getCoinMargin(secondSymbol);
        BigDecimal resultMargin = resultWithoutMargin.multiply(marginOfTakenCoin).divide(new BigDecimal(100) , 5, RoundingMode.HALF_UP);

        return resultWithoutMargin.add(resultMargin);
    }

    @Override
    public BigDecimal getPairPrice(String firstSymbol, String secondSymbol) {
        BigDecimal firstCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal secondCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        return firstCoinInUSDT.divide(secondCoinInUSDT, 5, RoundingMode.HALF_UP);
    }

    private BigDecimal getCoinMargin(String symbol) {
        return coinService.getCoinByCoinSymbol(symbol).getMargin();
    }

    @SneakyThrows
    private BigDecimal getCoinPriceInUSDT(String symbol) {
        return binanceApi.pricesMap().get(symbol+"USDT");
    }
}
