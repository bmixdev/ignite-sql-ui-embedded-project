# Ignite SQL UI (Single JAR) + Embedded Ignite + Swagger + Optional Keycloak

Небольшое Spring Boot приложение (Java 11 / Spring Boot 2.7), которое:
- подключается к **Apache Ignite** по **thin JDBC**
- предоставляет **REST API** для выполнения SQL (`/api/sql`)
- показывает результат `SELECT` как **HTML-таблицу** на той же странице + JSON для отладки
- отображает **диагностику Ignite** на главной странице (`SYS.NODES / SYS.CACHES / SYS.SCHEMAS / SYS.SQL_QUERIES`)
- имеет **Swagger UI** (`/swagger-ui.html`)
- умеет поднимать **embedded Ignite внутри самого JAR** для тестирования
- поддерживает **опциональную аутентификацию Keycloak (OIDC)**, **по умолчанию выключено**

---

## Требования

- **JDK 11**
- **Maven 3.6+**
- (опционально) Keycloak (локально/в контуре), если включаете авторизацию

---

## Сборка

```bash
mvn -U clean package
```

Результат:
- `target/ignite-sql-ui-1.1.0.jar` — fat-jar (запускать его)
- `target/ignite-sql-ui-1.1.0.jar.original` — тонкий jar (не нужен)

---

## Запуск

### 1) Обычный режим: подключение к внешнему Ignite

По умолчанию приложение использует thin JDBC:

- `jdbc:ignite:thin://127.0.0.1:10800`

Запуск:

```bash
java -jar target/ignite-sql-ui-1.1.0.jar
```

UI:
- http://localhost:8080/

Swagger:
- http://localhost:8080/swagger-ui.html

---

### 2) Embedded Ignite внутри JAR (для тестирования)

Включение:

```bash
java -jar target/ignite-sql-ui-1.1.0.jar --app.ignite.embedded.enabled=true
```

**Важно (Windows):** `workDir` должен быть **абсолютным**.

```bat
java -jar target\ignite-sql-ui-1.1.0.jar ^
  --app.ignite.embedded.enabled=true ^
  --app.ignite.embedded.workDir=D:\Project\Java\ignite-work
```

---

## Запуск на другом порту (не 8080)

### Через параметр командной строки

```bash
java -jar target/ignite-sql-ui-1.1.0.jar --server.port=9090
```

Можно совместить с embedded Ignite:

```bat
java -jar target\ignite-sql-ui-1.1.0.jar ^
  --server.port=9090 ^
  --app.ignite.embedded.enabled=true ^
  --app.ignite.embedded.workDir=D:\Project\Java\ignite-work
```

### Через `application.yml`

```yaml
server:
  port: 9090
```

---

## HTTPS (без хардкода в jar)

Spring Boot включает HTTPS через keystore. Правильная схема:
- keystore хранится **вне jar**
- пароль передаётся через **env** или secret-хранилище
- путь задаётся через параметры

Пример запуска (Windows):

```bat
set KEYSTORE_PASS=changeit

java -jar target\ignite-sql-ui-1.1.0.jar ^
  --server.port=8443 ^
  --server.ssl.enabled=true ^
  --server.ssl.key-store=file:D:\certs\ignite-ui.p12 ^
  --server.ssl.key-store-type=PKCS12 ^
  --server.ssl.key-store-password=%KEYSTORE_PASS% ^
  --server.ssl.key-alias=ignite-ui
```

Проверка:
- https://localhost:8443/

> Для self-signed сертификата браузер будет ругаться, это нормально для локального теста.

---

## API

### Выполнить SQL

`POST /api/sql`

Body:
```json
{ "sql": "SELECT * FROM SYS.NODES" }
```

Ответ (SELECT):
```json
{
  "ok": true,
  "elapsedMs": 7,
  "columns": ["CONSISTENT_ID", "..."],
  "rows": [
    {"CONSISTENT_ID": "WIN-...", "...": "..."}
  ]
}
```

Ответ (DML/DDL):
```json
{ "ok": true, "elapsedMs": 9, "updateCount": 1, "columns": [], "rows": [] }
```

### Сводная диагностика Ignite

`GET /api/ignite/overview`

Возвращает блоки `nodes/caches/schemas/activeQueries`, каждый в формате результата `/api/sql`.

---

## Примеры SQL запросов

Системные представления:
```sql
SELECT * FROM SYS.NODES;
SELECT * FROM SYS.CACHES;
SELECT * FROM SYS.SCHEMAS;
SELECT * FROM SYS.SQL_QUERIES;
```

Метаданные:
```sql
SELECT TABLE_SCHEMA, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA NOT IN ('INFORMATION_SCHEMA', 'SYS');
```

---

## Важно про Ignite + H2

Ignite 2.x SQL использует H2 внутри `ignite-indexing`.  
Для Ignite **2.16.0** требуется версия H2, где есть параметр `MULTI_THREADED`, поэтому в проекте зафиксирован:

- `com.h2database:h2:1.4.197`

---

# Optional: Keycloak аутентификация (по умолчанию выключена)

Ниже описан рекомендуемый способ для Spring Boot: **OIDC Resource Server (JWT)**.

## Идея

- При `app.security.keycloak.enabled=false` (по умолчанию) — всё доступно без логина.
- При `app.security.keycloak.enabled=true` — API и UI требуют Bearer JWT от Keycloak.

## 1) Добавить зависимости (pom.xml)

Добавьте:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

> Это не “Keycloak adapter”, а стандартный OIDC/JWT подход. Он проще и живучее.

## 2) Настройки в `application.yml`

По умолчанию выключено:

```yaml
app:
  security:
    keycloak:
      enabled: false
      issuerUri: "http://localhost:8081/realms/ignite"
      audience: "ignite-sql-ui"
```

> `issuerUri` — URL realm. Для Keycloak обычно: `http(s)://<host>/realms/<realm>`.

## 3) Конфигурация Security, включаемая по флагу

Создайте класс `SecurityConfig` (пример):

```java
@Configuration
public class SecurityConfig {

  @Bean
  @ConditionalOnProperty(prefix="app.security.keycloak", name="enabled", havingValue="false", matchIfMissing=true)
  SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests().anyRequest().permitAll();
    return http.build();
  }

  @Bean
  @ConditionalOnProperty(prefix="app.security.keycloak", name="enabled", havingValue="true")
  SecurityFilterChain keycloakJwt(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
          .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
          .anyRequest().authenticated()
        .and()
        .oauth2ResourceServer().jwt();
    return http.build();
  }
}
```

## 4) Подключение к Keycloak (JWT)

Добавьте в `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${app.security.keycloak.issuerUri}
```

(Аудиторию можно валидировать дополнительно, если нужно.)

## 5) Как тестировать

### Получить токен (пример)
Зависит от настроек realm/client. Типовой сценарий (password grant может быть запрещён политиками):
- Создаёте **client** (например `ignite-sql-ui`)
- Настраиваете пользователей/роли
- Получаете `access_token`
- Дальше вызываете API так:

```bash
curl -X POST http://localhost:8080/api/sql   -H "Content-Type: application/json"   -H "Authorization: Bearer <ACCESS_TOKEN>"   -d "{ \"sql\": \"SELECT 1\" }"
```

### Включение авторизации при запуске
```bash
java -jar target/ignite-sql-ui-1.1.0.jar   --app.security.keycloak.enabled=true   --app.security.keycloak.issuerUri=http://localhost:8081/realms/ignite
```

---

## Структура проекта

```
src/main/java/com/example/igniteapp/
  IgniteSqlUiApplication.java
  config/
    IgniteProperties.java
    OpenApiConfig.java
  ignite/
    EmbeddedIgniteManager.java
  service/
    SqlService.java
    IgniteDiagService.java
  web/
    SqlApiController.java
    IgniteDiagController.java
  api/dto/ (если добавляли DTO для Swagger)
    SqlExecuteRequest.java
    SqlExecuteResponse.java
    IgniteOverviewResponse.java

src/main/resources/
  application.yml
  static/
    index.html
    app.js
    style.css
```

---

## Troubleshooting

- **Work directory path must be absolute**  
  Укажите `--app.ignite.embedded.workDir=<ABS_PATH>`.

- **Unsupported connection setting MULTI_THREADED**  
  Проверьте, что в jar используется H2 **1.4.197**.

- **HTTPS не поднимается**  
  Проверьте `--server.ssl.key-store=file:...` и пароль.

---
