# Automatic UML Generation

The folder contains the nix code to automatically generate a class diagram using a [this generator](https://github.com/samuelroland/plantuml-parser).

We apply the following changes (we tried splitting it into multiple patches but then they wouldn't apply property):
- Support for the latest version of Java
- Added an option to sort of fields, methods, and the relationships in the diagram
- Added an option to show/hide nested types
- Added an option to show what classes use each other
- Added an option to exclude specific classes (we use this to exclude stdlib classes)
- Added an option to exclude all child classes of specific classes (we use this to hide the concrete children of Event)
- Fixed typeless fields (e.g., enum constants) still having a `:`
- Fixed records showing up as classes (`(C)` symbol) instead of records (`(R)` symbol)
- Made the primary constructor for records always show

Note: the generated diagram cannot just be checked in directly because it is messy. Manual adjustment to relationship directions and omission of superfluous relationships is still required to get a good looking diagram.

## Using outside of nix

To use our version of the generator of nix:
1. Clone the original repo, checking out the commit specified in the `rev` of `fetchFromGitHub` in `plantuml-parser-cli.nix`
2. Apply `fixes.patch` (`git apply < /path/to/fixes.patch`)
3. Build the generator `./gradlew build plantuml-parser-cli:uberJar` (You MUST use the `uberJar` task, not just the default `build`)
4. Run the generator against the core subproject using the given `flags.txt`:
```
java -jar plantuml-parser-cli-<version>-uber.jar -f <maestro path>/src/core/src/main/java/ $(cat <maestro path>/docs/uml-gen/flags.txt) > diagram.puml
```
