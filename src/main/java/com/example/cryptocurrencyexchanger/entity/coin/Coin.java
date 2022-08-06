package com.example.cryptocurrencyexchanger.entity.coin;

import lombok.*;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String wallet;

    private String image;

    private String amount;
}
