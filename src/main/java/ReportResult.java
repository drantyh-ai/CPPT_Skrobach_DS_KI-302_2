import java.util.List;

/**
 * Зберігає результати обробки даних спортивної ліги.
 *
 * @param validCount кількість коректних записів
 * @param totalGoals сумарна кількість голів
 * @param maxAttendance найбільша відвідуваність
 * @param totalAttendance сумарна відвідуваність
 * @param errors список помилок
 */
public record ReportResult(
        int validCount,
        int totalGoals,
        int maxAttendance,
        int totalAttendance,
        List<String> errors
) {

    public ReportResult {
        errors = List.copyOf(errors);
    }
}