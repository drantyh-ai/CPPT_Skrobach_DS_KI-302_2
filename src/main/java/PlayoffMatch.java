import java.time.LocalDate;

/**
 * Матч плей-оф.
 *
 * <p>У плей-оф переможець отримує три очки, а команда,
 * що програла, не отримує очок. Нічия не приносить очок,
 * оскільки переможець плей-оф визначається додатково.</p>
 */
public final class PlayoffMatch extends Match {

    /**
     * Створює матч плей-оф.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність
     */
    public PlayoffMatch(
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            int attendance
    ) {
        super(
                homeTeam, awayTeam, homeScore, awayScore, attendance,
                MatchKind.PLAYOFF
        );
    }

    /**
     * Створює матч плей-оф із датою та номером туру.
     *
     * @param homeTeam домашня команда
     * @param awayTeam гостьова команда
     * @param homeScore рахунок домашньої команди
     * @param awayScore рахунок гостьової команди
     * @param attendance відвідуваність
     * @param matchDate дата матчу
     * @param round номер туру
     */
    public PlayoffMatch(
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
                matchDate, round, MatchKind.PLAYOFF
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
        return 0;
    }
}
