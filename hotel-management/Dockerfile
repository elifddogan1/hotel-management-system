# 1. Aşama: Maven imajını kullanarak projeyi derliyoruz ve .jar üretiyoruz
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Pom.xml ve kaynak kodları içeri kopyalıyoruz
COPY pom.xml .
COPY src ./src

# Projeyi derleyip jar paketini hazırlıyoruz (Testleri hızlıca geçmesi için atlıyoruz)
RUN mvn clean package -DskipTests

# 2. Aşama: Sadece hafif bir JRE imajı alarak hafif bir container üretiyoruz
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# İlk aşamada üretilen .jar dosyasını bu aşamaya taşıyoruz
COPY --from=build /app/target/*.jar app.jar

# Uygulamanın çalışacağı portu belirtiyoruz
EXPOSE 8080

# Spring Boot'u ayağa kaldıracak komut
ENTRYPOINT ["java", "-jar", "app.jar"]