import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Тести для класу CsvProcessor.
 */
class CsvProcessorTest {

    @Test
    void shouldProcessValidRecords() {
        List<String> lines = List.of(
                "Динамо;Шахтар;2;1;12500",
                "Карпати;Зоря;1;1;8500",
                "Полісся;Верес;3;0;10200"
        );

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(3, result.validCount());
        assertEquals(8, result.totalGoals());
        assertEquals(12500, result.maxAttendance());
        assertEquals(31200, result.totalAttendance());
        assertEquals(0, result.errors().size());
    }

    @Test
    void shouldDetectWrongNumberOfFields() {
        List<String> lines = List.of(
                "Динамо;Шахтар;2;1"
        );

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(0, result.validCount());
        assertEquals(1, result.errors().size());
    }

    @Test
    void shouldDetectInvalidNumber() {
        List<String> lines = List.of(
                "Динамо;Шахтар;abc;1;10000"
        );

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(0, result.validCount());
        assertEquals(1, result.errors().size());
    }

    @Test
    void shouldDetectNegativeValues() {
        List<String> lines = List.of(
                "Динамо;Шахтар;-1;2;10000"
        );

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(0, result.validCount());
        assertEquals(1, result.errors().size());
    }

    @Test
    void shouldDetectEmptyTeamName() {
        List<String> lines = List.of(
                ";Шахтар;2;1;10000"
        );

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(0, result.validCount());
        assertEquals(1, result.errors().size());
    }

    @Test
    void shouldDetectEmptyLine() {
        List<String> lines = List.of("");

        ReportResult result = CsvProcessor.processLines(lines);

        assertEquals(0, result.validCount());
        assertEquals(1, result.errors().size());
    }
}