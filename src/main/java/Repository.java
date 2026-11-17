import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище записів у пам'яті.
 *
 * @param <T> тип записів
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /** Додає непорожній запис до сховища. */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null."));
    }

    /** Повертає незмінний знімок записів. */
    public List<T> all() {
        return List.copyOf(items);
    }

    /** Повертає незмінний список записів, що відповідають умові. */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова пошуку не може бути null.");
        return items.stream().filter(condition).toList();
    }
}
