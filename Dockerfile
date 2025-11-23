FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/big-bob-1.0-SNAPSHOT.jar /app/big-bob.jar

COPY .env /app/.env

CMD ["java", "-jar", "/app/big-bob.jar"]
