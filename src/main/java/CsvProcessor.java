import java.util.ArrayList;
import java.util.List;

/**
 * Клас для обробки даних спортивної ліги.
 */
public final class CsvProcessor {

    private CsvProcessor() {
    }

    /**
     * Обробляє рядки CSV-файла.
     *
     * @param lines рядки вхідного файла
     * @return результати обробки
     */
    public static ReportResult processLines(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Match> matches = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = lines.get(index);

            if (line.isBlank()) {
                errors.add("Рядок " + lineNumber + ": порожній рядок.");
                continue;
            }

            try {
                matches.add(Match.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                errors.add(
                        "Рядок " + lineNumber + ": "
                                + exception.getMessage()
                );
            }
        }

        List<ScoreAttendance> scoreAttendance = matches.stream()
                .map(Match::scoreAttendance)
                .toList();

        return new ReportResult(
                matches.size(),
                scoreAttendance.stream()
                        .mapToInt(ScoreAttendance::score)
                        .sum(),
                scoreAttendance.stream()
                        .mapToInt(ScoreAttendance::attendance)
                        .max()
                        .orElse(0),
                scoreAttendance.stream()
                        .mapToInt(ScoreAttendance::attendance)
                        .sum(),
                errors
        );
    }
}