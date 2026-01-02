# Step 1: Base image
FROM eclipse-temurin:21-jdk-alpine

# Step 2: Workdir
WORKDIR /app

# Step 3: Copy project files
COPY . .

# Step 4: Make Maven wrapper executable and build
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Step 5: Expose port
EXPOSE 8080

# Step 6: Run the jar
CMD ["java", "-jar", "target/ecommerce-0.0.1-SNAPSHOT.jar"]
