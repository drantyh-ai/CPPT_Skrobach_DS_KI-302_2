import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Універсально експортує анотовані поля плоских об'єктів у CSV. */
public final class CsvExporter {
    private CsvExporter() {
    }

    /**
     * Записує заголовок і записи у UTF-8.
     *
     * @param <T> тип записів
     * @param path шлях до CSV-файла
     * @param type клас записів
     * @param records записи для експорту
     * @throws DataStorageException якщо відсутні поля, доступ або файл
     */
    public static <T> void write(
            Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(path, "Шлях не може бути null.");
        Objects.requireNonNull(type, "Тип не може бути null.");
        Objects.requireNonNull(records, "Записи не можуть бути null.");

        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас не має анотованих CSV-полів.");
        }

        StringBuilder csv = new StringBuilder();
        appendRow(csv, fields.stream()
                .map(field -> field.getAnnotation(CsvColumn.class).value())
                .toList());
        for (T record : records) {
            if (record == null) {
                throw new DataStorageException("Список містить null-запис.");
            }
            List<Object> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(field.get(record));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле " + field.getName(),
                            exception);
                }
            }
            appendRow(csv, values);
        }

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    path,
                    csv.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new DataStorageException(
                    "Не вдалося записати CSV: " + path, exception);
        }
    }

    private static <T> List<Field> annotatedFields(Class<T> type)
            throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    if (!field.trySetAccessible()) {
                        throw new DataStorageException(
                                "Немає доступу до поля " + field.getName());
                    }
                    fields.add(field);
                }
            }
        }
        fields.sort(Comparator.comparing(Field::getName));
        return List.copyOf(fields);
    }

    private static void appendRow(StringBuilder csv, List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(values.get(i)));
        }
        csv.append(System.lineSeparator());
    }

    private static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",") || text.contains("\"")
                || text.contains("\r") || text.contains("\n");
        return quoted ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }
}
