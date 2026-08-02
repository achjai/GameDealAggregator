import java.sql.SQLException;
import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        DataSeeder.seedIfEmpty();

        while (true) {
            System.out.println("\n=== GAME DEAL AGGREGATOR ===");
            System.out.println("1. Login");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.print("Username: ");
                String u = scanner.nextLine();
                System.out.print("Password: ");
                String p = scanner.nextLine();
                if (AuthService.login(u, p)) {
                    currentUser = AuthService.getLoggedInUser();
                    showMainMenu();
                    AuthService.logout();
                    currentUser = null;
                }
            } else if (choice.equals("2")) {
                System.out.print("Username: ");
                String u = scanner.nextLine();
                System.out.print("Password (8+ chars, upper, lower, digit, special): ");
                String p = scanner.nextLine();
                System.out.print("First Name: ");
                String f = scanner.nextLine();
                System.out.print("Last Name: ");
                String l = scanner.nextLine();
                if (AuthService.signup(u, p, f, l, "USER")) {
                    System.out.println("Signup successful! Please login.");
                }
            } else if (choice.equals("3")) {
                System.out.println("Bye!");
                DatabaseConnection.closeConnection();
                System.exit(0);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void showMainMenu() {
        if (currentUser.isAdmin()) {
            showAdminMenu();
        } else {
            showUserMenu();
        }
    }

    private static void showUserMenu() {
        while (true) {
            System.out.println("\n--- USER MENU ---");
            System.out.println("1. Browse & Search Games");
            System.out.println("2. My Wishlist");
            System.out.println("3. Notifications (Price Drops)");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": browseGames(); break;
                case "2": wishlistMenu(); break;
                case "3": showNotifications(); break;
                case "4": System.out.println("Logging out..."); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private static void showAdminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Browse & Search Games");
            System.out.println("2. Set price for a specific game");
            System.out.println("3. Apply discount to a category");
            System.out.println("4. Execute raw SQL");
            System.out.println("5. Undo last price change");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": browseGames(); break;
                case "2": adminSetGamePrice(); break;
                case "3": adminApplyCategoryDiscount(); break;
                case "4": adminExecuteSQL(); break;
                case "5": adminUndo(); break;
                case "6": System.out.println("Logging out..."); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // ============================================================
    // 1. BROWSE & SEARCH (shared)
    // ============================================================
    private static void browseGames() {
        String searchQuery = "";
        Double minPrice = null;
        Double maxPrice = null;
        String category = null;
        String sortBy = "relevance";

        while (true) {
            System.out.println("\n--- SEARCH FILTERS ---");
            System.out.println("Search: '" + (searchQuery.isEmpty() ? "(all)" : searchQuery) + "'");
            System.out.println("Price: " + (minPrice==null?"any":minPrice) + " - " + (maxPrice==null?"any":maxPrice));
            System.out.println("Category: " + (category==null?"any":category));
            System.out.println("Sort: " + sortBy);
            System.out.println("1. Set search term");
            System.out.println("2. Set price range");
            System.out.println("3. Set category");
            System.out.println("4. Set sort order");
            System.out.println("5. Start searching");
            System.out.println("6. Reset filters");
            System.out.println("7. Back to main menu");
            System.out.print("Choose: ");
            String opt = scanner.nextLine();

            if (opt.equals("1")) {
                System.out.print("Enter search term: ");
                searchQuery = scanner.nextLine().trim();
            } else if (opt.equals("2")) {
                try {
                    System.out.print("Min price (or Enter for none): ");
                    String minIn = scanner.nextLine().trim();
                    minPrice = minIn.isEmpty() ? null : Double.parseDouble(minIn);
                    System.out.print("Max price (or Enter for none): ");
                    String maxIn = scanner.nextLine().trim();
                    maxPrice = maxIn.isEmpty() ? null : Double.parseDouble(maxIn);
                } catch (NumberFormatException e) { System.out.println("Invalid number."); }
            } else if (opt.equals("3")) {
                try {
                    List<String> categories = GameDAO.getAllCategories();
                    System.out.println("Available categories: " + String.join(", ", categories));
                    System.out.print("Enter category (or Enter for any): ");
                    String cat = scanner.nextLine().trim();
                    category = cat.isEmpty() ? null : cat;
                } catch (SQLException e) {
                    System.out.println("Error fetching categories: " + e.getMessage());
                }
            } else if (opt.equals("4")) {
                System.out.println("Options: relevance, price_low, price_high, popularity, rating, name");
                System.out.print("Sort by: ");
                String s = scanner.nextLine().trim().toLowerCase();
                if (List.of("relevance", "price_low", "price_high", "popularity", "rating", "name").contains(s)) {
                    sortBy = s;
                } else {
                    System.out.println("Invalid sort. Using 'relevance'.");
                }
            } else if (opt.equals("5")) {
                performSearch(searchQuery, minPrice, maxPrice, category, sortBy);
            } else if (opt.equals("6")) {
                searchQuery = "";
                minPrice = null;
                maxPrice = null;
                category = null;
                sortBy = "relevance";
                System.out.println("Filters reset.");
            } else if (opt.equals("7")) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void performSearch(String query, Double minPrice, Double maxPrice,
                                      String category, String sortBy) {
        int page = 0;
        int pageSize = 10;

        while (true) {
            GameService.SearchResult result = GameService.search(query, minPrice, maxPrice,
                    category, sortBy, page, pageSize);
            List<Game> games = result.games;
            int total = result.totalCount;

            if (games.isEmpty()) {
                System.out.println("No games match your criteria.");
                break;
            }

            System.out.println("\n--- RESULTS (Page " + (page+1) + " of " + ((total + pageSize - 1)/pageSize) + ") ---");
            for (int i = 0; i < games.size(); i++) {
                Game g = games.get(i);
                String scoreStr = (query != null && !query.isEmpty()) ?
                        String.format(" | Match: %.2f", g.getMatchScore()) : "";
                System.out.printf("%d. %s | %s | Store: %s | Price: $%.2f | Rank: %d | Rating: %.1f%s\n",
                        i+1, g.getGameName(), g.getCategory(), g.getStoreName(),
                        g.getSalePrice(), g.getPopularityRank(), g.getRating(), scoreStr);
            }
            System.out.println("Total: " + total + " games");

            System.out.println("\n[N]ext | [P]revious | [V]iew details (HTML) | [W]ishlist add | [B]ack");
            String cmd = scanner.nextLine().toLowerCase();

            if (cmd.equals("n") && (page+1) * pageSize < total) page++;
            else if (cmd.equals("p") && page > 0) page--;
            else if (cmd.equals("v")) {
                System.out.print("Enter game number: ");
                try {
                    int idx = Integer.parseInt(scanner.nextLine()) - 1;
                    if (idx >= 0 && idx < games.size()) {
                        Game selected = games.get(idx);
                        Game full = GameDAO.getGameById(selected.getGameId());
                        if (full != null) {
                            HtmlGenerator.generate(full);
                        }
                    }
                } catch (NumberFormatException e) { System.out.println("Invalid number."); }
                catch (SQLException e) { System.out.println("DB error: " + e.getMessage()); }
            } else if (cmd.equals("w")) {
                System.out.print("Enter game number: ");
                try {
                    int idx = Integer.parseInt(scanner.nextLine()) - 1;
                    if (idx >= 0 && idx < games.size()) {
                        Game g = games.get(idx);
                        if (WishlistDAO.add(currentUser.getUserId(), g.getGameId())) {
                            System.out.println("Added to wishlist.");
                        } else {
                            System.out.println("Already in wishlist.");
                        }
                    }
                } catch (NumberFormatException e) { System.out.println("Invalid."); }
                catch (SQLException e) { System.out.println("DB error: " + e.getMessage()); }
            } else if (cmd.equals("b")) {
                break;
            } else {
                System.out.println("Invalid command.");
            }
        }
    }

    // ============================================================
    // 2. WISHLIST (user only)
    // ============================================================
    private static void wishlistMenu() {
        try {
            List<Game> wishlist = WishlistDAO.getWishlist(currentUser.getUserId());
            if (wishlist.isEmpty()) {
                System.out.println("Wishlist is empty.");
                return;
            }
            System.out.println("\n--- YOUR WISHLIST ---");
            for (Game g : wishlist) {
                System.out.printf("%d. %s | Store: %s | Price: $%.2f\n",
                        g.getGameId(), g.getGameName(), g.getStoreName(), g.getSalePrice());
            }
            System.out.print("\nDo you want to remove a game from wishlist? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                System.out.print("Enter Game ID to remove: ");
                int id = Integer.parseInt(scanner.nextLine());
                if (WishlistDAO.remove(currentUser.getUserId(), id)) {
                    System.out.println("Removed.");
                } else {
                    System.out.println("Not found.");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============================================================
    // 3. NOTIFICATIONS (user only)
    // ============================================================
    private static void showNotifications() {
        try {
            List<Notification> notifs = NotificationDAO.getLatest(currentUser.getUserId(), 10);
            if (notifs.isEmpty()) {
                System.out.println("No notifications.");
                return;
            }
            System.out.println("\n--- NOTIFICATIONS (Latest first) ---");
            for (Notification n : notifs) {
                System.out.printf("[%s] %s %s\n", n.getCreatedAt(), n.getMessage(), n.isRead() ? "(read)" : "");
                if (!n.isRead()) NotificationDAO.markAsRead(n.getNotifId());
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============================================================
    // ADMIN ACTIONS
    // ============================================================
    private static void adminSetGamePrice() {
        try {
            System.out.print("Enter Game ID: ");
            int gameId = Integer.parseInt(scanner.nextLine());
            Game game = GameDAO.getGameById(gameId);
            if (game == null) {
                System.out.println("Game not found.");
                return;
            }
            System.out.print("Enter new sale price: ");
            double price = Double.parseDouble(scanner.nextLine());
            boolean success = AdminService.setGamePrice(gameId, new java.math.BigDecimal(price));
            if (success) {
                System.out.println("Price updated and notifications sent.");
            } else {
                System.out.println("Update failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }

    private static void adminApplyCategoryDiscount() {
        try {
            List<String> categories = GameDAO.getAllCategories();
            System.out.println("Categories: " + String.join(", ", categories));
            System.out.print("Enter category: ");
            String cat = scanner.nextLine().trim();
            System.out.print("Enter discount percentage (e.g., 20 for 20% off): ");
            double percent = Double.parseDouble(scanner.nextLine()) / 100.0;
            boolean success = AdminService.applyCategoryDiscount(cat, percent);
            if (success) {
                System.out.println("Discount applied and notifications sent.");
            } else {
                System.out.println("Discount failed.");
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void adminExecuteSQL() {
        System.out.println("Enter SQL query (type 'exit' on a new line to execute):");
        StringBuilder sql = new StringBuilder();
        String line;
        while (true) {
            line = scanner.nextLine();
            if (line.equalsIgnoreCase("exit")) break;
            sql.append(line).append("\n");
        }
        String query = sql.toString().trim();
        if (query.isEmpty()) {
            System.out.println("No query entered.");
            return;
        }
        AdminService.executeSQL(query);
    }

    private static void adminUndo() {
        if (AdminService.undoLastPriceChange()) {
            System.out.println("Undo Successful.");
        } else {
            System.out.println("Undo failed.");
        }
    }
}