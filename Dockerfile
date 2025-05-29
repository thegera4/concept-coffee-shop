FROM openjdk:21-jdk-slim

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Find the actual JAR file name
RUN find build/libs -name "*.jar" | grep -v "plain" > jarfile.txt
RUN cat jarfile.txt

ENV PORT=8080
EXPOSE ${PORT}

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "$(cat jarfile.txt)"]