import com.google.gson.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSeeder {

    private static final Random random = new Random();
    private static final String[] CATEGORIES = {
            "Action", "RPG", "Shooter", "Adventure", "Strategy",
            "Sports", "Racing", "Simulation", "Puzzle", "Fighting"
    };
    private static final int[] STORE_IDS = {1, 2, 3, 25};
    private static final int MAX_PER_STORE = 25;
    private static final int TOTAL_CAP = 100;

    // ============================================================
    // FIXED: seedIfEmpty() - checks games BEFORE inserting free games
    // ============================================================
    public static void seedIfEmpty() {
        createAdminIfMissing();

        // Check if games exist BEFORE inserting free games
        boolean hasGames = false;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM games")) {
            rs.next();
            hasGames = rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("System Message: Failed to check database: " + e.getMessage());
            return;
        }

        // If NO games exist, do a full seed (API + free games)
        if (!hasGames) {
            System.out.println("System Message: Fetching deals from CheapShark API...");
            List<Game> games = fetchFromCheapShark();

            if (!games.isEmpty()) {
                upsertGames(games);
                System.out.println("System Message: Seeding complete. " + games.size() + " games added.");
            } else {
                System.out.println("System Message: API failed. Using backup data...");
                List<Game> backup = getBackupGames();
                if (!backup.isEmpty()) {
                    upsertGames(backup);
                    System.out.println("System Message: Seeding complete (backup). " + backup.size() + " games added.");
                } else {
                    System.out.println("System Message: Seeding failed. No data available.");
                }
            }
        }

        // ALWAYS ensure free games exist (whether we just seeded or not)
        ensureFreeGamesExist();

        System.out.println("System Message: Database ready.");
    }

    private static void createAdminIfMissing() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String hashed = BCrypt.hashpw("admin123", BCrypt.gensalt());
            PreparedStatement check = conn.prepareStatement("SELECT 1 FROM users WHERE username = 'admin'");
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE users SET hashed_password = ?, role = 'ADMIN' WHERE username = 'admin'"
                );
                update.setString(1, hashed);
                update.executeUpdate();
            } else {
                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO users (username, hashed_password, first_name, last_name, role) VALUES (?, ?, ?, ?, ?)"
                );
                insert.setString(1, "admin");
                insert.setString(2, hashed);
                insert.setString(3, "Admin");
                insert.setString(4, "User");
                insert.setString(5, "ADMIN");
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("System Message: Could not set up admin: " + e.getMessage());
        }
    }

    private static void ensureFreeGamesExist() {
        String[][] freeGames = {
                {"Counter-Strike 2", "1", "0.00", "0.00", "https://store.steampowered.com/app/730/", "2023", "Free"},
                {"Valorant", "1", "0.00", "0.00", "https://store.steampowered.com/", "2020", "Free"},
                {"Apex Legends", "2", "0.00", "0.00", "https://store.epicgames.com/", "2019", "Free"},
                {"Overwatch 2", "2", "0.00", "0.00", "https://store.epicgames.com/", "2022", "Free"}
        };

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/game_aggregator",
                "postgres", "root")) {
            for (String[] row : freeGames) {
                PreparedStatement check = conn.prepareStatement("SELECT 1 FROM games WHERE game_name = ?");
                check.setString(1, row[0]);
                ResultSet rs = check.executeQuery();
                if (rs.next()) continue;

                Game g = new Game();
                g.setGameName(row[0]);
                g.setStoreId(Integer.parseInt(row[1]));
                g.setNormalPrice(new BigDecimal(row[2]));
                g.setSalePrice(new BigDecimal(row[3]));
                g.setDealUrl(row[4]);
                g.setReleaseYear(Integer.parseInt(row[5]));
                g.setCategory(row[6]);
                g.setPopularityRank(50 + random.nextInt(50));
                g.setRating(7.0 + (9.8 - 7.0) * random.nextDouble());
                g.setDescription("Free game: " + row[0]);
                GameDAO.insertGame(g);
            }
        } catch (SQLException e) {
            System.err.println("System Message: Could not ensure free games: " + e.getMessage());
        }
    }

    private static List<Game> fetchFromCheapShark() {
        List<Game> allGames = new ArrayList<>();
        int pageSize = 60;

        for (int storeId : STORE_IDS) {
            int page = 0;
            int fetched = 0;

            while (fetched < MAX_PER_STORE && allGames.size() < TOTAL_CAP) {
                try {
                    String urlStr = "https://www.cheapshark.com/api/1.0/deals?storeID=" + storeId
                            + "&pageSize=" + pageSize + "&page=" + page;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "GameAggregator/1.0 (collegeproject@example.com)");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        System.err.println("System Message: API error for store " + storeId + " page " + page + " (HTTP " + responseCode + ")");
                        break;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JsonArray deals = JsonParser.parseString(response.toString()).getAsJsonArray();
                    if (deals.size() == 0) break;

                    for (JsonElement elem : deals) {
                        if (fetched >= MAX_PER_STORE || allGames.size() >= TOTAL_CAP) break;
                        JsonObject obj = elem.getAsJsonObject();

                        String title = getString(obj, "title");
                        String retail = getString(obj, "retailPrice");
                        String sale = getString(obj, "salePrice");
                        String dealId = getString(obj, "dealID");

                        if (title == null || dealId == null) continue;

                        Game g = new Game();
                        g.setGameName(title);
                        g.setStoreId(storeId);

                        BigDecimal salePrice = new BigDecimal(sale != null ? sale : "0.00");
                        g.setSalePrice(salePrice);

                        BigDecimal normalPrice;
                        if (retail != null && !retail.equals("0.00")) {
                            normalPrice = new BigDecimal(retail);
                        } else if (salePrice.compareTo(BigDecimal.ZERO) > 0) {
                            normalPrice = salePrice.multiply(new BigDecimal("1.5"));
                            if (normalPrice.compareTo(new BigDecimal("100")) > 0) {
                                normalPrice = new BigDecimal("59.99");
                            }
                        } else {
                            normalPrice = new BigDecimal("59.99");
                        }
                        g.setNormalPrice(normalPrice);

                        g.setDealUrl("https://www.cheapshark.com/redirect?dealID=" + dealId);
                        g.setPopularityRank(random.nextInt(100) + 1);
                        g.setRating(6.0 + (9.5 - 6.0) * random.nextDouble());
                        g.setReleaseYear(2015 + random.nextInt(9));

                        if (g.getSalePrice().compareTo(BigDecimal.ZERO) == 0) {
                            g.setCategory("Free");
                        } else {
                            g.setCategory(CATEGORIES[random.nextInt(CATEGORIES.length)]);
                        }

                        g.setDescription("Available on " + getStoreName(storeId));
                        allGames.add(g);
                        fetched++;
                    }

                    page++;
                    if (deals.size() < pageSize) break;

                } catch (Exception e) {
                    System.err.println("System Message: API error for store " + storeId + ": " + e.getMessage());
                    break;
                }
            }
        }

        System.out.println("System Message: Fetched " + allGames.size() + " games from API.");
        return allGames;
    }

    private static void upsertGames(List<Game> games) {
        String selectSQL = "SELECT game_id FROM games WHERE LOWER(game_name) = LOWER(?)";
        String updateSQL = "UPDATE games SET description = ?, store_id = ?, normal_price = ?, sale_price = ?, " +
                "popularity_rank = ?, rating = ?, release_year = ?, category = ?, deal_url = ?, " +
                "last_updated = CURRENT_TIMESTAMP WHERE game_id = ?";
        String insertSQL = "INSERT INTO games (game_name, description, store_id, normal_price, sale_price, " +
                "popularity_rank, rating, release_year, category, deal_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int updated = 0, inserted = 0;
            for (Game g : games) {
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
                    selectStmt.setString(1, g.getGameName());
                    ResultSet rs = selectStmt.executeQuery();
                    if (rs.next()) {
                        int id = rs.getInt("game_id");
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                            updateStmt.setString(1, g.getDescription());
                            updateStmt.setInt(2, g.getStoreId());
                            updateStmt.setBigDecimal(3, g.getNormalPrice());
                            updateStmt.setBigDecimal(4, g.getSalePrice());
                            updateStmt.setInt(5, g.getPopularityRank());
                            updateStmt.setDouble(6, g.getRating());
                            updateStmt.setInt(7, g.getReleaseYear());
                            updateStmt.setString(8, g.getCategory());
                            updateStmt.setString(9, g.getDealUrl());
                            updateStmt.setInt(10, id);
                            updateStmt.executeUpdate();
                            updated++;
                        }
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                            insertStmt.setString(1, g.getGameName());
                            insertStmt.setString(2, g.getDescription());
                            insertStmt.setInt(3, g.getStoreId());
                            insertStmt.setBigDecimal(4, g.getNormalPrice());
                            insertStmt.setBigDecimal(5, g.getSalePrice());
                            insertStmt.setInt(6, g.getPopularityRank());
                            insertStmt.setDouble(7, g.getRating());
                            insertStmt.setInt(8, g.getReleaseYear());
                            insertStmt.setString(9, g.getCategory());
                            insertStmt.setString(10, g.getDealUrl());
                            insertStmt.executeUpdate();
                            inserted++;
                        }
                    }
                }
            }
            System.out.println("System Message: Upsert complete. " + updated + " updated, " + inserted + " inserted.");
        } catch (SQLException e) {
            System.err.println("System Message: Upsert failed: " + e.getMessage());
        }
    }

    private static String getString(JsonObject obj, String key) {
        JsonElement elem = obj.get(key);
        return (elem != null && !elem.isJsonNull()) ? elem.getAsString() : null;
    }

    private static int getInt(JsonObject obj, String key) {
        JsonElement elem = obj.get(key);
        return (elem != null && !elem.isJsonNull()) ? elem.getAsInt() : 0;
    }

    private static List<Game> getBackupGames() {
        List<Game> list = new ArrayList<>();
        String[][] data = {
                {"Elden Ring", "1", "59.99", "39.99", "https://store.steampowered.com/app/1245620/", "2022", "RPG"},
                {"Cyberpunk 2077", "1", "59.99", "29.99", "https://store.steampowered.com/app/1091500/", "2020", "RPG"},
                {"Grand Theft Auto V", "1", "29.99", "14.99", "https://store.steampowered.com/app/271590/", "2015", "Action"},
                {"Minecraft", "1", "26.95", "26.95", "https://www.minecraft.net/", "2011", "Adventure"},
                {"The Witcher 3", "3", "39.99", "9.99", "https://www.gog.com/game/the_witcher_3_wild_hunt", "2015", "RPG"},
                {"Red Dead Redemption 2", "1", "59.99", "29.99", "https://store.steampowered.com/app/1174180/", "2019", "Action"},
                {"Hades", "1", "24.99", "19.99", "https://store.steampowered.com/app/1145360/", "2020", "Action"},
                {"Stardew Valley", "1", "14.99", "11.99", "https://store.steampowered.com/app/413150/", "2016", "Simulation"},
                {"Baldur's Gate 3", "1", "59.99", "55.99", "https://store.steampowered.com/app/1086940/", "2023", "RPG"},
                {"Hogwarts Legacy", "2", "59.99", "39.99", "https://store.epicgames.com/", "2023", "Adventure"},
                {"Starfield", "1", "69.99", "49.99", "https://store.steampowered.com/app/1716740/", "2023", "RPG"},
                {"Resident Evil 4", "1", "59.99", "39.99", "https://store.steampowered.com/app/2050650/", "2023", "Action"},
                {"Counter-Strike 2", "1", "0.00", "0.00", "https://store.steampowered.com/app/730/", "2023", "Free"},
                {"Diablo IV", "2", "69.99", "39.99", "https://store.epicgames.com/", "2023", "RPG"},
                {"Assassin's Creed Mirage", "2", "49.99", "29.99", "https://store.epicgames.com/", "2023", "Adventure"},
                {"Forza Horizon 5", "1", "59.99", "39.99", "https://store.steampowered.com/app/1551360/", "2021", "Racing"},
                {"Call of Duty: MW II", "2", "69.99", "34.99", "https://store.epicgames.com/", "2022", "Shooter"},
                {"Overwatch 2", "2", "0.00", "0.00", "https://store.epicgames.com/", "2022", "Free"},
                {"Apex Legends", "2", "0.00", "0.00", "https://store.epicgames.com/", "2019", "Free"},
                {"Valorant", "1", "0.00", "0.00", "https://store.steampowered.com/", "2020", "Free"},
                {"God of War", "1", "49.99", "29.99", "https://store.steampowered.com/app/1593500/", "2022", "Action"},
                {"Horizon Zero Dawn", "1", "49.99", "24.99", "https://store.steampowered.com/app/1151640/", "2020", "Action"},
                {"Days Gone", "1", "49.99", "19.99", "https://store.steampowered.com/app/1259420/", "2021", "Action"},
                {"Death Stranding", "1", "59.99", "29.99", "https://store.steampowered.com/app/1190460/", "2020", "Adventure"},
                {"Control", "2", "39.99", "14.99", "https://store.epicgames.com/", "2019", "Action"},
                {"Sekiro", "1", "59.99", "29.99", "https://store.steampowered.com/app/814380/", "2019", "Action"},
                {"Ghost of Tsushima", "1", "59.99", "39.99", "https://store.steampowered.com/app/2215430/", "2024", "Action"},
                {"Spider-Man Remastered", "1", "59.99", "39.99", "https://store.steampowered.com/app/1817070/", "2022", "Action"},
                {"Uncharted 4", "1", "49.99", "29.99", "https://store.steampowered.com/app/1659420/", "2022", "Adventure"},
                {"The Last of Us Part I", "1", "59.99", "39.99", "https://store.steampowered.com/app/1888930/", "2023", "Action"}
        };

        for (String[] row : data) {
            Game g = new Game();
            g.setGameName(row[0]);
            g.setStoreId(Integer.parseInt(row[1]));
            g.setNormalPrice(new BigDecimal(row[2]));
            g.setSalePrice(new BigDecimal(row[3]));
            g.setDealUrl(row[4]);
            g.setReleaseYear(Integer.parseInt(row[5]));
            g.setCategory(row[6]);
            g.setPopularityRank(50 + random.nextInt(50));
            g.setRating(7.0 + (9.8 - 7.0) * random.nextDouble());
            g.setDescription("Backup game: " + row[0]);
            list.add(g);
        }
        return list;
    }

    private static String getStoreName(int id) {
        switch (id) {
            case 1: return "Steam";
            case 2: return "Epic Games";
            case 3: return "GOG";
            case 25: return "Humble Bundle";
            default: return "Unknown Store";
        }
    }
}