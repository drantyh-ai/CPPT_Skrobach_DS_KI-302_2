/**
 * Допоміжний набір показників одного матчу.
 *
 * @param score загальна кількість голів у матчі
 * @param attendance відвідуваність матчу
 */
public record ScoreAttendance(int score, int attendance) {

    public ScoreAttendance {
        if (score < 0 || attendance < 0) {
            throw new IllegalArgumentException(
                    "рахунок або відвідуваність не можуть бути від'ємними."
            );
        }
    }
}
