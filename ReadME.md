# ValidatorGen (IntelliJ IDEA plugin) — Architecture & Extension Guide (Step 1–3)

Документ описывает текущую архитектуру проекта после шагов **1–3** (Java + Gradle), объясняет логику работы, принятые решения и паттерны, а также даёт детальные инструкции по расширению функционала (добавление новых операторов, изменение UI, добавление модальных окон и т.д.).

> Цель архитектуры: сохранить текущее поведение прототипа, но разнести код на слои так, чтобы было **легко расширять** UI, проверки и кодогенерацию.

---

## 1. Общая идея: слои и зависимости

Проект организован по принципам **Clean Architecture / Hexagonal (Ports & Adapters)** и **MVP (Presenter + Passive View)**.

### 1.1. Слои (пакеты) и правила зависимостей


- **domain** — ядро: модели и интерфейсы (контракты). Не зависит ни от Swing, ни от PSI, ни от файловой системы.
- **application** — сценарии (use-cases): что происходит при нажатии кнопок и вызове команд.
- **presentation** — Presenter + View интерфейс: связывает UI и use-case’ы.
- **infrastructure** — адаптеры к платформе: IntelliJ PSI, сохранение в файлы, clipboard.
- **ui** — Swing-компоненты и отображение (ToolWindow, Panel, TableModel).

**Ключевой принцип:** UI не должен знать о PSI и о генерации напрямую, он общается через Presenter и use-case’ы.

---

## 2. Текущая структура проекта

### 2.1. Domain (`com.vkr.validatorgen.domain`)

**Назначение:** описать предметную область (правила, DTO спецификация) и объявить контракты для внешних зависимостей.

#### Модели
- `CompareOp` — перечисление операторов сравнения (пока только `GT(">")`).
- `CompareRule` — доменная модель одного условия:
    - `left`, `op`, `right` — сравнение
    - `target` — на какое поле “вешаем” ошибку (path)
    - `message` — текст ошибки
- `DtoSpec` — спецификация DTO для генерации:
    - `packageName`, `className`
    - `getterNames` — список методов, чтобы выбрать `dto.getX()` или `dto.x`
    - `intFields` — список `int` полей, для заполнения комбобоксов UI

#### Контракты (Ports)
- `DtoParser` — `parse(javaText) -> DtoSpec?` (парсинг DTO текста).
- `CodeGenerator` — `generate(dto, rules) -> String` (генерация исходника).
- `RuleRepository` — хранение/обновление правил.
- `ClipboardService` — копирование в буфер обмена.
- `GeneratedCodeSaver` — сохранение кода в нужную директорию.

> Почему это важно: domain не зависит от IntelliJ APIs, его можно тестировать отдельно.

---

### 2.2. Infrastructure (`com.vkr.validatorgen.infrastructure`)

**Назначение:** реализовать доменные контракты на конкретной платформе (IntelliJ IDEA, ОС).

#### Реализации
- `PsiDtoParser implements DtoParser`
    - использует IntelliJ PSI (`PsiFileFactory`, `PsiJavaFile`, `PsiClass`)
    - извлекает пакет, имя класса, `getterNames`, `intFields`
    - работает в `ReadAction` (корректно с PSI)
- `JavaValidatorGenerator implements CodeGenerator`
    - генерирует Java-код класса `*GeneratedValidator`
    - строит `if (!(left op right)) { violations.add(...) }`
    - формирует `Violation record` + `ruleId`
- `InMemoryRuleRepository implements RuleRepository`
    - хранит правила в `List` (простой прототип)
- `AwtClipboardService implements ClipboardService`
    - копирует текст в системный буфер (AWT)
- `DefaultGeneratedCodeSaver implements GeneratedCodeSaver`
    - сохраняет в `generated-sources/validator/<packagePath>/<ClassName>.java`

> Infrastructure — это “грязный слой” с зависимостями на IDE/OS/FS. Его легко заменить на другой.

---

### 2.3. Application (`com.vkr.validatorgen.application`)

**Назначение:** инкапсулировать сценарии (use-cases). Каждый use-case — небольшая автономная операция.

#### Use-cases
- `RefreshFieldsUseCase`
    - вызывает `DtoParser`
    - возвращает список `intFields` или ошибку
- `AddRuleUseCase`
    - валидирует `RuleDraft` (A/B/Target/op/message)
    - создаёт `CompareRule` и добавляет в `RuleRepository`
- `RemoveRuleUseCase`
    - удаляет правило по индексу
- `GenerateCodeUseCase`
    - проверяет, что правила не пустые
    - парсит DTO через `DtoParser`
    - генерирует код через `CodeGenerator`
- `CopyGeneratedCodeUseCase`
    - проверяет, что код не пустой
    - вызывает `ClipboardService.copy(code)`
- `SaveGeneratedCodeUseCase`
    - проверяет, что код не пустой
    - парсит DTO (чтобы вычислить output path)
    - вызывает `GeneratedCodeSaver.save(dto, code)`

> Почему use-case’ы: UI не должен содержать бизнес-логику. Use-case’ы удобно тестировать, переиспользовать и расширять.

---

### 2.4. Presentation (`com.vkr.validatorgen.presentation`)

**Назначение:** “прослойка” между UI и application.

#### Классы
- `ValidatorGenView` — контракт UI:
    - отдаёт данные (dto текст, draft, selection, generated code)
    - отображает результаты (fields/output/code)
    - сообщает, что таблицу надо перерисовать
- `RuleDraft` — структура данных из UI (может содержать null).
- `ValidatorGenPresenter` — обработчик событий UI:
    - вызывает нужный use-case
    - интерпретирует результат
    - обновляет view

> Это MVP: Presenter координирует, View — пассивно отображает.

---

### 2.5. UI (`com.vkr.plugin.ui`)

**Назначение:** отображение и взаимодействие пользователя.

#### Классы
- `ValidatorGenPanel implements ValidatorGenView`
    - строит UI (EditorTextField, JTable, кнопки, tabs)
    - связывает listeners → `presenter.onXxx()`
    - реализует методы view: показать поля, текст, код, обновить таблицу
- `RulesTableModel extends AbstractTableModel`
    - отображает `RuleRepository` как таблицу
    - поддерживает inline редактирование ячеек (как в прототипе)

> UI слой максимально тонкий: никакого PSI, никакого “куда сохранять”, никакой генерации строк.

---

## 3. Паттерны и принципы (почему именно так)

### 3.1. Separation of Concerns (SoC)
Каждый слой решает свою задачу:
- UI — отображение и сбор ввода
- Presenter — реакция на события
- Use-case — сценарий/команда
- Domain — модели и контракты
- Infrastructure — реализация контрактов

### 3.2. Ports & Adapters (Hexagonal)
Domain объявляет интерфейсы:
- `DtoParser`, `CodeGenerator`, `GeneratedCodeSaver`, etc.

Infrastructure реализует их:
- `PsiDtoParser`, `JavaValidatorGenerator`, `DefaultGeneratedCodeSaver`

Это позволяет:
- заменить реализацию без изменения use-case’ов и presenter’а
- легко мокать зависимости в тестах

### 3.3. MVP (Passive View)
UI реализует `ValidatorGenView` и не содержит логики “что делать”.
Presenter управляет потоком команд и обновлением UI.

### 3.4. Dependency Inversion
Высокоуровневая логика (use-case’ы) зависит не от деталей (PSI/FS), а от интерфейсов (ports).

---

## 4. Текущая логика работы (по кнопкам)

### 4.1. Refresh fields
1. UI → `presenter.onRefreshFields()`
2. Presenter → `view.getDtoText()`
3. Use-case `RefreshFieldsUseCase.execute(dtoText)`
4. `DtoParser.parse(dtoText)` (через `PsiDtoParser`) возвращает `DtoSpec`
5. Use-case возвращает `fields` или ошибку
6. Presenter → `view.showFields(fields)`, `view.showOutput(...)`

---

### 4.2. Add rule
1. UI → `presenter.onAddRule()`
2. Presenter → `view.getRuleDraft()`
3. `AddRuleUseCase.execute(draft)`:
    - валидирует поля + message
    - создаёт `CompareRule`
    - добавляет в `RuleRepository`
4. Presenter → `view.refreshRulesTable()`, `view.showOutput(...)`

---

### 4.3. Remove rule
1. UI → `presenter.onRemoveRule()`
2. Presenter → `view.getSelectedRuleIndex()`
3. `RemoveRuleUseCase.execute(index)` → удаляет
4. Presenter → `refreshRulesTable()`, `showOutput(...)`

---

### 4.4. Generate code
1. UI → `presenter.onGenerateCode()`
2. `GenerateCodeUseCase.execute(dtoText)`:
    - проверяет `repo.all()` не пусто
    - парсит `DtoSpec`
    - генерирует `String code`
3. Presenter → `view.showGeneratedCode(code)`, `view.showOutput(...)`

---

### 4.5. Copy / Save
- Copy: `CopyGeneratedCodeUseCase` + `ClipboardService`
- Save: `SaveGeneratedCodeUseCase` + `GeneratedCodeSaver`

---

## 5. Расширение функционала: детальные примеры

## 5.1. Пример: добавить новый знак сравнения (`>=`)

Сейчас поддерживается только `>` (`CompareOp.GT`). Добавление нового оператора требует согласованных изменений в нескольких местах.

### Шаг 1 — Domain: расширяем `CompareOp`
Файл: `com.vkr.validatorgen.domain.CompareOp`

Добавить:
```java
GE(">=")
