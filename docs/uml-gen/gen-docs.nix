{
  pkgs ? import <nixpkgs> {},
  runCommand ? pkgs.runCommand,
  callPackage ? pkgs.callPackage,
  ...
}: let
  flags = builtins.replaceStrings ["\n" "\r"] [" " " "] (builtins.readFile ./flags.txt);
  code = ../../src/core/src/main/java;
in runCommand "maestro-docs.puml" {
  nativeBuildInputs = [ (callPackage ./plantuml-parser-cli.nix {}) ];
} ''
  touch "$out"
  plantuml-parser-cli -f "${code}" ${flags} > "$out"
''
