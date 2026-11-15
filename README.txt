Gear & Effects HUD  (Fabric, Minecraft 1.21.11)

IN GAME
  Press K to open the HUD editor (change the key in Options > Controls > Key Binds).
  Drag the panels to move them, click the buttons to change settings.
  Settings are saved in .minecraft/config/gearhud.json

GET THE .JAR (pick one)
  A) Easiest, nothing to install: put this folder on GitHub (see below). GitHub builds the jar for you.
  B) On your PC: install Java 21 and Gradle, then run
        gradle wrapper --gradle-version 9.2.1
        ./gradlew build
     The jar is build/libs/gearhud-1.2.0.jar  (NOT the -sources jar)

INSTALL
  Put the jar in .minecraft/mods together with Fabric API (Fabric Loader 0.18.1+).

BEFORE PUBLISHING ON MODRINTH
  Replace "Your Name" in fabric.mod.json and LICENSE.
  Use modrinth-icon-512.png as the project icon.
  Loader: Fabric | Game version: 1.21.11 | Client side: required, Server side: unsupported
  Dependency: Fabric API (required)
