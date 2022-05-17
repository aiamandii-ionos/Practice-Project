package org.acme.validator;

import java.lang.annotation.*;

import javax.validation.*;

import static java.lang.annotation.ElementType.FIELD;

@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RamValidator.class)
@Documented
public @interface RamConstraint {
    String message() default "ram value must be a multiple of 1024 MB";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
