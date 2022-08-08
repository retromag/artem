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
    public BigDecimal getResultPrice(BigDecimal amount, String firstSymbol, String secondSymbol) {
        BigDecimal firstCoinInUSDT = getCoinPriceInUSDT(firstSymbol);
        BigDecimal secondCoinInUSDT = getCoinPriceInUSDT(secondSymbol);

        BigDecimal priceWithAmount = firstCoinInUSDT.multiply(amount);
        BigDecimal amountOfSecondCoin = priceWithAmount.divide(secondCoinInUSDT, 5, RoundingMode.HALF_UP);
        BigDecimal marginOfSecondCoin = getCoinMargin(secondSymbol);
        BigDecimal resultMargin = amountOfSecondCoin.multiply(marginOfSecondCoin).divide(new BigDecimal(100) , 5, RoundingMode.HALF_UP);

        return amountOfSecondCoin.subtract(resultMargin);
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
