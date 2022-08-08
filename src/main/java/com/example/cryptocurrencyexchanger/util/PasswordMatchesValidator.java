package com.example.cryptocurrencyexchanger.util;

import com.example.cryptocurrencyexchanger.entity.user.UserModel;
import com.example.cryptocurrencyexchanger.util.annotation.PasswordMatches;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String message;

    @Override
    public void initialize(PasswordMatches passwordMatches) {
        this.message = passwordMatches.message();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {

        final UserModel userModel = (UserModel) obj;
        boolean isValid = userModel.getPassword().equals(userModel.getConfirmPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate( message )
                    .addPropertyNode( "matchingPassword" ).addConstraintViolation();
        }

        return isValid;

    }
}
