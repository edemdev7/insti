FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copier les fichiers Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Construire l'application
RUN chmod +x ./mvnw
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Phase de construction de l'image finale
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Récupération des métadonnées de l'application
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Exposition du port de l'application
EXPOSE 8080

# Démarrage de l'application
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.payiskoul.institution.InstitutionApplication"]