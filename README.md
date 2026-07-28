# mams-groovy-integrations
MAMS-related integrations using Groovy

# Developer Information
This project uses a dev container to provide a standard development environment for working with Groovy. JDK 25 (the latest LTS version) is installed, as are VS Code extensions for supporting Groovy development.

## Dev container

This project comes with a basic dev container definition in `.devcontainer/devcontainer.json`. It's known to work with VS Code,
and may work with other IDEs like PyCharm. For VS Code, it also installs two extensions: [NicolasVuillamy.vscode-groovy-lint](https://marketplace.visualstudio.com/items?itemName=NicolasVuillamy.vscode-groovy-lint) for linting and [DontShaveTheYak.groovy-guru](https://marketplace.visualstudio.com/items?itemName=DontShaveTheYak.groovy-guru) for IntelliSense.

The dev container config will mount the project's directory at `/workspaces/mams-groovy-integrations` within the container.

### Building and connecting to the dev container

VS Code builds its own container from the project's Dockerfile. With the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) installed, when opening the project locally in VS Code, it will prompt you to reopen the project in a dev container. If you choose to do so, it will build the container and connect to it.

Related commands can be found in the Command Palette (Ctrl+Shift+P) under "Dev Containers".
