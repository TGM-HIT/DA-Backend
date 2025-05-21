#
# Build stage
#
FROM gradle:jdk21-noble AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build bootJar --no-daemon --console=verbose -x test


#
# Package stage
#
FROM eclipse-temurin:21-jre-noble
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar
RUN mkdir -p /data
WORKDIR /data
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]