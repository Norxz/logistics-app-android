# 8. Configuración y ejecución

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| Android Studio | Reciente, compatible con AGP 8.13.1 |
| JDK | 11 o superior (el proyecto compila con Java 11) |
| Android SDK | `compileSdk` 36; dispositivo o emulador con API 24 o superior |
| Kotlin | 2.2.0 |
| Backend | Spring Boot en `localhost:8080` con MySQL 3306 |
| Cuenta Google Cloud | Para una API key de *Maps SDK for Android* |

## Puesta en marcha

1. **Backend:** tener MySQL en el puerto 3306 con la base `logistica_db` (`jdbc:mysql://localhost:3306/logistica_db`) y arrancar el proyecto Spring Boot desde su IDE o con Gradle (`BackEndApplication`). Su `application.properties` usa usuario `root`, contraseña `root` y `ddl-auto=update`; además existe un `DataInitializer` en `config/` que se ejecuta al arrancar (revisar qué datos crea en el repositorio del backend).
2. **API key de Google Maps:** reemplazar el valor de `com.google.android.geo.API_KEY` en `app/src/main/AndroidManifest.xml` por una clave propia con restricción por paquete (`co.edu.unipiloto.myapplication`) y huella SHA-1. La clave del repositorio debe darse por comprometida.
3. **Abrir** el proyecto en Android Studio y sincronizar Gradle.
4. **Ejecutar** en un emulador. Desde el emulador `10.0.2.2` apunta al `localhost` del equipo.
5. En un **dispositivo físico**: cambiar `BASE_URL` en `dto/RetrofitClient.kt` por la IP LAN del equipo (por ejemplo `http://192.168.1.20:8080/api/v1/`).

> **Bloqueante conocido:** el manifest y `DriverDashboardActivity` usan `co.edu.unipiloto.myapplication.services.NotificationService` y `OdometerService`, pero ese paquete no está en el repositorio. Hasta restaurarlo (o quitar sus usos) el proyecto no compila desde un checkout limpio.

## Línea de comandos

```bash
./gradlew assembleDebug      # compila el APK de depuración
./gradlew test               # pruebas unitarias (solo plantilla)
./gradlew connectedAndroidTest  # pruebas instrumentadas (solo plantilla)
```

En Windows: `gradlew.bat`.

## Variantes de build

| Build type | Minificación | Notas |
|-----------|--------------|-------|
| `debug` | No | Por defecto |
| `release` | **No** (`isMinifyEnabled = false`) | Reglas ProGuard por defecto; sin firma configurada |

`applicationId`: `co.edu.unipiloto.myapplication` · `versionCode 1` · `versionName 1.0`.

## Dependencias principales

| Grupo | Artefacto | Versión |
|-------|----------|---------|
| AndroidX | appcompat / activity / constraintlayout / core-ktx | 1.7.1 / 1.10.1 / 2.2.1 / 1.17.0 |
| Material | material | 1.12.0 |
| Navigation | navigation-fragment-ktx / ui-ktx | 2.6.0 y 2.9.5 (declaradas dos veces) |
| Red | retrofit, converter-gson | 2.11.0 |
| Red | okhttp, logging-interceptor | 4.12.0 |
| Maps | play-services-maps | 18.2.0 y 19.2.0 (declarada dos veces) |
| Ubicación | play-services-location | 21.0.1 |
| Correo | android-mail, android-activation | 1.6.7 (sin uso encontrado) |
| Layout | gridlayout | 1.0.0 |
| Pruebas | junit 4.13.2, ext-junit 1.3.0, espresso 3.7.0 | |

El catálogo de versiones está en `gradle/libs.versions.toml`; algunas dependencias se declaran con coordenadas directas en `app/build.gradle.kts`.

## Permisos (`AndroidManifest.xml`)

| Permiso | Para qué |
|---------|----------|
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Mapa, GPS, odómetro |
| `INTERNET` | Llamadas al backend |
| `POST_NOTIFICATIONS` | Notificaciones (Android 13+) |
| `FOREGROUND_SERVICE` | Servicio de odómetro |
| `WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE` | Obsoletos; la guía se guarda en el directorio propio de la app (`getExternalFilesDir`) |

Además: `usesCleartextTraffic="true"`, `allowBackup="true"`, y un `FileProvider` (`${applicationId}.fileprovider`, ruta `external-files-path`) para abrir los PDF.

## Componentes declarados en el manifest

- 19 Activities (solo `MainActivity` exportada).
- `FileProvider`.
- Servicios `.services.NotificationService` y `.services.OdometerService` (**clases ausentes del repositorio**).

## Estructura del repositorio

```
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/co/edu/unipiloto/myapplication/   ← código (ver Arquitectura)
│       │   └── res/                                  ← layouts, drawables, values, xml
│       ├── test/            ← prueba unitaria de ejemplo
│       └── androidTest/     ← prueba instrumentada de ejemplo
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── docs/                    ← esta documentación
```

## Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---------|---------------|-----------|
| `Unresolved reference: services` al compilar | Falta el paquete `services` | Restaurar `NotificationService` y `OdometerService` |
| "Fallo de red" / `CLEARTEXT not permitted` | Backend apagado o URL incorrecta | Verificar que corre en el 8080; usar `10.0.2.2` en emulador |
| Mapa en gris | API key inválida o sin restricciones correctas | Generar una clave propia y habilitar *Maps SDK for Android* |
| `404` al cambiar estado o editar usuarios | Rutas del cliente no existen en el backend | Ver [API](06-api-y-contrato-backend.md) |
| No aparece el selector de PDF | No hay visor instalado | El archivo queda en el directorio externo de la app |
