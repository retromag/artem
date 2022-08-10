package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;

import java.math.BigDecimal;
import java.util.Set;

public interface CoinService {

    Coin findCoinById(Long id);

    Coin addNewCoin(Coin coin);

    Coin updateCoin(Coin coin);

    void deleteCoin(Long id);

    Set<Coin> getAllCoins();

    Coin getCoinByCoinSymbol(String symbol);

    BigDecimal getMinAllowedAmount(String symbol);

    BigDecimal getMaxAllowedAmount(String symbol);

    String getCoinWallet(String symbol);
}
