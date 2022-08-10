package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.repo.CoinRepository;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangerCoinService implements CoinService {

    private final CoinRepository coinRepository;

    @Override
    public Coin findCoinById(Long id) {
        return coinRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid coin Id:" + id));
    }

    @Override
    public Coin addNewCoin(Coin coin) {
        return coinRepository.save(coin);
    }

    @Override
    public Coin updateCoin(Coin coin) {
        return coinRepository.save(coin);
    }

    @Override
    public void deleteCoin(Long id) {
        Coin coin = findCoinById(id);
        coinRepository.delete(coin);
    }

    @Override
    public Set<Coin> getAllCoins() {
        return Sets.newHashSet(coinRepository.findAll());
    }

    @Override
    public Coin getCoinByCoinSymbol(String symbol) {
        return coinRepository.getCoinBySymbol(symbol);
    }

    @Override
    public BigDecimal getMinAllowedAmount(String symbol) {
        return coinRepository.getCoinBySymbol(symbol).getMinAmount();
    }

    @Override
    public BigDecimal getMaxAllowedAmount(String symbol) {
        return coinRepository.getCoinBySymbol(symbol).getAmount();
    }

    @Override
    public String getCoinWallet(String symbol) {
        return coinRepository.getCoinBySymbol(symbol).getWallet();
    }
}
