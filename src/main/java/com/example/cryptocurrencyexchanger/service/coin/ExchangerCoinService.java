package com.example.cryptocurrencyexchanger.service.coin;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.repo.CoinRepository;
import com.google.common.collect.Sets;
import java.math.RoundingMode;
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
        Set<Coin> coinList = Sets.newHashSet(coinRepository.findAll());

        for (Coin coin: coinList) {
            if (coin.getSymbol().startsWith("UAH") || coin.getSymbol().startsWith("RUB") || coin.getSymbol().startsWith("USD")) {
                coin.setSymbol(coin.getSymbol().substring(0, 3));
                coin.setAmount(coin.getAmount().setScale(2, RoundingMode.HALF_DOWN));
            }
            if (coin.getSymbol().startsWith("USD") && coin.getSymbol().equals("USDT") ) {
                coin.setSymbol(coin.getSymbol().substring(0, 3));
                coin.setAmount(coin.getAmount().setScale(2, RoundingMode.HALF_DOWN));
            }
        }

        return coinList;
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
