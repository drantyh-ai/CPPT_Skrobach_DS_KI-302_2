/**
 * Категорія матчу спортивної ліги.
 */
public enum MatchKind {
    LEAGUE("ліговий"),
    PLAYOFF("плей-оф");

    private final String label;

    MatchKind(String label) {
        this.label = label;
    }

    /**
     * @return україномовна назва категорії
     */
    public String label() {
        return label;
    }
}
