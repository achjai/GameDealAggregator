import java.sql.*;

public class UserDAO {

    public static boolean exists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public static boolean insert(String username, String hashedPassword, String firstName, String lastName, String role) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password, first_name, last_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, role != null ? role : "USER");
            return stmt.executeUpdate() > 0;
        }
    }

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, hashed_password, first_name, last_name, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("hashed_password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role")
                );
            }
            return null;
        }
    }

    public static int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
            return -1;
        }
    }
}