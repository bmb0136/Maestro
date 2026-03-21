{
  pkgs ? import <nixpkgs> { },
  lib ? pkgs.lib,
  stdenvNoCC ? pkgs.stdenvNoCC,
  gradle ? pkgs.gradle,
  javaPackages ? pkgs.javaPackages,
  makeWrapper ? pkgs.makeWrapper,
  fetchFromGitHub ? pkgs.fetchFromGitHub,
  ...
}:
let
  jdk = javaPackages.compiler.openjdk17;
in
stdenvNoCC.mkDerivation (finalAttrs: {
  pname = "plantuml-parser-cli";
  version = "1.0.0";

  src = fetchFromGitHub {
      owner = "samuelroland";
      repo = "plantuml-parser";
      rev = "1f0d9d185499632a84d4b232090b15461cc76ff8";
      sha256 = "sha256-D9KoRp2GtToeeinqYGvVo2wspGuTDvPa1BDtB/MwtKw=";
  };
  patches = [ ./fixes.patch ];

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

  gradleBuildTask = "plantuml-parser-cli:uberJar";

  installPhase = ''
    mkdir -p $out/bin
    mkdir -p $out/share/java
    cp ./plantuml-parser-cli/build/libs/*-uber.jar $out/share/java/${finalAttrs.pname}.jar
    makeWrapper ${jdk}/bin/java $out/bin/${finalAttrs.pname} \
      --add-flags "-jar $out/share/java/${finalAttrs.pname}.jar"
  '';

  meta.sourceProvenance = with lib.sourceTypes; [
    fromSource
    binaryBytecode # mitm cache
  ];
})
