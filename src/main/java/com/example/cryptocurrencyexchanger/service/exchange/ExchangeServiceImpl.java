package com.example.cryptocurrencyexchanger.service.exchange;

import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.repo.ExchangeRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangeServiceImpl implements ExchangeService {

    ExchangeRepository exchangeRepository;

    @Override
    public void makeAnExchange(ExchangeOrder order) {
        order.setStatus("Preliminary application");
        exchangeRepository.save(order);
    }

    @Override
    public void payForExchange(ExchangeOrder order) {
        order.setStatus("Wait confirmation");
        exchangeRepository.save(order);
    }

    @Override
    public void completeExchange(ExchangeOrder order) {
        order.setStatus("Complete");
        exchangeRepository.save(order);
    }

    @Override
    public void cancelExchange(ExchangeOrder order) {
        order.setStatus("Order deleted");
        exchangeRepository.save(order);
    }

    @Override
    public void deleteExchange(ExchangeOrder order) {
        exchangeRepository.delete(order);
    }

    @Override
    public ExchangeOrder findOrderByCode(String code) {
        return null;
    }

    @Override
    public List<ExchangeOrder> getAllExchangeOrders(ExchangerUser user) {
        if (user.isAllPrivileges()) {
            return exchangeRepository.findAll();
        } else {
            return exchangeRepository.getAllByUser(user);
        }
    }

    @Override
    public List<ExchangeOrder> getAllExchangeOrdersByStatus(String status) {
        return exchangeRepository.getAllByStatus(status);
    }
}
