package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<ExchangeOrder, Long> {
    List<ExchangeOrder> getAllByStatus(String status);

    List<ExchangeOrder> findByOrderByCreatedTimeDesc();

    List<ExchangeOrder> getAllByUserOrderByCreatedTimeDesc(ExchangerUser user);

    ExchangeOrder findByUniqCode(String code);
}
