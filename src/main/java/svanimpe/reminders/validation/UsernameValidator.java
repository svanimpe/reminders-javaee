package svanimpe.reminders.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String>
{
    @Override
    public void initialize(ValidUsername constraintAnnotation)
    {
    }

    /*
     * A valid username should contain between 8 and 255 alphabetical characters only. Leading or
     * trailing whitespace is not allowed. The User class should have already removed this
     * whitespace when setting the username.
     */
    @Override
    public boolean isValid(String username, ConstraintValidatorContext context)
    {
        return username != null && username.matches("[a-zA-Z]{8,255}+");
    }
}
