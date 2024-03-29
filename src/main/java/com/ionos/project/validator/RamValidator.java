package com.ionos.project.validator;

import javax.validation.*;

public class RamValidator implements ConstraintValidator<RamConstraint, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && value % 1024 == 0;
    }
}
