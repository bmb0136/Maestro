{
  inputs = {
    flake-parts.url = "github:hercules-ci/flake-parts";
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = inputs@{ flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      systems = [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" "x86_64-darwin" ];
      perSystem = { self', pkgs, ... }: {
        packages.default = self'.packages.maestro;

        packages.maestro = pkgs.callPackage ./src {};

        devShells.default = pkgs.callPackage ./src/shell.nix {};
      };
    };
}
