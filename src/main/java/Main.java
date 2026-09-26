import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Головний клас програми.
 *
 * <p>Консольна програма для обробки даних спортивної ліги.</p>
 */
public final class Main {

    private Main() {
    }

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

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
                        System.out.println(
                                "Помилка: після --input потрібно "
                                        + "вказати шлях."
                        );
                        return;
                    }

                    input = Path.of(args[++i]);
                }

                case "--output" -> {
                    if (i + 1 >= args.length) {
                        System.out.println(
                                "Помилка: після --output потрібно "
                                        + "вказати шлях."
                        );
                        return;
                    }

                    output = Path.of(args[++i]);
                }

                default -> {
                    System.out.println(
                            "Невідомий аргумент: " + args[i]
                    );
                    System.out.println("Використайте --help.");
                    return;
                }
            }
        }

        try {
            List<String> lines =
                    Files.readAllLines(input, StandardCharsets.UTF_8);

            ReportResult result =
                    CsvProcessor.processLines(lines);

            String report =
                    ReportGenerator.createReport(result);

            System.out.println(report);

            Path outputParent = output.getParent();

            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(
                    output,
                    report,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Звіт записано у: " + output
            );

        } catch (IOException exception) {
            System.out.println(
                    "Помилка роботи з файлом: "
                            + exception.getMessage()
            );
        }
    }

    /**
     * Виводить довідку щодо використання програми.
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
}