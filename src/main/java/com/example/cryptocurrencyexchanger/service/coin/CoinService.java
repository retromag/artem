package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;

import java.util.List;

public interface CoinService {

    Coin addNewCoin(Coin coin);

    Coin updateCoin(Coin coin);

    List<Coin> getAllCoins();
}
