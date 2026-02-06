
# Ignite SQL UI (Embedded + External) — Spring Boot

Веб‑приложение на Spring Boot для выполнения SQL запросов и диагностики Apache Ignite через Thin JDBC.  
Поддерживает работу как с внешним Ignite‑кластером, так и во встроенном (embedded) режиме.

Проект предоставляет REST API + Web UI + Swagger документацию в одном fat‑jar.

---

## 🚀 Возможности

- Выполнение SQL запросов к Ignite через Thin JDBC
- Ограничение количества строк результата (limitRows)
- Таймаут выполнения запросов
- Диагностика кластера через системные таблицы
- Embedded Ignite режим для локального тестирования
- REST API + Swagger UI
- Встроенный Web UI (HTML + JS)
- Светлая и тёмная тема
- Опциональная аутентификация (Keycloak / fakelogin / none)
- HTTPS поддержка
- Single JAR deployment

---

## 🧱 Архитектура

UI (HTML/JS) → REST Controllers → Service Layer → JDBC Thin Driver → Ignite Cluster

### Основные компоненты

**Controllers**
- SqlApiController — выполнение SQL
- IgniteDiagController — диагностика
- ReadyController — health check

**Services**
- SqlService — JDBC выполнение SQL + limitRows
- IgniteDiagService — системные таблицы Ignite

**Ignite**
- EmbeddedIgniteManager — lifecycle embedded Ignite

---

## 📋 Требования

- JDK 11+
- Maven 3.6+
- Spring Boot 2.7.x
- Apache Ignite 2.16.x

Опционально:
- Keycloak (OIDC)
- H2 (для indexing в Ignite)

---

## 📦 Сборка

```bash
git clone https://github.com/bmixdev/ignite-sql-ui-embedded-project.git
cd ignite-sql-ui-embedded-project
mvn clean package
```

Результат:

```
target/ignite-sql-ui-*.jar
```

---

## ▶ Запуск

### Внешний Ignite

```
java -jar target/ignite-sql-ui.jar
```

По умолчанию:

```
jdbc:ignite:thin://127.0.0.1:10800
server.port=8080
```

Переопределение:

```
java -jar ignite-sql-ui.jar   --app.ignite.jdbcUrl=jdbc:ignite:thin://host:10800   --app.ignite.queryTimeoutSeconds=30   --app.ignite.maxRows=1000   --server.port=9090
```

---

### Embedded Ignite

```
java -jar ignite-sql-ui.jar   --app.ignite.embedded.enabled=true
```

Windows:

```
java -jar ignite-sql-ui.jar ^
  --app.ignite.embedded.enabled=true ^
  --app.ignite.embedded.workDir=D:\ignite-work
```

---

## 🌐 Web UI

```
http://localhost:8080/
```

Вкладки:

- Diagnostics — узлы, кэши, схемы, активные запросы
- SQL — выполнение запросов
- Ctrl+Enter — выполнить SQL
- Кнопка темы — переключение light/dark
- Тема сохраняется в LocalStorage

---

## 🔌 REST API

### POST /api/sql

```
{
  "sql": "SELECT * FROM SYS.NODES"
}
```

SELECT ответ:

```
{
  "ok": true,
  "columns": [...],
  "rows": [...],
  "elapsedMs": 12
}
```

DML/DDL ответ:

```
{
  "ok": true,
  "updateCount": 1
}
```

С limitRows:

```
POST /api/sql?limitRows=200
```

---

### GET /api/ignite/overview

Возвращает:

- nodes
- caches
- schemas
- activeQueries

---

### GET /api/ready

Health check + SELECT 1 + режим запуска.

---

## 🧪 Примеры SQL

```
SELECT * FROM SYS.NODES
SELECT * FROM SYS.CACHES
SELECT * FROM SYS.SQL_QUERIES
SELECT * FROM SYS.SCHEMAS
```

---

## 🧰 curl примеры

Linux/macOS:

```
curl -X POST http://localhost:8080/api/sql   -H "Content-Type: application/json"   -d '{"sql":"SELECT 1"}'
```

Windows:

```
curl -X POST http://localhost:8080/api/sql ^
  -H "Content-Type: application/json" ^
  -d "{"sql":"SELECT 1"}"
```

Если включён fakelogin и приходит 302 redirect:

```
curl -L ...
```

---

## 🔐 Аутентификация

```
app.security.mode = none | fakelogin | keycloak
```

### fakelogin

Для тестов. Не требует внешнего провайдера.

### Keycloak

```
app:
  security:
    mode: keycloak
```

Настраивается через spring.security.oauth2.client.

---

## 🔒 HTTPS

Создать keystore:

```
keytool -genkeypair   -alias ignite-ui   -storetype PKCS12   -keystore ignite-ui.p12   -storepass changeit
```

Запуск:

```
java -jar ignite-sql-ui.jar   --server.port=8443   --server.ssl.enabled=true   --server.ssl.key-store=file:/path/ignite-ui.p12   --server.ssl.key-store-password=changeit
```

---

## ⚙ Конфигурация application.yml

```
app:
  ignite:
    jdbcUrl: jdbc:ignite:thin://127.0.0.1:10800
    queryTimeoutSeconds: 10
    maxRows: 500
    embedded:
      enabled: false
      instanceName: ignite-embedded
      workDir: ignite-work
      thinPort: 10800
```

---

## 🐛 Отладка

```
curl http://localhost:8080/api/ready
curl http://localhost:8080/api/ignite/overview
```

Debug логирование:

```
logging.level.com.example=DEBUG
```

---

## ⚠ Типовые проблемы

Connection refused → Ignite не запущен  
Invalid workDir → указать абсолютный путь  
Query timeout → увеличить queryTimeoutSeconds  
Port busy → сменить server.port

---

## 🗺 Roadmap

- История запросов
- CSV/Excel экспорт
- Query explain
- Real‑time обновление
- UI multi‑node discovery

---

## 📄 License

Apache License 2.0

---

Made by bmixdev
