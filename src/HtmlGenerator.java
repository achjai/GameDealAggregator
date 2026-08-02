import java.awt.Desktop;
import java.io.*;

public class HtmlGenerator {
    private static final String TEMPLATE =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>{GAME_NAME} - Game Deal</title>\n" +
                    "    <style>\n" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                    "        body {\n" +
                    "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                    "            background: #1b2838;\n" +
                    "            color: #c7d5e0;\n" +
                    "            display: flex;\n" +
                    "            justify-content: center;\n" +
                    "            padding: 40px 20px;\n" +
                    "        }\n" +
                    "        .game-card {\n" +
                    "            max-width: 900px;\n" +
                    "            width: 100%;\n" +
                    "            background: #2a475e;\n" +
                    "            border-radius: 12px;\n" +
                    "            padding: 30px;\n" +
                    "            box-shadow: 0 10px 30px rgba(0,0,0,0.6);\n" +
                    "        }\n" +
                    "        .game-header {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: flex-start;\n" +
                    "            flex-wrap: wrap;\n" +
                    "            border-bottom: 1px solid #4b6b7f;\n" +
                    "            padding-bottom: 15px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .game-title {\n" +
                    "            font-size: 2.2em;\n" +
                    "            font-weight: 700;\n" +
                    "            color: #ffffff;\n" +
                    "        }\n" +
                    "        .game-store {\n" +
                    "            background: #4b6b7f;\n" +
                    "            padding: 6px 15px;\n" +
                    "            border-radius: 20px;\n" +
                    "            font-size: 0.9em;\n" +
                    "            font-weight: 500;\n" +
                    "            color: #e5e5e5;\n" +
                    "            margin-top: 8px;\n" +
                    "        }\n" +
                    "        .game-details {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: 1fr 1fr;\n" +
                    "            gap: 20px 40px;\n" +
                    "            background: #1b2838;\n" +
                    "            padding: 20px;\n" +
                    "            border-radius: 8px;\n" +
                    "            margin: 20px 0;\n" +
                    "        }\n" +
                    "        .detail-item {\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "        }\n" +
                    "        .detail-label {\n" +
                    "            font-size: 0.8em;\n" +
                    "            text-transform: uppercase;\n" +
                    "            color: #8f9fa8;\n" +
                    "            letter-spacing: 0.5px;\n" +
                    "        }\n" +
                    "        .detail-value {\n" +
                    "            font-size: 1.2em;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #ffffff;\n" +
                    "            margin-top: 2px;\n" +
                    "        }\n" +
                    "        .price-box {\n" +
                    "            display: flex;\n" +
                    "            align-items: baseline;\n" +
                    "            gap: 15px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .normal-price {\n" +
                    "            text-decoration: line-through;\n" +
                    "            color: #8f9fa8;\n" +
                    "            font-size: 1em;\n" +
                    "        }\n" +
                    "        .sale-price {\n" +
                    "            font-size: 1.8em;\n" +
                    "            font-weight: 700;\n" +
                    "            color: #5cbf5c;\n" +
                    "        }\n" +
                    "        .description {\n" +
                    "            margin: 20px 0;\n" +
                    "            padding: 15px;\n" +
                    "            background: #1b2838;\n" +
                    "            border-radius: 8px;\n" +
                    "            line-height: 1.6;\n" +
                    "            color: #c7d5e0;\n" +
                    "        }\n" +
                    "        .btn-buy {\n" +
                    "            display: inline-block;\n" +
                    "            background: #5cbf5c;\n" +
                    "            color: #fff;\n" +
                    "            padding: 14px 35px;\n" +
                    "            border-radius: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "            font-weight: 700;\n" +
                    "            text-decoration: none;\n" +
                    "            transition: all 0.2s;\n" +
                    "            border: none;\n" +
                    "            cursor: pointer;\n" +
                    "        }\n" +
                    "        .btn-buy:hover {\n" +
                    "            background: #4da64d;\n" +
                    "            transform: scale(1.03);\n" +
                    "            box-shadow: 0 4px 15px rgba(92, 191, 92, 0.4);\n" +
                    "        }\n" +
                    "        .footer {\n" +
                    "            margin-top: 20px;\n" +
                    "            font-size: 0.8em;\n" +
                    "            color: #8f9fa8;\n" +
                    "            border-top: 1px solid #4b6b7f;\n" +
                    "            padding-top: 15px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        @media (max-width: 600px) {\n" +
                    "            .game-details { grid-template-columns: 1fr; }\n" +
                    "            .game-title { font-size: 1.6em; }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"game-card\">\n" +
                    "        <div class=\"game-header\">\n" +
                    "            <div class=\"game-title\">{GAME_NAME}</div>\n" +
                    "            <div class=\"game-store\">{STORE}</div>\n" +
                    "        </div>\n" +
                    "        <div class=\"game-details\">\n" +
                    "            <div class=\"detail-item\">\n" +
                    "                <span class=\"detail-label\">Price</span>\n" +
                    "                <div class=\"price-box\">\n" +
                    "                    <span class=\"normal-price\">${NORMAL_PRICE}</span>\n" +
                    "                    <span class=\"sale-price\">${SALE_PRICE}</span>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            <div class=\"detail-item\">\n" +
                    "                <span class=\"detail-label\">Popularity Rank</span>\n" +
                    "                <span class=\"detail-value\">#{POPULARITY}</span>\n" +
                    "            </div>\n" +
                    "            <div class=\"detail-item\">\n" +
                    "                <span class=\"detail-label\">Rating</span>\n" +
                    "                <span class=\"detail-value\">{RATING} / 10</span>\n" +
                    "            </div>\n" +
                    "            <div class=\"detail-item\">\n" +
                    "                <span class=\"detail-label\">Release Year</span>\n" +
                    "                <span class=\"detail-value\">{RELEASE_YEAR}</span>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        <div class=\"description\">\n" +
                    "            {DESCRIPTION}\n" +
                    "        </div>\n" +
                    "        <div style=\"text-align: center; margin: 25px 0 10px;\">\n" +
                    "            <a href=\"{DEAL_URL}\" class=\"btn-buy\" target=\"_blank\">🎮 Buy Now on {STORE}</a>\n" +
                    "        </div>\n" +
                    "        <div class=\"footer\">\n" +
                    "            Generated by Game Aggregator &bull; Data provided by CheapShark\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

    public static void generate(Game game) {
        String output = TEMPLATE
                .replace("{GAME_NAME}", game.getGameName())
                .replace("{STORE}", game.getStoreName())
                .replace("{NORMAL_PRICE}", String.format("%.2f", game.getNormalPrice()))
                .replace("{SALE_PRICE}", String.format("%.2f", game.getSalePrice()))
                .replace("{POPULARITY}", String.valueOf(game.getPopularityRank()))
                .replace("{RATING}", String.format("%.1f", game.getRating()))
                .replace("{RELEASE_YEAR}", game.getReleaseYear() == 0 ? "N/A" : String.valueOf(game.getReleaseYear()))
                .replace("{DESCRIPTION}", game.getDescription() != null ? game.getDescription() : "No description available.")
                .replace("{DEAL_URL}", game.getDealUrl() != null ? game.getDealUrl() : "#");

        File dir = new File("./output");
        if (!dir.exists()) dir.mkdir();

        String safeName = game.getGameName()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .trim();
        if (safeName.isEmpty()) safeName = "game_" + game.getGameId();
        String filename = "./output/" + safeName + "_report.html";

        File htmlFile = new File(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile))) {
            writer.write(output);
            writer.flush();
            System.out.println("HTML generated: " + htmlFile.getAbsolutePath());
            openInBrowser(htmlFile);
        } catch (IOException e) {
            System.err.println("Failed to write HTML: " + e.getMessage());
        }
    }

    private static void openInBrowser(File file) {
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getAbsolutePath());
            return;
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
                System.out.println("Opened in default browser.");
                return;
            } catch (Exception e) {
                System.err.println("Desktop API failed: " + e.getMessage());
            }
        }
        String os = System.getProperty("os.name").toLowerCase();
        try {
            String absPath = file.getAbsolutePath();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", absPath});
                System.out.println("Opened via Windows command.");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", absPath});
                System.out.println("Opened via Mac 'open'.");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", absPath});
                System.out.println("Opened via xdg-open.");
            } else {
                System.out.println("➡️ Please open the file manually: " + absPath);
            }
        } catch (Exception e) {
            System.err.println("Fallback open failed: " + e.getMessage());
            System.out.println("Please open the file manually: " + file.getAbsolutePath());
        }
    }
}