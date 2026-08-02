import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    // ==================== ADD ====================
    public static boolean add(int userId, int gameId) throws SQLException {
        String sql = "{ ? = call wishlist_add(?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setInt(2, userId);
            stmt.setInt(3, gameId);
            stmt.execute();
            return stmt.getBoolean(1);
        }
    }

    // ==================== REMOVE ====================
    public static boolean remove(int userId, int gameId) throws SQLException {
        String sql = "{ ? = call wishlist_remove(?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setInt(2, userId);
            stmt.setInt(3, gameId);
            stmt.execute();
            return stmt.getBoolean(1);
        }
    }

    // ==================== GET WISHLIST ====================
    public static List<Game> getWishlist(int userId) throws SQLException {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT g.game_id, g.game_name, s.store_name, g.sale_price, g.normal_price " +
                "FROM wishlist w JOIN games g ON w.game_id = g.game_id " +
                "JOIN stores s ON g.store_id = s.store_id " +
                "WHERE w.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game g = new Game();
                g.setGameId(rs.getInt("game_id"));
                g.setGameName(rs.getString("game_name"));
                g.setStoreName(rs.getString("store_name"));
                g.setSalePrice(rs.getBigDecimal("sale_price"));
                g.setNormalPrice(rs.getBigDecimal("normal_price"));
                list.add(g);
            }
        }
        return list;
    }

    // ==================== EXISTS ====================
    public static boolean exists(int userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM wishlist WHERE user_id = ? AND game_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, gameId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // ==================== USERS WITH GAME (for notifications) ====================
    public static List<Integer> getUsersWithGame(int gameId) throws SQLException {
        List<Integer> users = new ArrayList<>();
        String sql = "SELECT user_id FROM wishlist WHERE game_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getInt("user_id"));
            }
        }
        return users;
    }
}