FROM openjdk:21-jdk-slim

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

ENV PORT=8080
EXPOSE ${PORT}

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "build/libs/*.jar"]