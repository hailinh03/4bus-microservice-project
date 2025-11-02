FROM maven:3.9.8-amazoncorretto-21 AS build

WORKDIR /app
COPY booking-service/pom.xml .
COPY booking-service/src ./src

RUN mvn package -DskipTests

FROM amazoncorretto:21.0.4

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
