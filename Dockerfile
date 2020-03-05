FROM openjdk:7
WORKDIR /app
ADD jar/raceserver.jar /app/
EXPOSE 3002
ENTRYPOINT ["java", "-jar","raceserver.jar"]
