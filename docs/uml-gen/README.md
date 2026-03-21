# Automatic UML Generation

The folder contains the nix code to automatically generate documentation using a [this generator](https://github.com/samuelroland/plantuml-parser).

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
