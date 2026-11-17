# Лабораторна робота №5

## Параметризоване сховище, винятки й CSV-експорт

**Курс:** Кросплатформні засоби програмування  
**Варіант:** 17 — спортивна ліга
**Мова:** Java 21  
**Система збірки:** Maven  
**Версія:** 5.0.0
**Операційна система розробки:** Windows

## Мета роботи

Доповнити попередній Java-проєкт типобезпечним сховищем,
власним винятком, runtime-анотаціями CSV, універсальним рефлексивним
експортером, документним CSV-парсером та перевіркою round-trip:

```text
об'єкти → Repository<T> → CSV UTF-8 → об'єкти → equals
```

Попередня предметна область, варіант 17, модель `Match`, потокові запити
та старий CSV-формат збережені.

## Модель варіанта 17

Для CSV-обміну використовується незмінний клас `MatchRecord`:

| Поле | Тип | Правило |
|---|---|---|
| `homeTeam` | `String` | непорожня назва |
| `awayTeam` | `String` | непорожня назва |
| `date` | `LocalDate` | не `null` |
| `homeScore` | `int` | невід'ємне |
| `awayScore` | `int` | невід'ємне |
| `round` | `int` | невід'ємний |

Ключ round-trip:

```text
homeTeam + awayTeam + date
```

`equals` і `hashCode` враховують усі поля запису.

## Repository<T>

Клас [`Repository<T>`](src/main/java/Repository.java) реалізує:

- `add(T item)` — додає непорожній запис;
- `all()` — повертає незмінний знімок;
- `find(Predicate<? super T>)` — виконує пошук за умовою.

Raw types не використовуються. Внутрішній список недоступний для прямої
модифікації ззовні.

## CSV-анотація

[`CsvColumn`](src/main/java/CsvColumn.java) має:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
```

Анотовані поля `MatchRecord` експортуються з такими заголовками:

| Поле | CSV-заголовок |
|---|---|
| `homeTeam` | `господарі` |
| `awayTeam` | `гості` |
| `date` | `дата` |
| `homeScore` | `рахунок господарів` |
| `awayScore` | `рахунок гостей` |
| `round` | `тур` |

## Формат CSV

Повний заголовок:

```text
господарі,гості,дата,рахунок господарів,рахунок гостей,тур
```

Звичайний запис:

```text
Динамо,Шахтар,2026-03-10,2,0,1
```

Запис із комою та лапками:

```text
"Динамо, ""старе""",Шахтар,2026-03-10,2,0,1
```

Значення з комою, подвійною лапкою або перенесенням рядка береться в
подвійні лапки. Внутрішня подвійна лапка подвоюється.

## Рефлексивний експортер

[`CsvExporter`](src/main/java/CsvExporter.java) є універсальним і не
містить умов для `MatchRecord`, `Match`, `LeagueMatch` або `PlayoffMatch`.
Він:

1. знаходить поля з `@CsvColumn` через рефлексію;
2. перевіряє доступ через `trySetAccessible()`;
3. формує заголовок із метаданих анотацій;
4. використовує один стабільний порядок полів для заголовка і рядків;
5. екранує CSV-значення;
6. записує файл через `Files.writeString`;
7. використовує `StandardCharsets.UTF_8`;
8. застосовує `TRUNCATE_EXISTING`, щоб старі рядки не залишалися.

Приклад виклику:

```java
CsvExporter.write(path, MatchRecord.class, records);
```

Для порожнього списку `Class<T>` потрібен, щоб побудувати заголовок без
першого об'єкта.

## CSV-парсер та імпорт

[`CsvParser`](src/main/java/CsvParser.java) розбирає весь документ, а не
окремі фізичні рядки. Він підтримує:

- кому всередині лапок;
- подвійні лапки;
- перенесення рядка всередині поля;
- CRLF і LF як роздільники записів;
- перевірку незакритих або некоректних лапок.

`split(",")` для загального CSV непридатний, бо не розрізняє кому між
полями та кому всередині екранованого значення.

[`MatchRecord`](src/main/java/MatchRecord.java) відповідає за предметну
валідацію та відновлення типів через:

- `fromCsv(String line)`;
- `fromFields(List<String> fields)`;
- `readAll(Path path)`.

## Обробка помилок

[`DataStorageException`](src/main/java/DataStorageException.java) є
checked exception і має конструктор із першопричиною:

```java
DataStorageException(String message, Throwable cause)
```

| Рівень | Приклад | Реакція |
|---|---|---|
| Модель | порожня команда | `IllegalArgumentException` |
| Модель | від'ємний рахунок або тур | `IllegalArgumentException` |
| Формат | неправильна кількість полів | `DataStorageException` |
| Значення | неправильна дата або число | `DataStorageException` із `cause` |
| CSV | незакриті лапки | `DataStorageException` |
| Enum | невідомий `MatchKind` у попередній моделі | `IllegalArgumentException` |
| Файл | відсутній або недоступний файл | `DataStorageException` із `IOException` у `cause` |
| Рефлексія | немає доступу або CSV-полів | `DataStorageException` |

Помилки не поглинаються порожнім `catch`, а передаються з контекстом.

## Round-trip

Round-trip перевіряє, що після експорту та імпорту зберігаються:

- порядок записів;
- назви команд;
- дата;
- рахунки;
- тур;
- коми, лапки та перенесення рядків у текстових полях.

Порівняння виконується через:

```java
assertEquals(original, restored);
```

Порожній CSV і CSV лише із заголовком є допустимими випадками.

## Структура проєкту

- [`Repository.java`](src/main/java/Repository.java) — generic-сховище;
- [`CsvColumn.java`](src/main/java/CsvColumn.java) — CSV-анотація;
- [`CsvExporter.java`](src/main/java/CsvExporter.java) — рефлексивний експортер;
- [`CsvParser.java`](src/main/java/CsvParser.java) — документний CSV-парсер;
- [`DataStorageException.java`](src/main/java/DataStorageException.java) — власний виняток;
- [`MatchRecord.java`](src/main/java/MatchRecord.java) — модель варіанта 17;
- [`StorageTest.java`](src/test/java/StorageTest.java) — тести лабораторної №5;
- `Match.java`, `LeagueMatch.java`, `PlayoffMatch.java` — попередня модель;
- `MatchStreamQueries.java` — потокові запити попередньої роботи.

## Тестування

Запуск усіх тестів і SpotBugs:

```powershell
.\mvnw.cmd -B clean verify
```

Фінальний результат:

```text
37 tests run
Failures: 0
Errors: 0
Skipped: 0
SpotBugs: 0 errors
BUILD SUCCESS
```

Перевірено:

- generic-сховище та незмінний знімок;
- пошук за `Predicate`;
- runtime-анотації;
- заголовок CSV;
- UTF-8;
- коми, лапки та multiline-поля;
- порожній експорт;
- неправильну кількість полів;
- неправильне число;
- неправильну дату;
- неправильний `enum`;
- відсутній файл;
- `cause`;
- round-trip.

## Збірка і запуск

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B verify
.\mvnw.cmd -B package
java -jar target\cppt-labs.jar --version
```

Очікувана версія:

```text
version 5.0.0
```

## CI та артефакти

Workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) запускає
`verify` на:

- Ubuntu;
- Windows;
- macOS.

Після успішного виконання JAR публікується як artifact для кожної ОС.

- [CI run #54](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/actions/runs/44086932846)
- [CI workflow](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/actions/workflows/ci.yml)
- [Ubuntu artifact](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/actions/runs/44086932846)
- [Windows artifact](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/actions/runs/44086932846)
- [macOS artifact](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/actions/runs/44086932846)

## Версія і Git

- [Git tag v5.0.0](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/tree/v5.0.0)
- [Pull Request #42](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/pull/42)
- [Issue #36 — Repository<T>](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/issues/36)
- [Issue #37 — CSV metadata annotation](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/issues/37)
- [Issue #38 — Reflective CSV exporter](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/issues/38)
- [Issue #39 — CSV parser and import](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/issues/39)
- [Issue #40 — Tests and error handling](https://github.com/drantyh-ai/CPPT_Skrobach_DS_KI-302_2/issues/40)

## Академічна доброчесність

GitHub Copilot у Visual Studio Code використовувався як консультативний
інструмент для структури Java-коду, тестів і документації. Автор визначив
варіант, модель, інваріанти, формат CSV та перевірив код локально й через CI.

## Сумісність із попередніми роботами

Попередня модель спортивної ліги, потокові запити, старі тести та старий
CSV-формат збережені. Лабораторна №5 додає новий незалежний формат
`MatchRecord` для збереження та відновлення даних через CSV.

## Висновок

Проєкт відповідає вимогам лабораторної роботи №5: має generic-сховище,
runtime-анотації, універсальний рефлексивний експортер, коректний CSV-парсер,
власний виняток із `cause`, round-trip, тести, executable JAR, CI на трьох
ОС і GitHub-артефакти.
