import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Тести параметризованого сховища та CSV round-trip лабораторної №5. */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void repositoryProvidesSnapshotAndPredicateSearch() {
        Repository<MatchRecord> repository = new Repository<>();
        MatchRecord leagueMatch = match("Динамо", "Шахтар", "2026-03-10", 2);
        repository.add(leagueMatch);

        assertEquals(List.of(leagueMatch),
                repository.find(item -> item.round() == 2));
        List<MatchRecord> snapshot = repository.all();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
    }

    @Test
    void exporterEscapesCommaQuotesAndRestoresRecords() throws Exception {
        List<MatchRecord> original = List.of(
                match("Динамо, \"старе\"", "Шахтар", "2026-03-10", 1),
                match("Карпати", "Зоря", "2026-03-11", 2)
        );
        Path path = temporaryDirectory.resolve("matches.csv");

        CsvExporter.write(path, MatchRecord.class, original);
        String csv = Files.readString(path, StandardCharsets.UTF_8);
        List<MatchRecord> restored = MatchRecord.readAll(path);

        assertTrue(csv.contains("\"Динамо, \"\"старе\"\"\""));
        assertTrue(csv.startsWith(
                "господарі,гості,дата,рахунок господарів,рахунок гостей,тур"
                        + System.lineSeparator()));
        assertEquals(original, restored);
    }

    @Test
    void parserKeepsCommaAndNewlineInsideQuotedField() throws Exception {
        String document = "a,b,c\n\"team, \"\"A\"\"\",\"line\nvalue\",3\n";

        List<List<String>> rows = CsvParser.parseDocument(document);

        assertEquals(List.of("team, \"A\"", "line\nvalue", "3"), rows.get(1));
    }

    @Test
    void exporterWritesHeaderForEmptyInputAndUsesPlatformSeparator()
            throws Exception {
        Path path = temporaryDirectory.resolve("empty.csv");

        CsvExporter.write(path, MatchRecord.class, List.of());
        String csv = Files.readString(path, StandardCharsets.UTF_8);

        assertEquals(
                "господарі,гості,дата,рахунок господарів,рахунок гостей,тур"
                        + System.lineSeparator(),
                csv);
        assertEquals(1, CsvParser.parseDocument(csv).size());
        assertTrue(csv.endsWith(System.lineSeparator()));
    }

    @Test
    void rejectsWrongFieldCountAndInvalidNumber() {
        DataStorageException wrongCount = assertThrows(
                DataStorageException.class,
                () -> MatchRecord.fromFields(List.of(
                        "Динамо", "Шахтар", "2026-03-10")));
        DataStorageException invalidNumber = assertThrows(
                DataStorageException.class,
                () -> MatchRecord.fromCsv(
                        "Динамо,Шахтар,2026-03-10,not-a-number,0,1"));

        assertTrue(wrongCount.getMessage().contains("6 CSV-полів"));
        assertTrue(invalidNumber.getCause() instanceof NumberFormatException);
    }

    @Test
    void invalidRecordRetainsCause() {
        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> MatchRecord.fromCsv("Динамо,Шахтар,not-a-date,1,0,1"));

        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    void missingFileRetainsIoCause() {
        Path missing = temporaryDirectory.resolve("missing.csv");

        DataStorageException exception = assertThrows(
                DataStorageException.class, () -> MatchRecord.readAll(missing));

        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    private static MatchRecord match(
            String home, String away, String date, int round) {
        return new MatchRecord(
                home, away, LocalDate.parse(date), 2, 0, round);
    }
}
