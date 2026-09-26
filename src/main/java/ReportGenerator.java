import java.util.Locale;

/**
 * Формує текстовий звіт за результатами обробки.
 */
public final class ReportGenerator {

    private ReportGenerator() {
    }

    /**
     * Формує звіт.
     *
     * @param result результати обробки
     * @return текст звіту
     */
    public static String createReport(ReportResult result) {
        double averageGoals;

        if (result.validCount() == 0) {
            averageGoals = 0.0;
        } else {
            averageGoals =
                    (double) result.totalGoals() / result.validCount();
        }

        StringBuilder report = new StringBuilder();

        report.append("ЗВІТ: СПОРТИВНА ЛІГА")
                .append(System.lineSeparator());

        report.append("----------------------------------------")
                .append(System.lineSeparator());

        report.append(
                "Коректних записів: %d%n"
                        .formatted(result.validCount())
        );

        report.append(
                String.format(
                        Locale.ROOT,
                        "Середня кількість голів: %.2f%n",
                        averageGoals
                )
        );

        report.append(
                "Найбільша відвідуваність: %d%n"
                        .formatted(result.maxAttendance())
        );

        report.append(
                "Сумарна відвідуваність: %d%n"
                        .formatted(result.totalAttendance())
        );

        report.append(System.lineSeparator());
        report.append("Помилки:")
                .append(System.lineSeparator());

        if (result.errors().isEmpty()) {
            report.append("Немає помилок.")
                    .append(System.lineSeparator());
        } else {
            for (String error : result.errors()) {
                report.append(error)
                        .append(System.lineSeparator());
            }
        }

        return report.toString();
    }
}