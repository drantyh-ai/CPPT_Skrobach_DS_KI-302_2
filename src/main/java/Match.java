import java.util.Locale;
import java.util.Objects;
import java.time.LocalDate;

/**
 * Спільний тип матчів спортивної ліги.
 */
public abstract class Match {

    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;
    private final int attendance;
    private final MatchKind kind;
    private final LocalDate matchDate;
    private final int round;

    /**
     * Створює матч із перевіреними спільними полями.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність
     * @param kind категорія матчу
     */
    protected Match(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance,
            MatchKind kind
    ) {
        this(
                validateTeam(homeTeam),
                validateTeam(awayTeam),
                validateNonNegative(homeScore, "рахунок"),
                validateNonNegative(awayScore, "рахунок"),
                validateNonNegative(attendance, "відвідуваність"),
                LocalDate.of(1970, 1, 1), 0, validateKind(kind), true
        );
    }

    protected Match(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance,
            LocalDate matchDate,
            int round,
            MatchKind kind
    ) {
        this(
                validateTeam(homeTeam),
                validateTeam(awayTeam),
                validateNonNegative(homeScore, "рахунок"),
                validateNonNegative(awayScore, "рахунок"),
                validateNonNegative(attendance, "відвідуваність"),
                Objects.requireNonNull(matchDate, "Дата матчу не може бути null."),
                validateNonNegative(round, "тур"),
                validateKind(kind),
                true
        );
    }

    private Match(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance,
            LocalDate matchDate,
            int round,
            MatchKind kind,
            boolean validated
    ) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.attendance = attendance;
        this.matchDate = matchDate;
        this.round = round;
        this.kind = kind;
    }

    private static String validateTeam(String team) {
        if (team == null || team.isBlank()) {
            throw new IllegalArgumentException(
                    "назва домашньої або гостьової команди порожня."
            );
        }
        return team;
    }

    private static int validateNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    field + " не може бути від'ємним."
            );
        }
        return value;
    }

    private static MatchKind validateKind(MatchKind kind) {
        return Objects.requireNonNull(
                kind, "Категорія матчу не може бути null."
        );
    }

    /**
     * Створює відповідний підтип матчу з CSV-рядка.
     *
     * <p>П'ять полів зберігають формат лабораторної роботи №2
     * і трактуються як ліговий матч. Шосте поле може містити
     * {@code LEAGUE} або {@code PLAYOFF}. Вісім полів додають дату,
     * тур і відвідуваність для потокових запитів лабораторної №4.</p>
     *
     * @param line рядок у форматі старого або розширеного CSV
     * @return створений матч
     * @throws IllegalArgumentException якщо формат або значення хибні
     */
    public static Match fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок.");
        }

        String[] fields = line.split(";", -1);
        if (fields.length != 5 && fields.length != 6 && fields.length != 8) {
            throw new IllegalArgumentException(
                    "очікується 5, 6 або 8 полів, отримано "
                            + fields.length + "."
            );
        }

        final int homeScore;
        final int awayScore;
        final int attendance;
        final LocalDate matchDate;
        final int round;
        try {
            if (fields.length == 8) {
                matchDate = LocalDate.parse(fields[2]);
                homeScore = Integer.parseInt(fields[3]);
                awayScore = Integer.parseInt(fields[4]);
                round = Integer.parseInt(fields[5]);
                attendance = Integer.parseInt(fields[6]);
            } else {
                matchDate = LocalDate.of(1970, 1, 1);
                round = 0;
                homeScore = Integer.parseInt(fields[2]);
                awayScore = Integer.parseInt(fields[3]);
                attendance = Integer.parseInt(fields[4]);
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "числове поле має неправильний формат.",
                    exception
            );
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "дата матчу має неправильний формат YYYY-MM-DD.",
                    exception
            );
        }

        MatchKind kind = fields.length == 5
                ? MatchKind.LEAGUE
                : parseKind(fields.length == 6 ? fields[5] : fields[7]);

        return kind == MatchKind.LEAGUE
                ? new LeagueMatch(
                        fields[0], fields[1], homeScore, awayScore, attendance,
                        matchDate, round
                )
                : new PlayoffMatch(
                        fields[0], fields[1], homeScore, awayScore, attendance,
                        matchDate, round
                );
    }

    private static MatchKind parseKind(String value) {
        try {
            return MatchKind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "категорія матчу має бути LEAGUE або PLAYOFF.",
                    exception
            );
        }
    }

    /** @return назва домашньої команди */
    public final String homeTeam() {
        return homeTeam;
    }

    /** @return назва гостьової команди */
    public final String awayTeam() {
        return awayTeam;
    }

    /** @return рахунок домашньої команди */
    public final int homeScore() {
        return homeScore;
    }

    /** @return рахунок гостьової команди */
    public final int awayScore() {
        return awayScore;
    }

    /** @return кількість глядачів */
    public final int attendance() {
        return attendance;
    }

    /** @return категорія матчу */
    public final MatchKind kind() {
        return kind;
    }

    /** @return дата матчу */
    public final LocalDate matchDate() {
        return matchDate;
    }

    /** @return номер туру */
    public final int round() {
        return round;
    }

    /** @return абсолютна різниця між голами команд */
    public final int goalDifference() {
        return Math.abs(homeScore - awayScore);
    }

    /**
     * Обчислює очки господарів за правилами підтипу.
     *
     * @return кількість очок господарів
     */
    public abstract int homePoints();

    /**
     * Обчислює очки гостей за правилами підтипу.
     *
     * @return кількість очок гостей
     */
    public abstract int awayPoints();

    /** @return загальний рахунок і відвідуваність */
    public final ScoreAttendance scoreAttendance() {
        return new ScoreAttendance(
                (long) homeScore + awayScore,
                attendance
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Match match = (Match) other;
        return homeScore == match.homeScore
                && awayScore == match.awayScore
                && attendance == match.attendance
                && round == match.round
                && homeTeam.equals(match.homeTeam)
                && awayTeam.equals(match.awayTeam)
                && matchDate.equals(match.matchDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getClass(), homeTeam, awayTeam, homeScore, awayScore, attendance,
                matchDate, round
        );
    }

    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "%s{homeTeam='%s', awayTeam='%s', homeScore=%d, "
                        + "awayScore=%d, attendance=%d}",
                getClass().getSimpleName(),
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                attendance
        );
    }
}
