import java.util.*;
import java.util.stream.Collectors;

public class GameService {

    public static SearchResult search(String query, Double minPrice, Double maxPrice, String category,
                                      String sortBy, int page, int pageSize) {
        try {
            List<Game> games = GameDAO.getGames(minPrice, maxPrice, category);
            boolean hasQuery = query != null && !query.trim().isEmpty();

            for (Game g : games) {
                if (hasQuery) {
                    double score = MatchScorer.calculate(query, g.getGameName());
                    g.setMatchScore(score);
                } else {
                    g.setMatchScore(1.0);
                }
            }

            if (hasQuery) {
                boolean anyMatch = games.stream().anyMatch(g -> g.getMatchScore() > 0.0);
                if (anyMatch) {
                    games = games.stream()
                            .filter(g -> g.getMatchScore() > 0.0)
                            .collect(Collectors.toList());
                }
            }

            Comparator<Game> comparator = getComparator(sortBy, hasQuery);
            games.sort(comparator);

            int total = games.size();
            int fromIndex = page * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<Game> pageItems = (fromIndex < total) ? games.subList(fromIndex, toIndex) : new ArrayList<>();

            return new SearchResult(pageItems, total);
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResult(new ArrayList<>(), 0);
        }
    }

    private static Comparator<Game> getComparator(String sortBy, boolean hasQuery) {
        switch (sortBy) {
            case "price_low":
                return Comparator.comparing(Game::getSalePrice);
            case "price_high":
                return Comparator.comparing(Game::getSalePrice, Comparator.reverseOrder());
            case "popularity":
                return Comparator.comparingInt(Game::getPopularityRank).reversed();
            case "name":
                return Comparator.comparing(Game::getGameName, String.CASE_INSENSITIVE_ORDER);
            case "rating":
                return Comparator.comparingDouble(Game::getRating).reversed();
            case "relevance":
            default:
                if (hasQuery) {
                    return Comparator.comparingDouble(Game::getMatchScore).reversed();
                } else {
                    return Comparator.comparingInt(Game::getPopularityRank).reversed();
                }
        }
    }

    public static class SearchResult {
        public final List<Game> games;
        public final int totalCount;

        public SearchResult(List<Game> games, int totalCount) {
            this.games = games;
            this.totalCount = totalCount;
        }
    }
}