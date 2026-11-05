import java.time.LocalDate;

/**
 * Матч регулярного чемпіонату.
 */
public final class LeagueMatch extends Match {

    /**
     * Створює ліговий матч.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність
     */
    public LeagueMatch(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance
    ) {
        super(
                homeTeam, awayTeam, homeScore, awayScore, attendance,
                MatchKind.LEAGUE
        );
    }

    /**
     * Створює ліговий матч із датою та номером туру.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність
     * @param matchDate дата матчу
     * @param round номер туру
     */
    public LeagueMatch(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance,
            LocalDate matchDate,
            int round
    ) {
        super(
                homeTeam, awayTeam, homeScore, awayScore, attendance,
                matchDate, round, MatchKind.LEAGUE
        );
    }

    @Override
    public int homePoints() {
        return pointsFor(homeScore(), awayScore());
    }

    @Override
    public int awayPoints() {
        return pointsFor(awayScore(), homeScore());
    }

    private static int pointsFor(int ownScore, int opponentScore) {
        if (ownScore > opponentScore) {
            return 3;
        }
        return ownScore == opponentScore ? 1 : 0;
    }
}
