package svanimpe.reminders.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static svanimpe.reminders.util.Utilities.*;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String>
{
    @Override
    public void initialize(ValidPassword constraintAnnotation)
    {
    }

    /*
     * A valid password should contain between 8 and 255 characters. Leading or trailing whitespace
     * is not allowed. The User class should have already removed this whitespace when setting the
     * plain text password.
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context)
    {
        return password != null && password.equals(cleanUp(password)) && password.length() >= 8 && password.length() <= 255;
    }
}
