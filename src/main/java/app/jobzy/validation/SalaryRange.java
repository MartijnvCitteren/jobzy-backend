package app.jobzy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SalaryRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SalaryRange {

  String message() default "Invalid salary range, maximum salary should be higher than minimum salary";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
