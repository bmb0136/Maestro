# https://github.com/NixOS/nixpkgs/issues/338165
{
  pkgs ? import <nixpkgs> { },
  mkShell ? pkgs.mkShell,
  javaPackages ? pkgs.javaPackages,
  ...
}:
mkShell {
  packages = [
    (javaPackages.compiler.openjdk21.override { enableJavaFX = true; })
  ];
}
