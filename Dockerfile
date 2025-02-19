FROM openjdk:17-jdk-slim
WORKDIR /app
COPY ./target/simuladordispositivo.jar /app/simuladordispositivo.jar
EXPOSE 80
CMD ["java", "-jar", "simuladordispositivo.jar"]
