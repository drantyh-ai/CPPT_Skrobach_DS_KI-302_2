import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
        configureConsoleEncoding();

        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }

                case "--version" -> {
                    System.out.println("version 3.0.0");
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

            System.out.print(report);
            System.out.println();

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
                java -jar target/cppt-labs.jar [--help] [--version]
                    [--input <файл>] [--output <файл>]

                --help              показати довідку
                --version           показати версію програми
                --input <файл>      шлях до вхідного CSV-файла
                --output <файл>     шлях до файла звіту
                """);
    }

    /**
     * Встановлює UTF-8 для виводу українського тексту в консоль Windows.
     */
    private static void configureConsoleEncoding() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                Process process = new ProcessBuilder(
                        "cmd.exe",
                        "/c",
                        "chcp 65001 > nul"
                ).inheritIO().start();

                if (process.waitFor() != 0) {
                    throw new IllegalStateException(
                            "Не вдалося встановити кодову сторінку UTF-8."
                    );
                }
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Не вдалося налаштувати кодування консолі.",
                        exception
                );
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Налаштування кодування консолі перервано.",
                        exception
                );
            }
        }

        System.setOut(
                new PrintStream(
                        new FileOutputStream(FileDescriptor.out),
                        true,
                        StandardCharsets.UTF_8
                )
        );
    }
}