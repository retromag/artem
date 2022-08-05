package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.repo.CoinRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangerCoinService implements CoinService {

    CoinRepository coinRepository;

    @Override
    public List<Coin> getAllCoins() {
        return coinRepository.findAll();
    }
}
