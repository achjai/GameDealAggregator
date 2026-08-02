import java.util.regex.Pattern;

public class PasswordValidator {
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public static boolean isValid(String password) {
        return PATTERN.matcher(password).matches();
    }
}