FROM ghcr.io/graalvm/graalvm-ce:ol7-java17-22.3.3

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and project files
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

# Grant execute permissions to the Gradle wrapper
RUN chmod +x gradlew

# Build the JAR file
RUN ./gradlew bootJar --no-daemon

RUN ls -la build/libs
RUN pwd
RUN echo hello!

# Set the entry point
ENTRYPOINT ["java", "-Xmx2g", "-Xms2g", "-jar", "build/libs/ScalaSpringExperiment-0.0.1-SNAPSHOT.jar"]
