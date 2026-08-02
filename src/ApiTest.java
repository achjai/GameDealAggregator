import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiTest {
    public static void main(String[] args) {
        try {
            String urlStr = "https://www.cheapshark.com/api/1.0/deals?storeID=1&upperPrice=50&pageSize=5";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "GameAggregator/1.0 (collegeproject@example.com)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                System.out.println("Raw JSON: " + response.toString());

                // Parse and show first few games
                JsonArray deals = JsonParser.parseString(response.toString()).getAsJsonArray();
                System.out.println("Total deals: " + deals.size());
                for (int i = 0; i < Math.min(deals.size(), 3); i++) {
                    JsonObject obj = deals.get(i).getAsJsonObject();
                    System.out.println("Game: " + obj.get("title").getAsString());
                    System.out.println("  Store ID: " + obj.get("storeID").getAsInt());
                    System.out.println("  Retail: " + obj.get("retailPrice").getAsString());
                    System.out.println("  Sale: " + obj.get("salePrice").getAsString());
                    System.out.println("  Deal ID: " + obj.get("dealID").getAsString());
                }
            } else {
                // Read error stream
                BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = err.readLine()) != null) System.out.println("Error: " + line);
                err.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}