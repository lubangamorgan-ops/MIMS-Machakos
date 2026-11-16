FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY mims-java/pom.xml ./pom.xml
COPY mims-java/src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/mims-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
