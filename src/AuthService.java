import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class AuthService {
    private static User loggedInUser = null;
    private static int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    public static boolean signup(String username, String password, String firstName, String lastName, String role) {
        if (!PasswordValidator.isValid(password)) {
            System.out.println("Password must be 8+ chars, with upper, lower, digit, and special char.");
            return false;
        }
        try {
            if (UserDAO.exists(username)) {
                System.out.println("Username already taken.");
                return false;
            }
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            return UserDAO.insert(username, hashed, firstName, lastName, role);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }   

    public static boolean login(String username, String password) {
        if (loginAttempts >= MAX_ATTEMPTS) {
            System.out.println("Too many failed attempts. Restart the program.");
            return false;
        }
        try {
            User user = UserDAO.findByUsername(username);
            if (user == null) {
                System.out.println("User not found.");
                loginAttempts++;
                return false;
            }
            if (BCrypt.checkpw(password, user.getHashedPassword())) {
                loggedInUser = user;
                loginAttempts = 0;
                System.out.println("Welcome, " + user.getFirstName() + " (" + user.getRole() + ")!");
                return true;
            } else {
                loginAttempts++;
                System.out.println("Wrong password. Attempts left: " + (MAX_ATTEMPTS - loginAttempts));
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getLoggedInUser() { return loggedInUser; }
    public static void logout() { loggedInUser = null; loginAttempts = 0; }
}