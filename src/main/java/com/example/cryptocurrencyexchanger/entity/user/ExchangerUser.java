package com.example.cryptocurrencyexchanger.entity.user;

import com.example.cryptocurrencyexchanger.entity.exchange.ExchangeOrder;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"), name = "exchanger_user")
public class ExchangerUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Email
    @NotEmpty
    @Column(name = "email")
    private String email;

    @NotEmpty
    @Column(name = "password")
    private String password;

    @NotEmpty
    @Column(name = "сoupon")
    private String coupon;

    @Column(name = "walletAmount")
    private BigDecimal walletAmount;

    @Column(name = "userMargin", precision = 8, scale = 2)
    private BigDecimal userMargin;

    @CreationTimestamp
    @Column(name = "create_time")
    private Timestamp registrationDate;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "nonLocked")
    private boolean nonLocked;
    @Column(name = "emailSubscribed")
    private boolean emailSubscribed;

    @Column(name = "all_privileges")
    private boolean allPrivileges;

    @EqualsAndHashCode.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ExchangeOrder> exchangeOrders = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<UserRole> roles = new HashSet<>();
}
