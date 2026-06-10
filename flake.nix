{
  description = "FossWallet";

  outputs = { self, nixpkgs }:
  let
    system = "x86_64-linux";
    pkgs = import nixpkgs { inherit system; config = { allowUnfree = true; }; };
  in {

    devShells.${system}.default = pkgs.mkShell {
      buildInputs = [ pkgs.android-studio ];

      # Use the local ../compose-kit source via composite build (see settings.gradle.kts).
      FOSSWALLET_LOCAL_COMPOSE_KIT = "true";
    };

  };
}
