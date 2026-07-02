{
  system,
  nixpkgs,
}:

let
  buildToolsVersion = "37.0.0";

  buildToolsVersions = [ buildToolsVersion ];
  platformVersions = [ "37" ];

  pkgs = import nixpkgs {
    inherit system;

    config.allowUnfree = true;
    config.android_sdk.accept_license = true;
  };

  jdk = pkgs.jdk21;

  androidSdk = pkgs.androidenv.composeAndroidPackages {
    inherit buildToolsVersions platformVersions;
  };
in
{
  default = pkgs.mkShell {
    packages = [
      jdk
      pkgs.gradle
      androidSdk.androidsdk
    ];

    env = {
      ANDROID_HOME = "${androidSdk.androidsdk}/libexec/android-sdk";

      JAVA_HOME = "${jdk}";

      # aapt2 bundled in the AGP Maven artifact is a generic-Linux binary
      # that NixOS cannot run. Override it with the Nix-patched copy.
      GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk.androidsdk}/libexec/android-sdk/build-tools/${buildToolsVersion}/aapt2";

      # Path to the local compose-kit checkout; enables the composite build (see settings.gradle.kts).
      LOCAL_COMPOSE_KIT = "../compose-kit";
    };

    shellHook = ''
      cat > "$PWD/local.properties" <<EOF
      sdk.dir=${androidSdk.androidsdk}/libexec/android-sdk
      EOF
    '';
  };
}
