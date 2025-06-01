FROM eclipse-temurin:17-jre-alpine

# Ajouter des outils de diagnostic réseau
RUN apk add --no-cache curl iputils busybox-extras

RUN addgroup -S payiskoul && adduser -S app -G payiskoul

# Créer les répertoires nécessaires
RUN mkdir -p /home/app/logs && \
    chown -R app:payiskoul /home/app

# Copier le jar de l'application
COPY --chown=app:payiskoul target/institution-sce-*.jar /home/app/app.jar

# Définir le répertoire de travail
WORKDIR /home/app

# Passer à l'utilisateur non-root
USER app

ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar app.jar"]