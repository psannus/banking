FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY *.gradle gradle.* gradlew /app/
RUN mkdir -p /app/gradle
COPY gradle /app/gradle

RUN ./gradlew dependencies

COPY ./src /app/src

RUN ./gradlew assemble testClasses

COPY build/libs/banking.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","./app.jar"]
