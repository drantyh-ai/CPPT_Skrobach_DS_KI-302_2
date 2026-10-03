import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Тести предметної сутності матчу.
 */
class MatchTest {

    @Test
    void shouldCreateMatchAndExposeScoreAttendance() {
        Match match = new Match("Динамо", "Шахтар", 2, 1, 12500);

        assertEquals("Динамо", match.homeTeam());
        assertEquals("Шахтар", match.awayTeam());
        assertEquals(new ScoreAttendance(3, 12500), match.scoreAttendance());
    }

    @Test
    void shouldCreateMatchFromCsv() {
        Match match = Match.fromCsv("Динамо;Шахтар;2;1;12500");

        assertEquals(new ScoreAttendance(3, 12500), match.scoreAttendance());
    }

    @Test
    void shouldRejectMalformedCsv() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Match.fromCsv("Динамо;Шахтар;2;1")
        );

        assertTrue(exception.getMessage().contains("очікується 5 полів"));
    }

    @Test
    void shouldRejectNonNumericCsvValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Match.fromCsv("Динамо;Шахтар;abc;1;12500")
        );
    }

    @Test
    void shouldRejectEmptyTeamName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Match("", "Шахтар", 2, 1, 12500)
        );
    }

    @Test
    void shouldRejectNegativeMatchValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Match("Динамо", "Шахтар", -1, 1, 12500)
        );
    }

    @Test
    void shouldAllowBoundaryZeroValues() {
        Match match = new Match("Динамо", "Шахтар", 0, 0, 0);

        assertEquals(new ScoreAttendance(0, 0), match.scoreAttendance());
    }

    @Test
    void shouldKeepScoreAttendanceAsValue() {
        ScoreAttendance first = new ScoreAttendance(3, 12500);
        ScoreAttendance second = new ScoreAttendance(3, 12500);

        assertEquals(first, second);
        assertEquals(3, first.score());
        assertEquals(12500, first.attendance());
    }

    @Test
    void shouldFormatMatchWithStableToString() {
        Match match = new Match("Динамо", "Шахтар", 2, 1, 12500);

        assertEquals(
                "Match{homeTeam='Динамо', awayTeam='Шахтар', "
                        + "homeScore=2, awayScore=1, attendance=12500}",
                match.toString()
        );
    }
}
