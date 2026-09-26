import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Тести для класу ReportGenerator.
 */
class ReportGeneratorTest {

    @Test
    void shouldCreateReport() {
        ReportResult result = new ReportResult(
                3,
                8,
                12500,
                31200,
                List.of()
        );

        String report = ReportGenerator.createReport(result);

        assertTrue(report.contains("Коректних записів: 3"));
        assertTrue(report.contains("Середня кількість голів: 2.67"));
        assertTrue(report.contains("Найбільша відвідуваність: 12500"));
        assertTrue(report.contains("Сумарна відвідуваність: 31200"));
        assertTrue(report.contains("Немає помилок."));
    }

    @Test
    void shouldShowErrorsInReport() {
        ReportResult result = new ReportResult(
                0,
                0,
                0,
                0,
                List.of(
                        "Рядок 1: числове поле має неправильний формат."
                )
        );

        String report = ReportGenerator.createReport(result);

        assertTrue(
                report.contains(
                        "Рядок 1: числове поле має неправильний формат."
                )
        );
    }

    @Test
    void shouldHandleZeroValidRecords() {
        ReportResult result = new ReportResult(
                0,
                0,
                0,
                0,
                List.of()
        );

        String report = ReportGenerator.createReport(result);

        assertTrue(
                report.contains("Середня кількість голів: 0.00")
        );
    }
}