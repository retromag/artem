package com.example.cryptocurrencyexchanger.service.user;

import com.example.cryptocurrencyexchanger.config.PasswordEncoder;
import com.example.cryptocurrencyexchanger.entity.user.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.user.UserModel;
import com.example.cryptocurrencyexchanger.entity.user.UserRole;
import com.example.cryptocurrencyexchanger.repo.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangerUserService implements UserService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ExchangerUser user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                user.isNonLocked(),
                mapRolesToAuthorities(user.getRoles()));
    }


    @Override
    public ExchangerUser findByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public ExchangerUser saveNewUser(final UserModel userModel) {
        ExchangerUser user = createUser(userModel);
        return userRepository.save(user);
    }

    @Override
    public void activateUser(final ExchangerUser user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void changeUserPassword(final ExchangerUser user, final String password) {
        user.setPassword(PasswordEncoder.passwordEncoder().encode(password));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(final ExchangerUser user, final String oldPassword) {
        return PasswordEncoder.passwordEncoder().matches(oldPassword, user.getPassword());
    }

    @Override
    public boolean checkConfirmPassword(UserModel userModel) {
        return userModel.getPassword().equals(userModel.getConfirmPassword());
    }

    @Override
    public List<ExchangerUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void changeUserWalletAmount(ExchangerUser user, BigDecimal amount) {
        user.setWalletAmount(amount);

        userRepository.save(user);
    }

    @Override
    public void changeUserMargin(ExchangerUser user, BigDecimal precision) {
        user.setUserMargin(precision);

        userRepository.save(user);
    }

    @Override
    public void lockUser(ExchangerUser user) {
        user.setNonLocked(false);
        userRepository.save(user);
    }

    @Override
    public void unLockUser(ExchangerUser user) {
        user.setNonLocked(true);
        userRepository.save(user);
    }

    private ExchangerUser createUser(final UserModel userModel) {
        ExchangerUser user = new ExchangerUser();
        user.setEmail(userModel.getEmail());
        user.setPassword(PasswordEncoder.passwordEncoder().encode(userModel.getPassword()));
        user.setCoupon(userModel.getCoupon());
        user.setRoles(Collections.singletonList(new UserRole("ROLE_USER")));
        user.setAllPrivileges(false);
        user.setNonLocked(true);
        user.setWalletAmount(new BigDecimal(0));
        user.setUserMargin(new BigDecimal(0));

        return user;
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<UserRole> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
