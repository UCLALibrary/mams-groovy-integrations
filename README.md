# mams-groovy-integrations
MAMS-related integrations using Groovy

# Developer Information

## Build (first time) / rebuild (as needed)

`docker compose build`

This builds a Docker image, `mams-groovy-integrations-dev:latest`, which can be used for developing, testing, and running code.

## Dev container

This project comes with a basic dev container definition in `.devcontainer/devcontainer.json`. It's known to work with VS Code,
and may work with other IDEs like PyCharm. For VS Code, it also installs two extensions: [NicolasVuillamy.vscode-groovy-lint](https://marketplace.visualstudio.com/items?itemName=NicolasVuillamy.vscode-groovy-lint) for linting and [DontShaveTheYak.groovy-guru](https://marketplace.visualstudio.com/items?itemName=DontShaveTheYak.groovy-guru) for IntelliSense.

The project's directory is available within the container at `/home/groovy/project`.

## Running code

From within the dev container, you can run code using the `groovy` command. For example, `groovy HelloWorld.groovy` should print "Hello, World" to the console.

Otherwise, run a program via docker compose. From the project directory:

```
# Start the system
$ docker compose up -d

# Open a shell in the container
$ docker compose exec dev bash

# Check the Groovy version
$ groovy -version
$ WARNING: Using incubator modules: jdk.incubator.vector  <- This warning can be ignored
$ Groovy Version: 5.0.3 JVM: 25.0.1 Vendor: Eclipse Adoptium OS: Linux
``` 

