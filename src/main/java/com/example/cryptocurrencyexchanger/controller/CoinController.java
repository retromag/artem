package com.example.cryptocurrencyexchanger.controller;

import com.example.cryptocurrencyexchanger.entity.coin.Coin;
import com.example.cryptocurrencyexchanger.service.binance.BinanceService;
import com.example.cryptocurrencyexchanger.service.coin.CoinService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CoinController {

    CoinService coinService;
    BinanceService binanceService;

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

    @GetMapping("/coin/min/amount")
    public ResponseEntity<BigDecimal> getMinAllowedCoinAmount(@RequestParam("symbol") String symbol) {
        BigDecimal minAmount = coinService.getMinAllowedAmount(symbol);
        if (minAmount == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(minAmount);
        }
    }

    @GetMapping("/coin/max/amount")
    public ResponseEntity<BigDecimal> getMaxAllowedCoinAmount(@RequestParam("symbol") String symbol) {
        BigDecimal minAmount = coinService.getMaxAllowedAmount(symbol);
        if (minAmount == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(minAmount);
        }
    }

    @GetMapping("/coin/wallet")
    public ResponseEntity<String> getCoinWallet(@RequestParam("symbol") String symbol) {
        String wallet = coinService.getCoinWallet(symbol);
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(wallet);
        }
    }

    @GetMapping("/app/get/taken")
    public ResponseEntity<BigDecimal> getCalculatedAmountOfTakenCoins(@RequestParam("amount") String amount,
                                                                 @RequestParam("firstSymbol") String firstSymbol,
                                                                 @RequestParam("secondSymbol") String secondSymbol) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return ResponseEntity.ok(binanceService.getResultPriceFirstInput(bigDecimalAmount, firstSymbol, secondSymbol));
    }

    @GetMapping("/app/get/given")
    public ResponseEntity<BigDecimal> getCalculatedAmountOfGivenCoins(@RequestParam("amount") String amount,
                                                                 @RequestParam("firstSymbol") String firstSymbol,
                                                                 @RequestParam("secondSymbol") String secondSymbol) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return ResponseEntity.ok(binanceService.getResultPriceFirstInput(bigDecimalAmount, firstSymbol, secondSymbol));
    }

    @GetMapping("/app/get/price")
    public ResponseEntity<BigDecimal> getPriceOfCoinPair(@RequestParam("firstSymbol") String firstSymbol,
                                                                 @RequestParam("secondSymbol") String secondSymbol) {
        return ResponseEntity.ok(binanceService.getPairPrice(firstSymbol, secondSymbol));
    }
}
