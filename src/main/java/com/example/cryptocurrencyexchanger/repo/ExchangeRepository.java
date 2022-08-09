package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.user.ExchangeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRepository extends JpaRepository<ExchangeNote, Long> {
}
