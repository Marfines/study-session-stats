# Study Session Stats

Консольная программа на Java 17: читает JSON с учебными сессиями,
считает число занятий, общую длительность и минуты по каждому предмету.
Работает на Windows, Linux и macOS. Код создан специально для задания.

## Сборка и запуск

Требуются JDK 17+ и Maven 3.9+.

```sh
mvn --batch-mode --no-transfer-progress clean verify
java -jar target/study-session-stats.jar examples/sessions.json
```

Результат:

```text
Sessions: 4
Total minutes: 175
CI/CD: 40 min
Git: 30 min
Java: 105 min
```

Формат входа: массив объектов с непустым строковым `subject` и целым
числом `minutes` от 1 до 1440. Пустой массив допустим. Ошибочные данные
дают код возврата 1, неправильное число аргументов - 2.
Gson 2.13.2 разбирает JSON; JUnit Jupiter 5.13.4 проверяет программу.
Maven Shade включает runtime-зависимости в исполняемый JAR.

## GitHub Actions

Workflow: [ci-cd.yml](.github/workflows/ci-cd.yml).
События: каждый `push` (включая теги), `pull_request`, ручной `workflow_dispatch`.

* `build`: матрица Ubuntu, Windows, macOS; установка Temurin 17;
  восстановление кэша Maven; `clean verify`; запуск готового JAR;
  загрузка JAR и отчётов тестов в Artifacts на 30 дней.
* `deploy`: после успеха всей матрицы, только для push тега `v*`;
  загрузка проверенного JAR и публикация в GitHub Releases.
  Для CLI-программы это доставка готового дистрибутива пользователю.

`actions/setup-java` кэширует `~/.m2/repository`. Ключ зависит от ОС,
архитектуры, менеджера зависимостей и хэша `pom.xml`. При совпадении ключа
ранее загруженные зависимости восстанавливаются. Первый запуск, изменение
`pom.xml` или удаление кэша потребуют скачивания зависимостей.
Кэш не является артефактом: он ускоряет сборку, а Artifacts содержит её результат.

Во вкладке **Actions** откройте успешный запуск и скачайте
`study-session-stats-<os>` из раздела **Artifacts** (нужен вход в GitHub).
Распакуйте ZIP и запустите JAR с Java 17+. Готовые версии также доступны
в **Releases**, без ограничения срока хранения артефактов workflow.

Для выпуска версии:

```sh
git tag v1.0.0
git push origin v1.0.0
```

Используются `actions/checkout@v6`, `actions/setup-java@v5`,
`actions/upload-artifact@v4`, `actions/download-artifact@v5` и GitHub CLI.
Права по умолчанию `contents: read`; только `deploy` получает `contents: write`.
Пользовательские секреты не требуются: публикация использует `GITHUB_TOKEN`.
