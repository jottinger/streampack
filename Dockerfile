FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package

FROM eclipse-temurin:latest
WORKDIR /app
COPY --from=build /app/application-llm/target/application-llm-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "application-llm-0.0.1-SNAPSHOT.jar"]
