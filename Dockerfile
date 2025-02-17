FROM eclipse-temurin:21-jre-noble
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]