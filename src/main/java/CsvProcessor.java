import java.util.ArrayList;
import java.util.List;

/**
 * Клас для обробки даних спортивної ліги.
 */
public final class CsvProcessor {

    private static final int EXPECTED_FIELDS = 5;

    private CsvProcessor() {
    }

    /**
     * Обробляє рядки CSV-файла.
     *
     * @param lines рядки вхідного файла
     * @return результати обробки
     */
    public static ReportResult processLines(List<String> lines) {
        int validCount = 0;
        int totalGoals = 0;
        int maxAttendance = 0;
        int totalAttendance = 0;

        List<String> errors = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = lines.get(index);

            if (line.isBlank()) {
                errors.add("Рядок " + lineNumber + ": порожній рядок.");
                continue;
            }

            String[] fields = line.split(";", -1);

            if (fields.length != EXPECTED_FIELDS) {
                errors.add(
                        "Рядок " + lineNumber
                                + ": очікується 5 полів, отримано "
                                + fields.length + "."
                );
                continue;
            }

            if (fields[0].isBlank() || fields[1].isBlank()) {
                errors.add(
                        "Рядок " + lineNumber
                                + ": назва домашньої або гостьової команди порожня."
                );
                continue;
            }

            try {
                int homeScore = Integer.parseInt(fields[2]);
                int awayScore = Integer.parseInt(fields[3]);
                int attendance = Integer.parseInt(fields[4]);

                if (homeScore < 0 || awayScore < 0 || attendance < 0) {
                    errors.add(
                            "Рядок " + lineNumber
                                    + ": рахунок або відвідуваність "
                                    + "не можуть бути від'ємними."
                    );
                    continue;
                }

                int goals = homeScore + awayScore;

                validCount++;
                totalGoals += goals;
                totalAttendance += attendance;
                maxAttendance = Math.max(maxAttendance, attendance);

            } catch (NumberFormatException exception) {
                errors.add(
                        "Рядок " + lineNumber
                                + ": числове поле має неправильний формат."
                );
            }
        }

        return new ReportResult(
                validCount,
                totalGoals,
                maxAttendance,
                totalAttendance,
                errors
        );
    }
}