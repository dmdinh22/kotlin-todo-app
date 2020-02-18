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
# RUN mvn clean package
RUN mvn install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=0 /app/target/kotlin-todo-app-1.0.0 /app
EXPOSE 8080
CMD ["java -jar kotlin-todo-app-1.0.0.jar"]

