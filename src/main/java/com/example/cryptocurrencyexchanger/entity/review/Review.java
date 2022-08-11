package com.example.cryptocurrencyexchanger.entity.review;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "review")
public class Review {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String massage;

    private int rate;
}
