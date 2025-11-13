# Compilation Commands
Project is build on maven. To compile the project, use the following command:

``` mvn clean install ```

This command will clean any previous builds and install the necessary dependencies before compiling the project.

If you want to only build project without cleaning previous builds (first time compilation for example), you can use:

``` mvn install ```

This will compile the project and install the necessary dependencies without cleaning previous builds.

After successful compilation, the compiled files will be located in the `target` directory of the project.
For running tests, you can use:

``` mvn test ```

This command will execute all the tests in the project.

Make sure you have Maven installed and configured properly on your system to use these commands.

JDK version: 21 or higher is required to compile the project.