import java.util.Locale;

/**
 * Незмінна сутність матчу спортивної ліги.
 */
public final class Match {

    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;
    private final int attendance;

    /**
     * Створює матч і перевіряє його предметні обмеження.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність матчу
     */
    public Match(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance
    ) {
        if (homeTeam == null || homeTeam.isBlank()
                || awayTeam == null || awayTeam.isBlank()) {
            throw new IllegalArgumentException(
                    "назва домашньої або гостьової команди порожня."
            );
        }
        if (homeScore < 0 || awayScore < 0 || attendance < 0) {
            throw new IllegalArgumentException(
                    "рахунок або відвідуваність не можуть бути від'ємними."
            );
        }

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.attendance = attendance;
    }

    /**
     * Створює матч з одного CSV-рядка.
     *
     * @param line рядок у форматі home;away;homeScore;awayScore;attendance
     * @return створена сутність матчу
     * @throws IllegalArgumentException якщо формат або значення рядка хибні
     */
    public static Match fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок.");
        }

        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                    "очікується 5 полів, отримано " + fields.length + "."
            );
        }

        final int homeScore;
        final int awayScore;
        final int attendance;
        try {
            homeScore = Integer.parseInt(fields[2]);
            awayScore = Integer.parseInt(fields[3]);
            attendance = Integer.parseInt(fields[4]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "числове поле має неправильний формат.",
                    exception
            );
        }

        return new Match(
                fields[0],
                fields[1],
                homeScore,
                awayScore,
                attendance
        );
    }

    /** @return назва домашньої команди */
    public String homeTeam() {
        return homeTeam;
    }

    /** @return назва гостьової команди */
    public String awayTeam() {
        return awayTeam;
    }

    /** @return рахунок домашньої команди */
    public int homeScore() {
        return homeScore;
    }

    /** @return рахунок гостьової команди */
    public int awayScore() {
        return awayScore;
    }

    /** @return кількість глядачів */
    public int attendance() {
        return attendance;
    }

    /**
     * Повертає допоміжні показники матчу.
     *
     * @return загальний рахунок і відвідуваність
     */
    public ScoreAttendance scoreAttendance() {
        return new ScoreAttendance(homeScore + awayScore, attendance);
    }

    /**
     * Повертає локалізовано-незалежне текстове представлення матчу.
     *
     * @return форматований опис матчу
     */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "Match{homeTeam='%s', awayTeam='%s', homeScore=%d, "
                        + "awayScore=%d, attendance=%d}",
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                attendance
        );
    }
}
