# Groovy image with JDK 25 (current LTS version)
FROM groovy:jdk25

USER root

RUN apt-get update && apt-get install -y git

USER groovy