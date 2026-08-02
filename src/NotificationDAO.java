import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // ==================== ADD ====================
    public static void add(int userId, String message) throws SQLException {
        String sql = "{ call notification_add(?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.execute();
        }
    }

    // ==================== GET LATEST ====================
    public static List<Notification> getLatest(int userId, int limit) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setNotifId(rs.getInt("notif_id"));
                n.setUserId(rs.getInt("user_id"));
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        }
        return list;
    }

    // ==================== MARK AS READ ====================
    public static void markAsRead(int notifId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notif_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notifId);
            stmt.executeUpdate();
        }
    }
}