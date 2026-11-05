import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Потокові запити для спортивної ліги, варіант 17.
 */
public final class MatchStreamQueries {

    private MatchStreamQueries() {
    }

    /** @return матчі заданого туру */
    public static List<Match> byRound(List<Match> matches, int round) {
        return matches.stream()
                .filter(match -> match.round() == round)
                .toList();
    }

    /** @return рахунки у форматі «господарі:гості» */
    public static List<String> scoreTexts(List<Match> matches) {
        return matches.stream()
                .map(match -> match.homeScore() + ":" + match.awayScore())
                .toList();
    }

    /** @return кількість матчів у кожному турі */
    public static Map<Integer, Long> countByRound(List<Match> matches) {
        return matches.stream()
                .collect(Collectors.groupingBy(
                        Match::round,
                        Collectors.counting()
                ));
    }

    /** @return статистика кількості голів господарів */
    public static IntSummaryStatistics homeGoalsStatistics(
            List<Match> matches
    ) {
        return matches.stream()
                .mapToInt(Match::homeScore)
                .summaryStatistics();
    }

    /**
     * Повертає не більше N матчів із найбільшою різницею рахунку.
     * За однакової різниці раніший матч має вищий пріоритет.
     *
     * @param matches вхідні матчі
     * @param limit максимальна кількість результатів
     * @return відсортований список матчів
     */
    public static List<Match> topByGoalDifference(
            List<Match> matches,
            int limit
    ) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним.");
        }

        return matches.stream()
                .sorted(
                        Comparator.comparingInt(Match::goalDifference)
                                .reversed()
                                .thenComparing(Match::matchDate)
                )
                .limit(limit)
                .toList();
    }

    /** @return перший матч між заданою парою команд */
    public static Optional<Match> findByTeams(
            List<Match> matches,
            String homeTeam,
            String awayTeam
    ) {
        return matches.stream()
                .filter(match -> match.homeTeam().equals(homeTeam))
                .filter(match -> match.awayTeam().equals(awayTeam))
                .findFirst();
    }
}
