package org.acme.validator;

import javax.validation.*;

public class RamValidator implements ConstraintValidator<RamConstraint, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value % 1024 == 0;
    }
}
