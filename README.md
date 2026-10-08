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

From within the dev container, you can run code using the `groovy` command. For example, `groovy -version` should print the Groovy version to the console.

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

## match_baton_profile.groovy

This script constructs a lookup key from XML inputs representing technical metadata of media assets in the MAMS to match against profiles active in UCLA's Baton media quality control system. The script is a step in an integration workflow within the smartWork component of the MAMS. The smartWork workflow makes XML available to the script as input, while the development environment here uses XML fixtures stored under `fixtures/` for testing and development purposes.

Running the script using `groovy match_baton_profile.groovy` will process the XML fixtures and output the constructed lookup key and any matching results. As new profiles are added or existing ones are updated in UCLA's Baton system, the script can be rerun to verify that the lookup key correctly matches the expected profiles. A new fixture should be added to the `fixtures/` directory whenever a new type of media asset needs to be tested, and a corresponding test case should be added to the `runTests()` function in `match_baton_profile.groovy`.
