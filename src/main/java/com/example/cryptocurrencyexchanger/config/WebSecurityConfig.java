package com.example.cryptocurrencyexchanger.config;

import com.example.cryptocurrencyexchanger.service.user.ExchangerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ExchangerUserService userDetailsService;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/registration").not().fullyAuthenticated()
                .antMatchers()
                .hasRole("ADMIN")
                .antMatchers().hasAnyRole("USER", "ADMIN")
                .antMatchers("/",
                        "/registration/confirm/**",
                        "/user/reset/password/**",
                        "/forgot/password",
                        "/reset/password",
                        "/update/password",
                        "/reserves",
                        "/rules",
                        "/aml",
                        "/exchange/create",
                        "/review/all",
                        "/exchange/pay",
                        "/review/new",
                        "/contacts",
                        "/about-us",
                        "/order/delete/*",
                        "/api/**",
                        "/js/**",
                        "/css/**",
                        "/images/**",
                        "/styles/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                    .defaultSuccessUrl("/")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .logoutSuccessUrl("/");
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(PasswordEncoder.passwordEncoder());
        return auth;
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(PasswordEncoder.passwordEncoder());
    }
}
