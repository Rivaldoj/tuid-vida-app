# TuID Vida

Aplicación para fe de vida de jubilados

## Compilar

Requisitos: JDK 17, Android SDK (platform 35, build-tools 34/35).

1. Crear `local.properties` con `sdk.dir=<ruta al SDK>` (no se versiona).
2. Para firmar el release, crear `keystore.properties` (no se versiona) con `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. Sin ese archivo el release sale sin firmar.
3. `gradlew assembleDebug` / `gradlew assembleRelease` / `gradlew bundleRelease`.
