# Repository Guidelines

## Project Structure & Module Organization
- Spring Boot sources live in `src/main/java/com/readour/...`; group new features under domain-focused packages such as `chat`, `community`, or shared utilities in `common`.
- Shared configuration sits in `src/main/resources`, with profile-specific settings in `application.properties` and `application-nokafka.properties`.
- Generated artifacts land in `build/`; keep this directory out of version control.
- Tests mirror the main tree in `src/test/java`; place fixtures alongside the package they exercise.
- Local messaging support is defined in `kafka-compose.yml` for spinning up Kafka when required.

## Build, Test, and Development Commands
- `./gradlew bootRun` starts the API locally with hot reload support.
- `./gradlew build` compiles, runs unit tests, and assembles the runnable JAR.
- `./gradlew test` executes the JUnit 5 suite; add `-i` for verbose diagnostics.
- `./gradlew asciidoctor` regenerates REST Docs output after controller changes (depends on successful tests).
- `docker compose -f kafka-compose.yml up -d` provisions Kafka locally; tear down with the matching `down`.

## Coding Style & Naming Conventions
- Target Java 17 with 4-space indentation and organized imports (let your IDE or Gradle manage formatting).
- Use `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields, and `UPPER_SNAKE_CASE` for constants.
- Keep package names lowercase; align component names with their Spring stereotype (e.g., `ChatService`, `ChatController`).
- Prefer Lombok annotations already in use; avoid mixing manual getters with `@Data`/`@Getter`.
- Place cross-cutting DTOs or validators in `com.readour.common`; domain-specific contracts stay under their respective package.

## Testing Guidelines
- Write tests under `src/test/java`, mirroring production package paths for easy discovery.
- Favor descriptive method names like `shouldCreateRoomWhenValidRequest()` and add `@DisplayName` for complex flows.
- Use Spring’s `@SpringBootTest`/`@WebMvcTest` for integration coverage and MockMvc or Kafka test utilities when touching messaging.
- Run `./gradlew test` before committing so REST Docs snippets in `build/generated-snippets` stay in sync for previews.

## Commit & Pull Request Guidelines
- Follow the existing convention `type : short summary` (e.g., `feat : add websocket broker`); include scope when it clarifies the impact.
- Keep commits focused; separate refactors from feature work and ensure builds are green beforehand.
- Pull requests should describe the change, reference related issues, list automated/manual test results, and attach screenshots or API samples when relevant.
- Request review from a domain owner (chat, community, etc.) and wait for approval before merging into the default branch.

## Configuration & Local Profiles
- Use environment variables or `application-nokafka.properties` for lightweight runs when Kafka is unavailable.
- Document any new secrets or required services in `HELP.md`, and prefer `.env` indirection over hard-coded values.
