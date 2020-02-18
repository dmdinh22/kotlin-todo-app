FROM alpine/git as clone

# create app src dir
RUN mkdir -p /app
# set home dir
WORKDIR /app

FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
# copy src code to container
COPY . /app
# build with maven
RUN mvn clean package
RUN mvn install

# debugging - ls the content of /app/target dir
RUN ls -l /app/target

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/kotlin-todo-app-1.0-SNAPSHOT.jar /app
EXPOSE 8080
CMD ["java -jar kotlin-todo-app-1.0-SNAPSHOT.jar"]

