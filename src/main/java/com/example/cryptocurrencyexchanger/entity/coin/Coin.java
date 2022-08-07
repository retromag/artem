package com.example.cryptocurrencyexchanger.entity.coin;

import lombok.*;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "coin")
public class Coin {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String wallet;

    @Column(precision = 8, scale = 4)
    private BigDecimal margin;

    private String symbol;

    private BigDecimal amount;

    private String image;
}
