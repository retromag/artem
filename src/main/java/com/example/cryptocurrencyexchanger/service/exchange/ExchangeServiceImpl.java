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
    public void makeAnExchange(ExchangeOrder note) {
        note.setStatus("Preliminary application");
        exchangeRepository.save(note);
    }

    @Override
    public void payForExchange(ExchangeOrder note) {
        note.setStatus("Wait confirmation");
        exchangeRepository.save(note);
    }

    @Override
    public void completeExchange(ExchangeOrder note) {
        note.setStatus("Complete");
        exchangeRepository.save(note);
    }

    @Override
    public void cancelExchange(ExchangeOrder note) {
        note.setStatus("Order deleted");
        exchangeRepository.save(note);
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
