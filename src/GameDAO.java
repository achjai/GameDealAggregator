import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    // ==================== INSERT (fixed) ====================
    public static void insertGame(Game g) throws SQLException {
        String sql = "{ ? = call insert_game(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, g.getGameName());
            stmt.setString(3, g.getDescription());
            stmt.setInt(4, g.getStoreId());
            stmt.setBigDecimal(5, g.getNormalPrice());
            stmt.setBigDecimal(6, g.getSalePrice());
            stmt.setInt(7, g.getPopularityRank());
            // FIX: use BigDecimal for rating
            stmt.setBigDecimal(8, BigDecimal.valueOf(g.getRating()));
            stmt.setInt(9, g.getReleaseYear());
            stmt.setString(10, g.getCategory());
            stmt.setString(11, g.getDealUrl());
            stmt.execute();
        }
    }

    // ==================== UPDATE PRICE ====================
    public static boolean updatePrice(int gameId, BigDecimal newPrice) throws SQLException {
        String sql = "{ ? = call update_price(?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setInt(2, gameId);
            stmt.setBigDecimal(3, newPrice);
            stmt.execute();
            return stmt.getBoolean(1);
        }
    }

    // ==================== UPDATE BY CATEGORY ====================
    public static int updatePriceByCategory(String category, double discountPercent) throws SQLException {
        String sql = "{ ? = call update_price_by_category(?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, category);
            stmt.setDouble(3, discountPercent);
            stmt.execute();
            return stmt.getInt(1);
        }
    }

    // ==================== GET BY ID ====================
    public static Game getGameById(int id) throws SQLException {
        String sql = "SELECT g.game_id, g.game_name, g.description, g.store_id, s.store_name, " +
                "g.normal_price, g.sale_price, g.popularity_rank, g.rating, g.release_year, g.category, g.deal_url " +
                "FROM games g JOIN stores s ON g.store_id = s.store_id WHERE g.game_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    // ==================== GET WITH FILTERS (uses stored function) ====================
    public static List<Game> getGames(Double minPrice, Double maxPrice, String category) throws SQLException {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM get_games_filtered(?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (minPrice != null) stmt.setBigDecimal(1, java.math.BigDecimal.valueOf(minPrice));
            else stmt.setNull(1, Types.NUMERIC);
            if (maxPrice != null) stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(maxPrice));
            else stmt.setNull(2, Types.NUMERIC);
            if (category != null) stmt.setString(3, category);
            else stmt.setNull(3, Types.VARCHAR);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ==================== GET BY CATEGORY ====================
    public static List<Game> getGamesByCategory(String category) throws SQLException {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT g.game_id, g.game_name, g.description, g.store_id, s.store_name, " +
                "g.normal_price, g.sale_price, g.popularity_rank, g.rating, g.release_year, g.category, g.deal_url " +
                "FROM games g JOIN stores s ON g.store_id = s.store_id WHERE LOWER(g.category) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ==================== GET ALL CATEGORIES ====================
    public static List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM games ORDER BY category";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) categories.add(rs.getString("category"));
        }
        return categories;
    }

    // ==================== MAP ROW ====================
    private static Game mapRow(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setGameId(rs.getInt("game_id"));
        g.setGameName(rs.getString("game_name"));
        g.setDescription(rs.getString("description"));
        g.setStoreId(rs.getInt("store_id"));
        g.setStoreName(rs.getString("store_name"));
        g.setNormalPrice(rs.getBigDecimal("normal_price"));
        g.setSalePrice(rs.getBigDecimal("sale_price"));
        g.setPopularityRank(rs.getInt("popularity_rank"));
        g.setRating(rs.getDouble("rating"));
        g.setReleaseYear(rs.getInt("release_year"));
        g.setCategory(rs.getString("category"));
        g.setDealUrl(rs.getString("deal_url"));
        return g;
    }
}