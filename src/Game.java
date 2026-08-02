import java.math.BigDecimal;

public class Game {
    private int gameId;
    private String gameName;
    private String description;
    private int storeId;
    private String storeName;
    private BigDecimal normalPrice;
    private BigDecimal salePrice;
    private int popularityRank;
    private double rating;
    private int releaseYear;
    private String category;
    private String dealUrl;
    private double matchScore;

    // Getters & Setters
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public BigDecimal getNormalPrice() { return normalPrice; }
    public void setNormalPrice(BigDecimal normalPrice) { this.normalPrice = normalPrice; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public int getPopularityRank() { return popularityRank; }
    public void setPopularityRank(int popularityRank) { this.popularityRank = popularityRank; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDealUrl() { return dealUrl; }
    public void setDealUrl(String dealUrl) { this.dealUrl = dealUrl; }
    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
}