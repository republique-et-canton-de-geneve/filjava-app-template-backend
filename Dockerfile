ARG JAVA_RUNTIME_IMAGE
FROM ${JAVA_RUNTIME_IMAGE}

WORKDIR /app
COPY ui/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m -Dspring.profiles.active=prod -Dfile.encoding=UTF-8"
USER 10001:10001
EXPOSE 8080 5701
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
