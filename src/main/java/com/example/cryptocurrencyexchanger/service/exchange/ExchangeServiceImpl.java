package com.example.cryptocurrencyexchanger.service.exchange;

import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.UserRole;
import com.example.cryptocurrencyexchanger.repo.ExchangeRepository;
import com.example.cryptocurrencyexchanger.repo.UserRoleRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangeServiceImpl implements ExchangeService {

    ExchangeRepository exchangeRepository;
    UserRoleRepository userRoleRepository;

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
        order.setStatus("Order canceled");
        exchangeRepository.save(order);
    }

    @Override
    public void deleteExchange(ExchangeOrder order) {
        exchangeRepository.delete(order);
    }

    @Override
    public ExchangeOrder findOrderById(Long id) {
        return exchangeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid exchange Id:" + id));
    }

    @Override
    public ExchangeOrder findOrderByCode(String code) {
        return exchangeRepository.findByUniqCode(code);
    }

    @Override
    public List<ExchangeOrder> getAllExchangeOrders(ExchangerUser user) {
        UserRole role = userRoleRepository.findByName("ROLE_ADMIN");

        if (user.getRoles().contains(role)) {
            return exchangeRepository.findByOrderByCreatedTimeDesc();
        } else {
            return exchangeRepository.getAllByUserOrderByCreatedTimeDesc(user);
        }
    }

    @Override
    public List<ExchangeOrder> getAllExchangeOrdersByStatus(String status) {
        return exchangeRepository.getAllByStatus(status);
    }
}
