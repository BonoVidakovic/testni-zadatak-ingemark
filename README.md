# Simple movies listing API

In order to start a project you just need to run: *./mwnv spring-boot:run*

Docker-compose (postgres) is autoconfigured by Spring.

I've added integration tests and have setup Maven to use Failsafe
for running them, so if *mvn clean install* takes too long try running it with *-DskipIT*.

Tech used:
  - Spring Boot
  - Postgres
  - Docker
  - Hibernate
  - Liquibase
  - Testcontainers
  - Wiremock
  - RestAssured
