package com.example.cryptocurrencyexchanger.service.exchange;

import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;

import java.util.List;

public interface ExchangeService {
    void makeAnExchange(ExchangeOrder note);

    void payForExchange(ExchangeOrder note);

    void completeExchange(ExchangeOrder note);

    void cancelExchange(ExchangeOrder note);

    List<ExchangeOrder> getAllExchangeOrders(ExchangerUser user);

    List<ExchangeOrder> getAllExchangeOrdersByStatus(String status);

}
