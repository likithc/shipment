FROM eclipse-temurin:17-jre

WORKDIR /app

# Copies your compiled jar into the container
COPY target/shipment-service-1.0.0.jar app.jar

# Updated to match the actual port of your shipment service application
EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
