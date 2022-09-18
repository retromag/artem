package com.example.cryptocurrencyexchanger.entity.user;

import com.example.cryptocurrencyexchanger.util.annotation.FieldMatch;
import com.example.cryptocurrencyexchanger.util.annotation.PasswordValueMatch;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@PasswordValueMatch.List({
        @PasswordValueMatch(
                field = "password",
                fieldMatch = "confirmPassword",
                message = "Passwords do not match!"
        )
})
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

    private String coupon;

    private boolean allPrivileges;
}

