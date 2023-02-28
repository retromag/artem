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
    private static final String USD_SYMBOL = "USD";
    private static final String UAH_SYMBOL = "UAH";
    private static final String EUR_SYMBOL = "EUR";
    private static final String PLN_SYMBOL = "PLN";
    private static final String RUB_SYMBOL = "RUB";

    BinanceApi binanceApi;
    CoinService coinService;

    @Override
    public BigDecimal getResultPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (isFirstSymbolUSD(firstSymbol) && !isFirstSymbolUSD(secondSymbol)) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (isSecondSymbolUSD(secondSymbol) && !isFirstSymbolUSD(firstSymbol)) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else if (isFiatCoin(firstSymbol)) {
            return getPriceFirstInputFiat(amount, firstSymbol, secondSymbol);
        } else if (isBothSymbolsUSD(firstSymbol, secondSymbol)) {
            return new BigDecimal(1).multiply(amount).setScale(2, RoundingMode.HALF_UP);
        } else {
            return getPriceFirstInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getResultPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        if (isFirstSymbolUSD(secondSymbol) && !isSecondSymbolUSD(secondSymbol)) {
            return getResultPriceIfFirstInputUSDT(amount, secondSymbol);
        } else if (isSecondSymbolUSD(secondSymbol) && !isFirstSymbolUSD(firstSymbol)) {
            return getResultPriceIfSecondInputUSDT(amount, firstSymbol);
        } else if (isFiatCoin(secondSymbol)) {
            return getPriceSecondInputFiat(amount, firstSymbol, secondSymbol);
        } else if (isBothSymbolsUSD(firstSymbol, secondSymbol)) {
            return new BigDecimal(1).multiply(amount).setScale(2, RoundingMode.HALF_UP);
        } else {
            return getPriceSecondInput(amount, firstSymbol, secondSymbol);
        }
    }

    @Override
    public BigDecimal getPairPrice(String firstSymbol, String secondSymbol) {
        if (isFirstSymbolUSD(firstSymbol) && !isBothSymbolsUSD(firstSymbol, secondSymbol)) {
            return resultPriceFirstInputUSD(secondSymbol);
        } else if (isSecondSymbolUSD(secondSymbol) && !isBothSymbolsUSD(firstSymbol, secondSymbol)) {
            return getCoinPriceInUSDT(firstSymbol);
        } else if (isFiatCoin(firstSymbol) && isFiatCoin(secondSymbol)) {
            return getPriceWhenFirstInputFiat(firstSymbol, secondSymbol);
        } else if (isBothSymbolsUSD(firstSymbol, secondSymbol)) {
            return new BigDecimal(1).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal firstCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
            BigDecimal secondCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

            if (isFiatCoin(secondSymbol)) {
                return firstCoinInUSDT.multiply(secondCoinInUSDT).setScale(2, RoundingMode.HALF_DOWN);
            }

            return firstCoinInUSDT.divide(secondCoinInUSDT, 5, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getResultPriceIfFirstInputUSDT(BigDecimal amount, String symbol) {
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(symbol);

        if (isFiatCoin(symbol)) {
            return amount.multiply(takenCoinInUSDT);
        }

        BigDecimal amountOfTakenCoin = amount.divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
        BigDecimal marginOfTakenCoin = getCoinMargin(symbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);

        return amountOfTakenCoin.add(resultMargin);
    }

    private BigDecimal getResultPriceIfSecondInputUSDT(BigDecimal amount, String symbol) {
        BigDecimal coinPriceInUSDT = getCoinPriceInUSDT(symbol);

        if (isFiatCoin(symbol)) {
            return amount.divide(coinPriceInUSDT, 2, RoundingMode.HALF_UP);
        }

        BigDecimal amountOfTakenCoin = amount.multiply(coinPriceInUSDT);
        BigDecimal marginOfTakenCoin = getCoinMargin(symbol);
        BigDecimal resultMargin = amountOfTakenCoin.multiply(marginOfTakenCoin).divide(new BigDecimal(100), 7, RoundingMode.HALF_UP);

        return amountOfTakenCoin.subtract(resultMargin);
    }

    private BigDecimal getPriceFirstInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenCoins(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMargin(amountOfTakenCoin, secondSymbol);

        if (isFiatCoin(secondSymbol)) {
            return calculateResultAmountTakenFiatInput(amount, firstSymbol, secondSymbol);
        }

        return amountOfTakenCoin.add(resultMargin);
    }

    private BigDecimal getPriceSecondInput(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal amountOfTakenCoin = calculateResultAmountTakenCoins(amount, firstSymbol, secondSymbol);
        BigDecimal resultMargin = calculateResultMargin(amountOfTakenCoin, secondSymbol);

        if (isFiatCoin(firstSymbol)) {
            return calculateResultAmountTakenFiatInput(amount, firstSymbol, secondSymbol);
        }

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

        if (isFiatCoin(firstSymbol) || isFiatCoin(secondSymbol)) {
            return amount.divide(givenCoinInUSDT, 2, RoundingMode.HALF_DOWN)
                .multiply(new BigDecimal(1).divide(takenCoinInUSDT, 2, RoundingMode.HALF_DOWN));
        } else {
            return givenCoinInUSDT.multiply(takenCoinInUSDT).multiply(amount);
        }
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

        return new BigDecimal(1).divide(firstSymbolUSD, 7, RoundingMode.HALF_DOWN)
            .multiply(new BigDecimal(1).divide(secondSymbolInUSD, 7, RoundingMode.HALF_DOWN));
    }

    private BigDecimal resultPriceFirstInputUSD(String symbol) {
        BigDecimal takenCoinInUSDT = getCoinPriceInUSDT(symbol);
        if (isFiatCoin(symbol)) {
            return takenCoinInUSDT.setScale(2,  RoundingMode.HALF_UP);
        }
        return new BigDecimal(1).divide(takenCoinInUSDT, 7, RoundingMode.HALF_UP);
    }

    private boolean isFiatCoin(String symbol) {
        return symbol.startsWith(UAH_SYMBOL) || symbol.startsWith(RUB_SYMBOL)
            || symbol.startsWith(PLN_SYMBOL) || symbol.startsWith(EUR_SYMBOL);
    }

    private boolean isBothSymbolsUSD(String firstSymbol, String secondSymbol) {
        return (firstSymbol.equals(USDT_SYMBOL) || firstSymbol.startsWith(USD_SYMBOL))
            && (secondSymbol.equals(USDT_SYMBOL) || secondSymbol.startsWith(USD_SYMBOL));
    }

    private boolean isFirstSymbolUSD(String firstSymbol) {
        return firstSymbol.equals(USDT_SYMBOL) || firstSymbol.startsWith(USD_SYMBOL);
    }

    private boolean isSecondSymbolUSD(String secondSymbol) {
        return secondSymbol.equals(USDT_SYMBOL) || secondSymbol.startsWith(USD_SYMBOL);
    }

    @SneakyThrows
    private BigDecimal getCoinPriceInUSDT(String symbol) {
        if (symbol.startsWith(EUR_SYMBOL)) {
            return binanceApi.pricesMap().get(symbol.substring(0, 3) + USDT_SYMBOL);
        }

        if(isFiatCoin(symbol) ) {
            BigDecimal fiatPriceUSDT = binanceApi.pricesMap().get(USDT_SYMBOL + symbol.substring(0, 3));
            if (fiatPriceUSDT == null) {
                return binanceApi.pricesMap().get("BUSD" + symbol.substring(0, 3));
            }
            return binanceApi.pricesMap().get(USDT_SYMBOL + symbol.substring(0, 3));
        }

        return binanceApi.pricesMap().get(symbol + USDT_SYMBOL);
    }
}
