package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.service.coin.CoinService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CoinController {

    CoinService coinService;

    @GetMapping("/coin/{symbol}")
    public ResponseEntity<Coin> getCoin(@PathVariable("symbol") String symbol) {
        Coin coin = coinService.getCoinByCoinSymbol(symbol);
        if (coin == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(coin);
        }
    }

    @GetMapping("/coin/{symbol}/margin")
    public ResponseEntity<BigDecimal> getCoinMargin(@PathVariable("symbol") String symbol) {
        Coin coin = coinService.getCoinByCoinSymbol(symbol);
        if (coin == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(coin.getMargin());
        }
    }
}
