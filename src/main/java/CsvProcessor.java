import java.util.List;
import java.util.stream.IntStream;

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
        List<ProcessingResult> processingResults =
                IntStream.range(0, lines.size())
                        .mapToObj(index -> processLine(index + 1, lines.get(index)))
                        .toList();

        List<Match> matches = processingResults.stream()
                .map(ProcessingResult::match)
                .map(java.util.Optional::ofNullable)
                .flatMap(java.util.Optional::stream)
                .toList();

        List<String> errors = processingResults.stream()
                .map(ProcessingResult::error)
                .map(java.util.Optional::ofNullable)
                .flatMap(java.util.Optional::stream)
                .toList();

        List<ScoreAttendance> scoreAttendance = matches.stream()
                .map(Match::scoreAttendance)
                .toList();

        return new ReportResult(
                matches.size(),
                scoreAttendance.stream()
                        .mapToLong(ScoreAttendance::score)
                        .sum(),
                scoreAttendance.stream()
                        .mapToInt(ScoreAttendance::attendance)
                        .max()
                        .orElse(0),
                scoreAttendance.stream()
                        .mapToLong(ScoreAttendance::attendance)
                        .sum(),
                matches.stream()
                        .mapToLong(match -> (long) match.homePoints()
                                + match.awayPoints())
                        .sum(),
                errors
        );
    }

    private static ProcessingResult processLine(int lineNumber, String line) {
        if (line.isBlank()) {
            return new ProcessingResult(
                    null,
                    "Рядок " + lineNumber + ": порожній рядок."
            );
        }

        try {
            return new ProcessingResult(Match.fromCsv(line), null);
        } catch (IllegalArgumentException exception) {
            return new ProcessingResult(
                    null,
                    "Рядок " + lineNumber + ": "
                            + exception.getMessage()
            );
        }
    }

    private record ProcessingResult(Match match, String error) {
    }
}