# 9. Seguridad

Este documento describe el estado real del cliente. La app es un prototipo académico; los puntos siguientes deben resolverse antes de cualquier uso con datos reales.

## Resumen de riesgos

| # | Riesgo | Severidad | Dónde |
|---|--------|-----------|-------|
| 1 | API key de Google Maps commiteada | Alta | `AndroidManifest.xml` e historial de git |
| 2 | Sin autenticación en las peticiones (sin token) | Alta | `RetrofitClient`, backend |
| 3 | Tráfico HTTP sin cifrar | Alta | `BASE_URL`, `usesCleartextTraffic="true"` |
| 4 | Logging HTTP en nivel `BODY` | Media | `RetrofitClient` |
| 5 | Control de roles solo en el cliente | Media | Activities (`sessionManager.getRole()`) |
| 6 | Contraseñas: el cliente las envía en claro; `PasswordHasher` sin uso | Media | `LoginRequest`, `security/PasswordHasher.kt` |
| 7 | `allowBackup="true"` | Baja | Manifest |
| 8 | Permisos de almacenamiento obsoletos | Baja | Manifest |
| 9 | Release sin minificar ni ofuscar | Baja | `app/build.gradle.kts` |

## Detalle

### 1. API key de Google Maps expuesta
El valor está escrito en el manifest y permanece en el historial. Pasos:
1. Revocar la clave en Google Cloud Console.
2. Crear una nueva con restricción por aplicación (paquete + SHA-1) y por API (*Maps SDK for Android*).
3. Cargarla desde `local.properties` (no versionado) con `manifestPlaceholders`:

```kotlin
// app/build.gradle.kts
val mapsKey = (project.findProperty("MAPS_API_KEY") as String?) ?: ""
defaultConfig { manifestPlaceholders["MAPS_API_KEY"] = mapsKey }
```
```xml
<meta-data android:name="com.google.android.geo.API_KEY" android:value="${MAPS_API_KEY}" />
```
4. Si se publica el repositorio, limpiar el historial (`git filter-repo`) o asumir la clave como pública y restringida.

### 2. Sin token de autenticación
`RetrofitClient` tiene un comentario `TODO` para añadir un interceptor `Authorization: Bearer`. Hoy cualquier cliente que conozca las rutas puede invocar el backend, incluidas las de administración. Recomendación: JWT emitido en el login, guardado en `EncryptedSharedPreferences` e inyectado con un `Interceptor` de OkHttp, con autorización por rol en el backend.

### 3. HTTP sin cifrar
Permitido solo para desarrollo con el emulador. Para producción: HTTPS, quitar `usesCleartextTraffic` y, si hace falta, un `network_security_config.xml` que permita HTTP solo para `10.0.2.2` en `debug`.

### 4. Logs con datos sensibles
`HttpLoggingInterceptor.Level.BODY` escribe en Logcat las credenciales del login y los datos personales de las solicitudes. Usar `BODY` solo en `BuildConfig.DEBUG` y `NONE` en release.

### 5. Autorización solo en la interfaz
El cliente comprueba el rol al abrir ciertas pantallas, y no todas lo hacen (ver [Roles y sesión](04-roles-y-sesion.md)). Es una ayuda de UX, no un control de seguridad.

### 6. Contraseñas
- El cliente envía la contraseña en texto plano al backend (correcto sobre HTTPS; el hash debe hacerse en el servidor con bcrypt o argon2).
- `security/PasswordHasher.kt` aplica SHA-256 sin sal y no se usa en ninguna pantalla; debería eliminarse para evitar un falso sentido de seguridad.
- Política de contraseña implementada en `RegisterActivity`: mínimo 8 caracteres con mayúscula, minúscula y número.

### 7–9. Hardening menor
- `allowBackup="false"` o reglas explícitas en `backup_rules.xml` / `data_extraction_rules.xml`.
- Quitar `READ/WRITE_EXTERNAL_STORAGE`: no se necesitan con `getExternalFilesDir`.
- Activar `isMinifyEnabled = true` y reglas ProGuard/R8 en `release` (Gson requiere reglas para las clases con `@SerializedName`).

## Datos personales tratados

La app maneja nombre, email, teléfono, documento de identidad, direcciones y ubicación (coordenadas) de clientes, remitentes y receptores. Si se usa fuera del entorno académico se requiere política de privacidad, consentimiento y cumplimiento de la normativa local de protección de datos.

## Lo que sí está bien

- `SharedPreferences` en modo privado y sin contraseñas almacenadas.
- Actividades no exportadas (solo la de entrada).
- `FileProvider` para compartir los PDF en lugar de `file://`.
- Solicitud del permiso de ubicación y de notificaciones en tiempo de ejecución.
