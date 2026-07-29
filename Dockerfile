# Groovy image with JDK 25 (current LTS version)
FROM groovy:jdk25 AS base

USER root

# Update base image
RUN apt-get update

# Set correct timezone
RUN ln -sf /usr/share/zoneinfo/America/Los_Angeles /etc/localtime

# Switch to application directory, creating it if needed
WORKDIR /home/groovy/project

# Dev container stage
FROM base AS dev

# Install git here for compatibility across IDEs
RUN apt-get install -y git

# Node is needed for the VS Code linting extension we are using
COPY --from=node:lts /usr/local/bin/node /usr/local/bin/node

# Base image includes groovy user
USER groovy
