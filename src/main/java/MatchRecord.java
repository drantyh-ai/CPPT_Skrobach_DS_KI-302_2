import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Плоский запис матчу спортивної ліги для CSV-обміну, варіант 17. */
public final class MatchRecord {
    @CsvColumn("господарі")
    private final String homeTeam;
    @CsvColumn("гості")
    private final String awayTeam;
    @CsvColumn("дата")
    private final LocalDate date;
    @CsvColumn("рахунок господарів")
    private final int homeScore;
    @CsvColumn("рахунок гостей")
    private final int awayScore;
    @CsvColumn("тур")
    private final int round;

    /** Створює перевірений запис матчу. */
    public MatchRecord(
            String homeTeam,
            String awayTeam,
            LocalDate date,
            int homeScore,
            int awayScore,
            int round
    ) {
        this.homeTeam = validateTeam(homeTeam);
        this.awayTeam = validateTeam(awayTeam);
        this.date = Objects.requireNonNull(date, "Дата матчу не може бути null.");
        this.homeScore = validateNonNegative(homeScore, "Рахунок");
        this.awayScore = validateNonNegative(awayScore, "Рахунок");
        this.round = validateNonNegative(round, "Тур");
    }

    /**
     * Відновлює запис з одного CSV-рядка.
     *
     * @param line CSV-рядок у форматі MatchRecord
     * @return відновлений запис
     * @throws DataStorageException якщо рядок має неправильний формат
     */
    public static MatchRecord fromCsv(String line)
            throws DataStorageException {
        return fromFields(CsvParser.parse(line));
    }

    /**
     * Відновлює запис із полів у порядку CSV.
     *
     * @param fields поля в порядку CSV-заголовка
     * @return відновлений запис
     * @throws DataStorageException якщо кількість або значення полів хибні
     */
    public static MatchRecord fromFields(List<String> fields)
            throws DataStorageException {
        if (fields.size() != 6) {
            throw new DataStorageException(
                    "Для MatchRecord очікується 6 CSV-полів, отримано "
                            + fields.size() + ".");
        }
        try {
            return new MatchRecord(
                    fields.get(0),
                    fields.get(1),
                    LocalDate.parse(fields.get(2)),
                    Integer.parseInt(fields.get(3)),
                    Integer.parseInt(fields.get(4)),
                    Integer.parseInt(fields.get(5))
            );
        } catch (RuntimeException exception) {
            throw new DataStorageException(
                    "Некоректний CSV-запис матчу.", exception);
        }
    }

    /**
     * Читає CSV-файл разом із заголовком і відновлює записи.
     *
     * @param path шлях до CSV-файла
     * @return відновлені записи
     * @throws DataStorageException якщо файл або запис неможливо прочитати
     */
    public static List<MatchRecord> readAll(java.nio.file.Path path)
            throws DataStorageException {
        try {
            List<List<String>> rows = CsvParser.parseDocument(
                    java.nio.file.Files.readString(
                            path, java.nio.charset.StandardCharsets.UTF_8));
            if (rows.isEmpty()) {
                return List.of();
            }
            if (rows.get(0).size() != 6) {
                throw new DataStorageException(
                        "Заголовок MatchRecord має містити 6 стовпців.");
            }
            return rows.subList(1, rows.size()).stream()
                    .map(fields -> {
                        try {
                            return fromFields(fields);
                        } catch (DataStorageException exception) {
                            throw new RecordImportException(exception);
                        }
                    })
                    .toList();
        } catch (java.io.IOException exception) {
            throw new DataStorageException(
                    "Не вдалося прочитати CSV: " + path, exception);
        } catch (RecordImportException exception) {
            throw exception.cause;
        }
    }

    /** @return назва команди господарів */
    public String homeTeam() {
        return homeTeam;
    }

    /** @return назва команди гостей */
    public String awayTeam() {
        return awayTeam;
    }

    /** @return дата матчу */
    public LocalDate date() {
        return date;
    }

    /** @return рахунок команди господарів */
    public int homeScore() {
        return homeScore;
    }

    /** @return рахунок команди гостей */
    public int awayScore() {
        return awayScore;
    }

    /** @return номер туру */
    public int round() {
        return round;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MatchRecord match)) {
            return false;
        }
        return homeScore == match.homeScore && awayScore == match.awayScore
                && round == match.round && homeTeam.equals(match.homeTeam)
                && awayTeam.equals(match.awayTeam) && date.equals(match.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                homeTeam, awayTeam, date, homeScore, awayScore, round);
    }

    private static String validateTeam(String team) {
        if (team == null || team.isBlank()) {
            throw new IllegalArgumentException("Назва команди не може бути порожньою.");
        }
        return team;
    }

    private static int validateNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " не може бути від'ємним.");
        }
        return value;
    }

    private static final class RecordImportException
            extends RuntimeException {
        private final DataStorageException cause;

        private RecordImportException(DataStorageException cause) {
            this.cause = cause;
        }
    }
}
