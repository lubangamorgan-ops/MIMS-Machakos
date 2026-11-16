# MIMS — Machakos Farmers Association

Market Information Management System built with Java, Spring Boot, Thymeleaf, and H2.

The application supports crop and market registration, price entry with duplicate and date validation, farmer registration, broadcasts, price lookup, market trends, and historical reports.

## Requirements

- Java 25 LTS or newer
- Maven 3.9 or newer
- Internet access for the first build so Maven can download dependencies

## Run the application

```bash
cd mims-java
mvn spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080).

You can also open the `mims-java` folder as a Maven project in IntelliJ IDEA, Eclipse, or VS Code and run `MimsApplication.java`.

When opening the repository root in VS Code, choose **Run and Debug → Launch MIMS Application** (or press F5 with that profile selected). The profile runs from `mims-java` so the H2 database stays in the expected location. Stop any existing app instance first if port 8080 is already in use.

## Database

The application uses a file-based H2 database by default; no separate database setup is needed. Data is stored in `mims-java/data/` and persists between restarts.

While the application is running, the H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console). Connect with driver `org.h2.Driver`, JDBC URL `jdbc:h2:file:./data/mims;AUTO_SERVER=TRUE`, username `sa`, and a blank password. Run the application from the `mims-java` directory so `./data/mims` resolves to `mims-java/data/mims`.

The `.mv.db` file is an H2 database, not SQLite. Do not open it with a SQLite connection or point a saved connection at a previous project location; use the H2 console settings above.

For deployment, the database URL, username, and password are configurable through `DATABASE_URL`, `DB_USERNAME`, and `DB_PASSWORD`. PostgreSQL is supported by the included JDBC driver. Use a PostgreSQL JDBC URL (`jdbc:postgresql://...`) and set the matching credentials as environment variables. Avoid using the default H2 file database on hosts without persistent storage.

## Deploy to Render with Docker

The repository-root `Dockerfile` builds the Maven project from `mims-java/`. In Render, create a **Web Service** from this GitHub repository and select **Docker** as the runtime. Leave **Root Directory** blank so Render uses the repository root and finds the root Dockerfile.

Render should build the service from `Dockerfile`. Add these environment variables in the Render dashboard:

- `DATABASE_URL`: PostgreSQL JDBC URL, for example `jdbc:postgresql://<host>:5432/<database>`
- `DB_USERNAME` and `DB_PASSWORD`: credentials for that PostgreSQL database
- `H2_CONSOLE_ENABLED`: `false`
- `THYMELEAF_CACHE`: `true`
- `MIMS_PUBLIC_BASE_URL`: the public HTTPS URL for the deployed service
- `MIMS_ADMIN_USERNAME`, `MIMS_ADMIN_EMAIL`, and `MIMS_ADMIN_PASSWORD`: initial administrator credentials (password must be at least 12 characters)

The app reads Render's assigned port through `PORT`. Configure SMTP environment variables (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, and `MIMS_MAIL_FROM`) if password-recovery email delivery is needed. Keep credentials and other secrets in Render's environment settings, not in source control.

## Accounts and password recovery

Farmers can create accounts with a username, email, password, phone number, and nearest market. Passwords are stored as BCrypt hashes. Administrator signup is not public; provision the first administrator by setting these environment variables before the first application start:

- `MIMS_ADMIN_USERNAME`
- `MIMS_ADMIN_EMAIL`
- `MIMS_ADMIN_PASSWORD` (at least 12 characters)

The administrator account is created only if the configured username/email do not already exist. Changing the environment password later does not overwrite an existing account.

On sign-in, select **Remember me for 30 days** to stay signed in across browser restarts. The persistent sign-in is stored as a revocable, rotating token in the application database; signing out removes it. Leave the option unchecked on shared devices.

Password recovery sends a single-use link that expires after 30 minutes. Configure SMTP outside source control before enabling email delivery:

- `SMTP_HOST`, `SMTP_PORT` (defaults to `587`)
- `SMTP_USERNAME`, `SMTP_PASSWORD`
- `SMTP_AUTH` and `SMTP_STARTTLS` (default to `true`)
- `MIMS_MAIL_FROM`
- `MIMS_PUBLIC_BASE_URL` (defaults to `http://localhost:8080`; set this to the public HTTPS app URL in deployment)

If SMTP is not configured, the recovery form reports that email delivery is unavailable.

## Using the application

1. On the landing page, choose **Administrator sign in** to manage crops, markets, prices, farmers, and broadcasts.
2. Choose **Look up today's prices** to access the farmer price lookup, trends, and alerts.
3. Administrators can test duplicate-price validation by entering the same crop, market, and date more than once.

## What's new in version 3

See `CHANGELOG.md`. Administrators now see a clear message after every action, cannot resubmit a form by refreshing the page, confirm before removing records, and are prompted to post today's prices when none have been entered.

## Project structure

```text
mims-java/
  src/main/java/ke/co/machakosfa/mims/
    model/        JPA entities
    repository/   Spring Data JPA repositories
    service/      Business logic and validation
    controller/   Spring MVC controllers
    exception/    Custom exceptions
  src/main/resources/
    templates/    Thymeleaf views
    static/css/   Stylesheet
    application.properties
  pom.xml
```
