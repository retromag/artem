package com.example.cryptocurrencyexchanger;

import com.webcerebrium.binance.api.BinanceApi;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CryptocurrencyExchangerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptocurrencyExchangerApplication.class, args);
    }

    @Bean
    public BinanceApi binanceApi() {
        return new BinanceApi();
    }
}
