# SuperFurnaceMod

[![Build SuperFurnaceMod](https://github.com/d8873045-a11y/SuperFurnaceMod/actions/workflows/build.yml/badge.svg)](https://github.com/d8873045-a11y/SuperFurnaceMod/actions/workflows/build.yml)

Мод для **Minecraft 1.12.2 (Forge)**, добавляющий **Супер-печь** — блок, который плавит предметы **в 3 раза быстрее** обычной печи.

---

## Особенности

| Параметр | Ванильная печь | Супер-печь |
|----------|---------------|-----------|
| Время плавки | 200 тиков (10 сек) | 66 тиков (~3.3 сек) |
| Скорость | 1× | **3×** |
| GUI | Стандартный | Идентичен ванильному |
| Текстуры | — | Ванильные текстуры печи |

---

## Рецепт крафта

```
[ C ][ C ][ C ]
[ C ][ D ][ C ]
[ C ][ C ][ C ]
```

- `C` = Булыжник (Cobblestone)
- `D` = Алмаз (Diamond)

---

## Сборка мода

### Вариант 1 — GitHub Actions (рекомендуется, ничего устанавливать не нужно)

1. Откройте репозиторий на GitHub
2. Перейдите во вкладку **Actions**
3. В левом меню выберите **"Build SuperFurnaceMod"**
4. Нажмите кнопку **"Run workflow"** → **"Run workflow"**
5. Дождитесь завершения (обычно 5–15 минут, первый раз дольше)
6. Скачайте готовый `.jar` из раздела **Artifacts** внизу завершённого прогона

### Вариант 2 — Локальная сборка (Windows / Linux / macOS)

#### Требования
- **Java 8** (JDK 8, не новее!) — [скачать Adoptium Temurin 8](https://adoptium.net/temurin/releases/?version=8)
- **Git**
- Интернет-соединение

#### Шаги

```bash
# 1. Клонировать репозиторий
git clone https://github.com/d8873045-a11y/SuperFurnaceMod.git
cd SuperFurnaceMod

# 2. Настроить Forge-воркспейс (скачивает Minecraft + Forge, ~500 МБ, один раз)
./gradlew setupDecompWorkspace        # Linux / macOS
gradlew.bat setupDecompWorkspace      # Windows

# 3. Собрать JAR
./gradlew build                       # Linux / macOS
gradlew.bat build                     # Windows
```

Готовый файл появится здесь:
```
build/libs/superfurnacemod-1.0.0.jar
```

#### Куда положить JAR

1. Найдите папку `.minecraft`:
   - **Windows**: `%APPDATA%\.minecraft`
   - **macOS**: `~/Library/Application Support/minecraft`
   - **Linux**: `~/.minecraft`
2. Откройте (или создайте) папку `.minecraft/mods/`
3. Скопируйте туда файл `superfurnacemod-1.0.0.jar`
4. Убедитесь, что у вас установлен **Forge 1.12.2** (версия `14.23.5.2860`)
5. Запустите Minecraft через профиль Forge 1.12.2

---

## Установка Forge 1.12.2

1. Скачайте установщик: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html
   - Выберите **14.23.5.2860** (Recommended) → **Installer**
2. Запустите `forge-1.12.2-14.23.5.2860-installer.jar`
3. Выберите "Install client" → OK
4. В лаунчере Minecraft выберите профиль **Forge**

---

## Структура проекта

```
SuperFurnaceMod/
├── .github/workflows/build.yml          ← GitHub Actions (CI/CD)
├── build.gradle                          ← Конфигурация сборки (ForgeGradle 2.3)
├── gradle.properties                     ← Параметры JVM
├── settings.gradle                       ← Имя проекта
├── gradlew / gradlew.bat                 ← Скрипты Gradle Wrapper
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties         ← Gradle 4.9
└── src/main/
    ├── java/com/example/superfurnacemod/
    │   ├── SuperFurnaceMod.java          ← Главный класс (@Mod)
    │   ├── block/BlockSuperFurnace.java  ← Блок (off/on состояния)
    │   ├── tileentity/TileEntitySuperFurnace.java  ← Логика плавки 3×
    │   ├── container/ContainerSuperFurnace.java    ← Инвентарь
    │   ├── gui/GuiSuperFurnace.java      ← GUI (клиент)
    │   ├── gui/GuiHandler.java           ← Обработчик GUI
    │   ├── init/ModBlocks.java           ← Регистрация блоков
    │   └── proxy/                        ← Client/Server прокси
    └── resources/assets/superfurnacemod/
        ├── blockstates/                  ← JSON состояний блока
        ├── models/block/                 ← JSON модели блока
        ├── models/item/                  ← JSON модель предмета
        ├── recipes/                      ← JSON рецепт крафта
        └── lang/                         ← en_us.lang, ru_ru.lang
```

---

## Технические детали

- **Mod ID**: `superfurnacemod`
- **Версия**: `1.0.0`
- **Minecraft**: `1.12.2`
- **Forge**: `14.23.5.2860`
- **ForgeGradle**: `2.3-SNAPSHOT`
- **Gradle**: `4.9`
- **Java**: `8`
