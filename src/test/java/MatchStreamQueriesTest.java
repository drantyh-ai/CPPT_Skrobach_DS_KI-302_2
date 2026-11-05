import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Тести потокових запитів лабораторної роботи №4.
 */
class MatchStreamQueriesTest {

    private static List<Match> matches() {
        return List.of(
                match("Динамо", "Шахтар", "2026-03-10", 2, 0, 1),
                match("Карпати", "Зоря", "2026-03-11", 1, 1, 1),
                match("Полісся", "Верес", "2026-03-12", 4, 0, 2),
                match("Рух", "Олександрія", "2026-03-13", 3, 1, 2),
                match("Динамо", "Шахтар", "2026-03-20", 0, 2, 3),
                match("Львів", "Ворскла", "2026-03-21", 2, 1, 4)
        );
    }

    private static Match match(
            String home,
            String away,
            String date,
            int homeScore,
            int awayScore,
            int round
    ) {
        return new LeagueMatch(
                home,
                away,
                homeScore,
                awayScore,
                1000,
                LocalDate.parse(date),
                round
        );
    }

    @Test
    void shouldFilterByRoundWithoutMutatingInput() {
        List<Match> input = new ArrayList<>(matches());
        List<Match> result = MatchStreamQueries.byRound(input, 2);

        assertEquals(2, result.size());
        assertEquals(6, input.size());
    }

    @Test
    void shouldMapScoresToText() {
        assertEquals(
                List.of("2:0", "1:1", "4:0", "3:1", "0:2", "2:1"),
                MatchStreamQueries.scoreTexts(matches())
        );
    }

    @Test
    void shouldGroupMatchesByRound() {
        assertEquals(
                Map.of(1, 2L, 2, 2L, 3, 1L, 4, 1L),
                MatchStreamQueries.countByRound(matches())
        );
    }

    @Test
    void shouldCalculateHomeGoalsStatisticsAndEmptyStatistics() {
        assertEquals(
                12,
                MatchStreamQueries.homeGoalsStatistics(matches()).getSum()
        );
        assertEquals(
                0,
                MatchStreamQueries.homeGoalsStatistics(List.of()).getCount()
        );
    }

    @Test
    void shouldSortTopMatchesByDifferenceAndThenDate() {
        List<Match> result =
                MatchStreamQueries.topByGoalDifference(matches(), 5);

        assertEquals(5, result.size());
        assertEquals("Полісся", result.get(0).homeTeam());
        assertEquals("Динамо", result.get(1).homeTeam());
        assertEquals(
                "Рух",
                MatchStreamQueries.topByGoalDifference(matches(), 5)
                        .get(2)
                        .homeTeam()
        );
        assertTrue(
                MatchStreamQueries.topByGoalDifference(matches(), 0).isEmpty()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> MatchStreamQueries.topByGoalDifference(matches(), -1)
        );
    }

    @Test
    void shouldFindExistingAndMissingTeamPair() {
        Optional<Match> found = MatchStreamQueries.findByTeams(
                matches(), "Динамо", "Шахтар"
        );
        Optional<Match> missing = MatchStreamQueries.findByTeams(
                matches(), "Металіст", "Інгулець"
        );

        assertTrue(found.isPresent());
        assertEquals("2026-03-10", found.orElseThrow().matchDate().toString());
        assertFalse(missing.isPresent());
    }
}
