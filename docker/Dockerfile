FROM openjdk:17-alpine
#RUN apk add tzdata
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*
ARG JAR_FILE_PATH=*.jar
COPY ${JAR_FILE_PATH} app.jar
VOLUME ["/var/log", "/var/firmware"]