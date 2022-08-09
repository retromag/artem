package com.example.cryptocurrencyexchanger.entity.user;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "exchanger_note")
public class ExchangeNote {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private ExchangerUser user;

    private String givenCoin;

    private String takenCoin;

    private BigDecimal givenAmount;

    private BigDecimal takenAmount;

    private String wallet;

    @CreationTimestamp
    private Timestamp createdTime;
}
