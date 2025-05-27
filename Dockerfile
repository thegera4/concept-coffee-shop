# Use a lightweight openJDK image as the base
FROM openjdk:21-jdk-slim
# Set the working directory inside the container
WORKDIR /app
# Copy the JAR file into the container
COPY ./build/libs/*.jar /app/concept-coffee-shop.jar
# Expose the port that the application will run on
EXPOSE 8080
# Set the command to run the application
CMD ["java", "-jar", "concept-coffee-shop.jar"]