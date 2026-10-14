# Multi-stage Docker build for CampusTrack Java Backend
# Stage 1: Build WAR with Maven & JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to Apache Tomcat 10 (Jakarta Servlet 5.0)
FROM tomcat:10.1-jdk17-temurin
LABEL maintainer="CampusTrack Team"

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy packaged WAR as ROOT.war so endpoints are accessible directly at /api/*
COPY --from=builder /app/target/campustrack.war /usr/local/tomcat/webapps/ROOT.war

# Default Port
EXPOSE 8080

# Environment variables with defaults
ENV PORT=8080
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=campustrack
ENV DB_USER=root
ENV DB_PASSWORD=""
ENV JWT_SECRET="CampusTrackSecureSecretKey2026!9876543210#@"

CMD ["catalina.sh", "run"]
