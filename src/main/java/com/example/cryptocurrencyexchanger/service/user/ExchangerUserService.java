package com.example.cryptocurrencyexchanger.service.user;

import com.example.cryptocurrencyexchanger.config.PasswordEncoder;
import com.example.cryptocurrencyexchanger.entity.ExchangerUser;
import com.example.cryptocurrencyexchanger.entity.UserModel;
import com.example.cryptocurrencyexchanger.entity.UserRole;
import com.example.cryptocurrencyexchanger.repo.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
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
                true,
                mapRolesToAuthorities(user.getRoles()));
    }
    @Override
    public ExchangerUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public ExchangerUser saveNewUser(UserModel userModel) {
        ExchangerUser user = createUser(userModel);
        return userRepository.save(user);
    }

    @Override
    public void activateUser(ExchangerUser user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    private ExchangerUser createUser(UserModel userModel) {
        ExchangerUser user = new ExchangerUser();
        user.setEmail(userModel.getEmail());
        user.setPassword(PasswordEncoder.passwordEncoder().encode(userModel.getPassword()));
        user.setRoles(Collections.singletonList(new UserRole("ROLE_USER")));
        user.setAllPrivileges(false);

        return user;
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<UserRole> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
