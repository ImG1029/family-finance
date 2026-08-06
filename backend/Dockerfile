FROM amazoncorretto:25-alpine-jdk AS base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

FROM base AS dev
COPY src ./src
EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]

FROM base AS builder
COPY src ./src

RUN ./mvnw package -DskipTests

FROM amazoncorretto:25-alpine-jdk AS prod

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]