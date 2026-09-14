package ua.lpnu.kzp.lab01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Консольна програма для обробки даних спортивної ліги.
 *
 * <p>Формат одного запису:
 * home;away;homeScore;awayScore;attendance</p>
 */
public final class Lab01 {

    private static final int EXPECTED_FIELDS = 5;

    /**
     * Забороняє створення екземплярів службового класу.
     */
    private Lab01() {
    }

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

        // Обробка аргументів командного рядка.
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }
                case "--version" -> {
                    System.out.println("lab01 version 1.0.0");
                    return;
                }
                case "--input" -> {
                    if (i + 1 >= args.length) {
                        System.out.println("Помилка: після --input потрібно вказати шлях.");
                        return;
                    }
                    input = Path.of(args[++i]);
                }
                case "--output" -> {
                    if (i + 1 >= args.length) {
                        System.out.println("Помилка: після --output потрібно вказати шлях.");
                        return;
                    }
                    output = Path.of(args[++i]);
                }
                default -> {
                    System.out.println("Невідомий аргумент: " + args[i]);
                    System.out.println("Використайте --help.");
                    return;
                }
            }
        }

        try {
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            ReportResult result = processLines(lines);

            String report = createReport(result);

            System.out.println(report);

            Path outputParent = output.getParent();
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(output, report, StandardCharsets.UTF_8);

            System.out.println("Звіт записано у: " + output);

        } catch (IOException exception) {
            System.out.println("Помилка роботи з файлом: " + exception.getMessage());
        }
    }

    /**
     * Показує довідку щодо запуску програми.
     */
    private static void printHelp() {
        System.out.println("""
                Використання:
                java -jar lab01.jar [--help] [--version]
                    [--input <файл>] [--output <файл>]

                --help              показати довідку
                --version           показати версію програми
                --input <файл>      шлях до вхідного CSV-файла
                --output <файл>     шлях до файла звіту
                """);
    }

    /**
     * Обробляє всі рядки вхідного файла.
     *
     * @param lines рядки CSV-файла
     * @return результати обробки
     */
    private static ReportResult processLines(List<String> lines) {
        int validCount = 0;
        int totalGoals = 0;
        int maxAttendance = 0;
        int totalAttendance = 0;

        List<String> errors = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = lines.get(index);

            if (line.isBlank()) {
                errors.add("Рядок " + lineNumber + ": порожній рядок.");
                continue;
            }

            String[] fields = line.split(";", -1);

            if (fields.length != EXPECTED_FIELDS) {
                errors.add("Рядок " + lineNumber + ": очікується 5 полів, отримано " + fields.length + ".");
                continue;
            }

            // Перевірка текстових полів.
            if (fields[0].isBlank() || fields[1].isBlank()) {
                errors.add("Рядок " + lineNumber + ": назва домашньої або гостьової команди порожня.");
                continue;
            }

            try {
                int homeScore = Integer.parseInt(fields[2]);
                int awayScore = Integer.parseInt(fields[3]);
                int attendance = Integer.parseInt(fields[4]);

                // Від'ємні значення недопустимі.
                if (homeScore < 0 || awayScore < 0 || attendance < 0) {
                    errors.add("Рядок " + lineNumber + ": рахунок або відвідуваність не можуть бути від'ємними.");
                    continue;
                }

                int goals = homeScore + awayScore;

                validCount++;
                totalGoals += goals;
                totalAttendance += attendance;
                maxAttendance = Math.max(maxAttendance, attendance);

            } catch (NumberFormatException exception) {
                errors.add("Рядок " + lineNumber + ": числове поле має неправильний формат.");
            }
        }

        return new ReportResult(
                validCount,
                totalGoals,
                maxAttendance,
                totalAttendance,
                errors
        );
    }

    /**
     * Формує текстовий звіт.
     *
     * @param result результати обробки
     * @return готовий звіт
     */
    private static String createReport(ReportResult result) {
        double averageGoals;
        if(result.validCount() == 0){
            averageGoals = 0.0;
        }
        else{
            averageGoals = (double) result.totalGoals() / result.validCount();
        }

        StringBuilder report = new StringBuilder();

        report.append("ЗВІТ: СПОРТИВНА ЛІГА").append(System.lineSeparator());
        report.append("----------------------------------------").append(System.lineSeparator());

        report.append("Коректних записів: %d%n".formatted(result.validCount()));

        report.append(
            String.format(Locale.ROOT, "Середня кількість голів: %.2f%n", averageGoals));

        report.append("Найбільша відвідуваність: %d%n".formatted(result.maxAttendance()));

        report.append("Сумарна відвідуваність: %d%n".formatted(result.totalAttendance()));

        report.append(System.lineSeparator());
        report.append("Помилки:").append(System.lineSeparator());

        if (result.errors().isEmpty()) {
            report.append("Немає помилок.").append(System.lineSeparator());
        } else {
            for (String error : result.errors()) {
                report.append("%s%n".formatted(error));
            }
        }

        return report.toString();
    }

    /**
     * Зберігає результати обробки.
     *
     * @param validCount кількість коректних записів
     * @param totalGoals сумарна кількість голів
     * @param maxAttendance найбільша відвідуваність
     * @param totalAttendance сумарна відвідуваність
     * @param errors список помилок
     */
    private record ReportResult(
            int validCount,
            int totalGoals,
            int maxAttendance,
            int totalAttendance,
            List<String> errors
    ) 
    {
    }
}