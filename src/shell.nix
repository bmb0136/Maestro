# https://github.com/NixOS/nixpkgs/issues/338165
{
  pkgs ? import <nixpkgs> { },
  ...
}:
pkgs.mkShell {
  buildInputs = with pkgs; [
    javaPackages.openjfx21
    libglibutil
    xorg.libXxf86vm
    glibc
    glib
    gsettings-desktop-schemas
    scenebuilder
    (pkgs.jdk21.override { enableJavaFX = true; })
  ];
  GSETTINGS_SCHEMA_DIR = "${pkgs.gtk3}/share/gsettings-schemas/${pkgs.gtk3.name}/glib-2.0/schemas";
}
