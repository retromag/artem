package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;

import java.util.List;
import java.util.Set;

public interface CoinService {

    Coin findCoinById(Long id);

    Coin addNewCoin(Coin coin);

    Coin updateCoin(Coin coin);

    Set<Coin> getAllCoins();
}
