{
  pkgs ? import <nixpkgs> { },
  lib ? pkgs.lib,
  stdenvNoCC ? pkgs.stdenvNoCC,
  gradle ? pkgs.gradle,
  javaPackages ? pkgs.javaPackages,
  makeWrapper ? pkgs.makeWrapper,
  ...
}:
let
  jdk = javaPackages.compiler.openjdk21.override { enableJavaFX = true; };
in
stdenvNoCC.mkDerivation (finalAttrs: {
  pname = "maestro";
  version = "1.0.0";

  src = ./.;

  mitmCache = gradle.fetchDeps {
    pkg = finalAttrs.finalPackage;
    data = ./deps.json;
  };

  nativeBuildInputs = [
    gradle
    makeWrapper
  ];

  gradleFlags = [
    "-Dorg.gradle.java.home=${jdk}"
  ];

  gradleBuildTask = "app:build";

  installPhase = ''
    mkdir -p $out/bin
    mkdir -p $out/share/java
    cp ./app/build/libs/app-all.jar $out/share/java/maestro.jar
    makeWrapper ${jdk}/bin/java $out/bin/maestro \
      --add-flags "-jar $out/share/java/maestro.jar"
  '';

  meta.sourceProvenance = with lib.sourceTypes; [
    fromSource
    binaryBytecode # mitm cache
  ];
})
