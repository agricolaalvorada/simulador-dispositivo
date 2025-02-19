FROM openjdk:17-jdk-slim
WORKDIR /app
COPY ./target/simuladordispositivo-0.0.1-SNAPSHOT.jar /app/simuladordispositivo.jar
EXPOSE 80
CMD ["java", "-jar", "simuladordispositivo.jar"]
