# Используем официальный образ Java 17
FROM eclipse-temurin:17-jdk

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем Maven-обёртку и pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Скачиваем зависимости (кэшируем этот слой)
RUN ./mvnw dependency:resolve

# Копируем исходный код
COPY src ./src

# Собираем приложение и запускаем
CMD ["./mvnw", "spring-boot:run"]

# Stage 1: Build
FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]