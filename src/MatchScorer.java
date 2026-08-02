import org.apache.commons.text.similarity.JaroWinklerSimilarity;

public class MatchScorer {
    private static final JaroWinklerSimilarity JARO_WINKLER = new JaroWinklerSimilarity();

    public static double calculate(String query, String gameName) {
        if (query == null || query.isEmpty()) return 1.0;
        if (gameName == null || gameName.isEmpty()) return 0.0;

        String q = query.toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim();
        String name = gameName.toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim();

        if (q.isEmpty() || name.isEmpty()) return 0.0;
        if (q.equals(name)) return 1.0;
        if (name.contains(q)) {
            return 0.85 + 0.15 * ((double) q.length() / name.length());
        }
        return JARO_WINKLER.apply(q, name);
    }
}