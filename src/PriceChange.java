import java.math.BigDecimal;

public class PriceChange {
    private int gameId;
    private String gameName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;

    public PriceChange(int gameId, String gameName, BigDecimal oldPrice, BigDecimal newPrice) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    public int getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
}