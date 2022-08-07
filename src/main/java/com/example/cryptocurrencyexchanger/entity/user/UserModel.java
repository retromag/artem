package com.example.cryptocurrencyexchanger.entity.user;

import com.example.cryptocurrencyexchanger.util.annotation.FieldMatch;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
        @FieldMatch(first = "email", second = "confirmEmail", message = "The email fields must match")})
public class UserModel {

    @NonNull
    @NotBlank(message = "New password is mandatory")
    private String password;

    @NotEmpty
    @NotBlank(message = "Confirm password is mandatory")
    private String confirmPassword;

    @Email
    @NotEmpty
    private String email;

    private boolean allPrivileges;
}

