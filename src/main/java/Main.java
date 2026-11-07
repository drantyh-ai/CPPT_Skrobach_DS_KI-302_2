import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

        CommandLineOptions options = new CommandLineOptions();
        Arrays.stream(args).forEach(options::accept);

        if (options.shouldStop()) {
            if (options.helpRequested()) {
                printHelp();
            } else if (options.versionRequested()) {
                System.out.println("version " + applicationVersion());
            } else {
                System.out.println(options.errorMessage());
                System.out.println("Використайте --help.");
            }
            return;
        }

        try {
            List<String> lines =
                    Files.readAllLines(options.input(), StandardCharsets.UTF_8);
            ReportResult result = CsvProcessor.processLines(lines);
            String report = ReportGenerator.createReport(result);

            System.out.print(report);
            System.out.println();

            Path outputParent = options.output().getParent();
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(
                    options.output(),
                    report,
                    StandardCharsets.UTF_8
            );

            System.out.println("Звіт записано у: " + options.output());
        } catch (IOException exception) {
            System.out.println(
                    "Помилка роботи з файлом: " + exception.getMessage()
            );
        }
    }

    private static String applicationVersion() {
        Properties properties = new Properties();
        try (var stream = Main.class.getClassLoader()
                .getResourceAsStream("version.properties")) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Ресурс версії не знайдено."
                );
            }
            properties.load(stream);
            return properties.getProperty("app.version");
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не вдалося прочитати версію програми.",
                    exception
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
                        "cmd.exe", "/c", "chcp 65001 > nul"
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

    private static final class CommandLineOptions {
        private Path input = Path.of("data", "input.csv");
        private Path output = Path.of("out", "report.txt");
        private String pendingOption;
        private String errorMessage;
        private boolean helpRequested;
        private boolean versionRequested;

        private void accept(String argument) {
            if (errorMessage != null || helpRequested || versionRequested) {
                return;
            }

            if (pendingOption != null) {
                setPath(argument);
                pendingOption = null;
                return;
            }

            switch (argument) {
                case "--help" -> helpRequested = true;
                case "--version" -> versionRequested = true;
                case "--input", "--output" -> pendingOption = argument;
                default -> errorMessage = "Невідомий аргумент: " + argument;
            }
        }

        private void setPath(String value) {
            if ("--input".equals(pendingOption)) {
                input = Path.of(value);
            } else {
                output = Path.of(value);
            }
        }

        private boolean shouldStop() {
            if (pendingOption != null && errorMessage == null) {
                errorMessage = "Помилка: після " + pendingOption
                        + " потрібно вказати шлях.";
            }
            return helpRequested || versionRequested || errorMessage != null;
        }

        private boolean helpRequested() {
            return helpRequested;
        }

        private boolean versionRequested() {
            return versionRequested;
        }

        private String errorMessage() {
            return errorMessage;
        }

        private Path input() {
            return input;
        }

        private Path output() {
            return output;
        }
    }
}
