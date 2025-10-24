FROM eclipse-temurin:25-jre

# Create a non-root user to run the application
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
WORKDIR /app

# Copy the runnable JAR produced by the build. The filename may include the version, so
# we copy by pattern and rename to app.jar for convenience.
ARG JAR_FILE=api/target/*api-*.jar
COPY ${JAR_FILE} app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]