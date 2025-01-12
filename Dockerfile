FROM openjdk:17-jdk
ARG JAR_FILE=./build/libs/playus-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev" , "-jar" ,"/app.jar" ]