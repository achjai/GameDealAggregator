import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    // Undo stack for price changes
    private static GenericStack<PriceChange> undoStack = new GenericStack<>();

    // ==================== PRICE SETTING ====================
    public static boolean setGamePrice(int gameId, BigDecimal newPrice) {
        try {
            Game g = GameDAO.getGameById(gameId);
            if (g == null) return false;
            BigDecimal oldPrice = g.getSalePrice();
            boolean updated = GameDAO.updatePrice(gameId, newPrice);
            if (updated) {
                undoStack.push(new PriceChange(gameId, g.getGameName(), oldPrice, newPrice));
                String msg = "Price drop! " + g.getGameName() + " is now $" + String.format("%.2f", newPrice);
                notifyUsersForGame(gameId, msg);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Error updating game price: " + e.getMessage());
            return false;
        }
    }

    // ==================== CATEGORY DISCOUNT ====================
    public static boolean applyCategoryDiscount(String category, double discountPercent) {


        if (discountPercent < 0 || discountPercent > 1.0) {
            System.out.println("Discount must be between 0 and 1.0 (e.g., 0.20 for 20%)");
            return false;
        }

        try {
            List<Game> games = GameDAO.getGamesByCategory(category);
            if (games.isEmpty()) {
                System.out.println("No games found in category '" + category + "'.");
                return false;
            }

            int updatedCount = 0;
            for (Game g : games) {
                // Additional safety: skip any game with price 0.00
                if (g.getSalePrice().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                BigDecimal oldPrice = g.getSalePrice();
                BigDecimal newPrice = oldPrice.multiply(BigDecimal.ONE.subtract(new BigDecimal(discountPercent)));
                boolean updated = GameDAO.updatePrice(g.getGameId(), newPrice);

                if (updated) {
                    undoStack.push(new PriceChange(g.getGameId(), g.getGameName(), oldPrice, newPrice));
                    String msg = "Price drop! " + g.getGameName() + " is now $" + String.format("%.2f", newPrice);
                    notifyUsersForGame(g.getGameId(), msg);
                    updatedCount++;
                }
            }

            if (updatedCount == 0) {
                System.out.println("No games were updated in category '" + category + "'. (Maybe all were free?)");
                return false;
            }

            System.out.println("Applied discount to " + updatedCount + " games in category '" + category + "'.");
            return true;

        } catch (SQLException e) {
            System.err.println("Error applying category discount: " + e.getMessage());
            return false;
        }
    }

    // ==================== UNDO ====================
    public static boolean undoLastPriceChange() {
        if (undoStack.isEmpty()) {
            System.out.println("No price changes to undo.");
            return false;
        }
        PriceChange change = undoStack.pop();
        try {
            boolean reverted = GameDAO.updatePrice(change.getGameId(), change.getOldPrice());
            if (reverted) {
                System.out.println("Undo successful: " + change.getGameName() + " reverted to $" + change.getOldPrice());
                return true;
            } else {
                // revert failed – push back to stack
                undoStack.push(change);
                System.err.println("Undo failed.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error during undo: " + e.getMessage());
            return false;
        }
    }

    // ==================== NOTIFICATION HELPER ====================
    public static void notifyUsersForGame(int gameId, String message) {
        try {
            List<Integer> userIds = WishlistDAO.getUsersWithGame(gameId);
            for (int userId : userIds) {
                NotificationDAO.add(userId, message);
            }
            if (!userIds.isEmpty()) {
                System.out.println("Sent notifications to " + userIds.size() + " users.");
            }
        } catch (SQLException e) {
            System.err.println("Could not send notifications: " + e.getMessage());
        }
    }

    // ==================== RAW SQL ====================
    public static boolean executeSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            System.out.println("No SQL provided.");
            return false;
        }
        sql = sql.trim();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // .execute() returns true if the first result is a ResultSet (SELECT),
            // false if it's an update count (INSERT, UPDATE, DELETE, DDL, etc.)
            boolean isResultSet = stmt.execute(sql);

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    printResultSet(rs);
                }
            } else {
                int affected = stmt.getUpdateCount();
                System.out.println("Query executed. Affected rows: " + affected);
            }
            return true;

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return false;
        }
    }

    // ==================== TABLE PRINTER ====================
    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        List<String> headers = new ArrayList<>();
        for (int i = 1; i <= colCount; i++) {
            headers.add(meta.getColumnName(i));
        }
        printTableRow(headers);

        List<String> separators = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            separators.add("--------");
        }
        printTableRow(separators);

        int rowCount = 0;
        while (rs.next() && rowCount < 50) {
            List<String> rowData = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                Object val = rs.getObject(i);
                rowData.add(val != null ? val.toString() : "null");
            }
            printTableRow(rowData);
            rowCount++;
        }
        if (rowCount >= 50) {
            System.out.println("... (only showing first 50 rows)");
        }
        System.out.println("Total rows returned: " + rowCount);
    }

    private static void printTableRow(List<String> columns) {
        StringBuilder sb = new StringBuilder("| ");
        for (String col : columns) {
            sb.append(String.format("%-20s | ", col.length() > 20 ? col.substring(0, 20) : col));
        }
        System.out.println(sb.toString());
    }
}