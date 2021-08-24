FROM adoptopenjdk:11-jre-hotspot
COPY ./target/broker-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "broker-0.0.1-SNAPSHOT.jar"]