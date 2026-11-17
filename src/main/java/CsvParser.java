import java.util.ArrayList;
import java.util.List;

/** Розбирає CSV-документи з RFC-сумісним екрануванням. */
public final class CsvParser {
    private CsvParser() {
    }

    /** Розбирає один CSV-запис. */
    public static List<String> parse(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV-запис не може бути null.");
        }
        return parseRecord(text, false);
    }

    /**
     * Розбирає весь документ, не вважаючи перенос рядка в лапках межею запису.
     */
    public static List<List<String>> parseDocument(String document)
            throws DataStorageException {
        if (document == null) {
            throw new DataStorageException("CSV-документ не може бути null.");
        }
        if (document.isEmpty()) {
            return List.of();
        }

        List<List<String>> rows = new ArrayList<>();
        StringBuilder record = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < document.length(); i++) {
            char current = document.charAt(i);
            if (current == '"') {
                record.append(current);
                if (quoted && i + 1 < document.length()
                        && document.charAt(i + 1) == '"') {
                    record.append(document.charAt(++i));
                } else {
                    quoted = !quoted;
                }
            } else if ((current == '\n' || current == '\r') && !quoted) {
                if (current == '\r' && i + 1 < document.length()
                        && document.charAt(i + 1) == '\n') {
                    i++;
                }
                if (record.length() > 0) {
                    rows.add(parseRecord(record.toString(), true));
                    record.setLength(0);
                }
            } else {
                record.append(current);
            }
        }
        if (quoted) {
            throw new DataStorageException("Незакриті лапки у CSV-документі.");
        }
        if (record.length() > 0) {
            rows.add(parseRecord(record.toString(), true));
        }
        return List.copyOf(rows);
    }

    private static List<String> parseRecord(String text, boolean documentRecord)
            throws DataStorageException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (quoted) {
                if (current == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == '"') {
                if (field.length() > 0 || closedQuote) {
                    throw malformed();
                }
                quoted = true;
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (closedQuote && !Character.isWhitespace(current)) {
                throw malformed();
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw malformed();
        }
        fields.add(field.toString());
        return List.copyOf(fields);
    }

    private static DataStorageException malformed() {
        return new DataStorageException("Некоректне CSV-екранування.");
    }
}
