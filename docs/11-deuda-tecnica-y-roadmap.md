# 11. Deuda técnica y hoja de ruta

Lista priorizada de lo encontrado al revisar el código. Cada punto indica dónde está y cómo resolverlo.

## P0 — Bloqueantes

| # | Problema | Dónde | Solución |
|---|----------|-------|----------|
| 1 | No compila desde un checkout limpio: faltan `services.NotificationService` y `services.OdometerService` | `DriverDashboardActivity.kt`, `AndroidManifest.xml` | Restaurar las clases (están referenciadas con `EXTRA_MESSAGE`, `OdometerBinder`, `getDistance()`, `getOdometer()`, `updateConfig(tiempo, distancia)`) o quitar sus usos |
| 2 | API key de Google Maps commiteada | `AndroidManifest.xml` | Ver [Seguridad](09-seguridad.md) |

## P1 — Funcionalidad rota o inconsistente

| # | Problema | Dónde | Solución |
|---|----------|-------|----------|
| 3 | Endpoints del cliente que no existen en el backend | `SolicitudApi`, `UserApi` | Tabla en [API](06-api-y-contrato-backend.md); corregir rutas del cliente o añadir endpoints al backend |
| 4 | Cambio de estado desde `RequestDetailActivity` usa `solicitudes/{id}/status` | `SolicitudApi.actualizarEstado` | Usar `updateEstado` (`/estado`) |
| 5 | Edición y borrado de usuarios logísticos usan `users/logistic/{id}` | `UserApi`, `EditLogisticUserActivity`, `LogisticUserManagementActivity` | Crear `GET/PUT users/logistic/{id}` en el backend y usar `DELETE users/{id}` |
| 6 | Estados que no existen en el backend: `FINALIZADA`, `PENDIENTE_ASIGNACION`; `RequestDetailActivity` ofrece `PENDIENTE/ASIGNADO/EN RUTA/COMPLETADO/CANCELADO` | `ClientDashboardActivity`, `AssignDriverActivity`, `RequestDetailActivity` | Centralizar los estados en un `enum class EstadoSolicitud` que replique el del backend |
| 7 | El `GESTOR` entra a pantallas distintas según el camino; `ManagerDashboardActivity` lo rechaza | `LoginActivity`, `MainActivity`, `ManagerDashboardActivity` | Definir un único mapa rol → pantalla |
| 8 | Extra de admin con clave distinta (`IS_ADMIN_REGISTERING` vs `IS_ADMIN_REGISTER`) | `ViewLogisticUsersActivity` | Usar `RegisterActivity.EXTRA_IS_ADMIN_REGISTER` |
| 9 | `fechaRecoleccion` fija en `"2025-01-01"` | `SolicitudActivity` | Añadir selector de fecha |
| 10 | "Enviar guía por correo" es simulado | `GuideConfirmationActivity` | Endpoint de envío en el backend |
| 11 | `SolicitudResponse.toModel()` rellena con "N/A" | `dto/SolicitudResponseMappers.kt` | Trabajar con el DTO o pedir al backend un DTO de detalle completo |

## P2 — Arquitectura

| # | Problema | Solución |
|---|----------|----------|
| 12 | Pantallas que llaman a Retrofit directamente (`AssignDriver`, `RequestDetail`, `EditLogisticUser`, `LogisticUserManagement`, `ViewLogisticUsers`, `ViewAllRequests`, `DriverDashboard`, `AdminPanel`, `TrackShipping`, `GuideConfirmation`, fragmentos de rama) | Crear ViewModels y pasar por los repositorios |
| 13 | Dos estilos de asincronía (`suspend` y `Call.enqueue`); endpoints declarados dos veces | Migrar todo a `suspend` y borrar las versiones `Call` |
| 14 | `handleApiCall` copiado en tres repositorios | Extraer a una función compartida (extensión o clase base) |
| 15 | Inyección manual repetida en cada pantalla | Introducir Hilt o un `AppContainer` |
| 16 | `RetrofitClient` en el paquete `dto` y con propiedades duplicadas (`solicitudService`, `getSolicitudApi()`) | Moverlo a `api/` o `network/` y dejar un único acceso |
| 17 | Mezcla de ViewBinding y `findViewById` | Usar ViewBinding en todo |
| 18 | `LocationHelper` usa hilos manuales (`Thread` + `Handler`) y método síncrono `reverseGeocode` sin uso | Sustituir por corrutinas (`Dispatchers.IO`) |
| 19 | Textos escritos en el código y `Toast` en español dispersos | Mover a `strings.xml` |
| 20 | Nombres mezclados español/inglés (`Solicitud`/`Request`, `Sucursal`/`Branch`, `Recolector`/`Driver`) | Elegir un idioma por capa |

## P3 — Limpieza

| # | Elemento | Acción |
|---|----------|--------|
| 21 | `nav_graph.xml` (plantilla con fragmentos inexistentes) y dependencias de Navigation | Eliminar o implementar |
| 22 | `BranchPagerAdapter` y fragmentos `Branch*` sin uso | Integrarlos o borrarlos |
| 23 | `ViewLogisticUsersActivity` vs `LogisticUserManagementActivity` (casi iguales) | Unificar |
| 24 | `PasswordHasher`, `ShippingStatus.displayResults`, `DireccionResponse`, `AssignedSolicitudResponse` sin uso | Borrar |
| 25 | `activity_edit_user.xml` con espacio de nombres `res-app` | Corregir o borrar |
| 26 | Dependencias duplicadas o sin uso (`android-mail`, `play-services-maps` x2, Navigation x2) | Limpiar `build.gradle.kts` |
| 27 | Permisos de almacenamiento obsoletos; `allowBackup="true"` | Ver [Seguridad](09-seguridad.md) |
| 28 | Coordenadas fijas del botón de mapa del conductor | Usar la ubicación actual o la dirección de la ruta |
| 29 | Comentarios de depuración (`🚨 CORRECCIÓN…`) y emojis en KDoc | Limpiar |
| 30 | Pruebas de plantilla | Ver [Pruebas](10-pruebas-y-calidad.md) |

## Hoja de ruta sugerida

**Fase 1 — Compilable y coherente (1–2 días)**
- Restaurar el paquete `services`.
- Rotar la API key y moverla fuera del repositorio.
- Alinear rutas cliente/backend (puntos 3–5) y unificar estados (6).

**Fase 2 — Seguridad básica (2–3 días)**
- JWT + HTTPS, `EncryptedSharedPreferences`, logs solo en debug.
- Autorización por rol en el backend.

**Fase 3 — Arquitectura (1 semana)**
- ViewModels para todas las pantallas, todo en corrutinas, un único `handleApiCall`.
- Hilt, ViewBinding en todo, mover textos a `strings.xml`.

**Fase 4 — Producto (continuo)**
- Selector de fecha de recolección, envío real de guía por correo, tracking en mapa en tiempo real, historial de estados por solicitud, notificaciones push (FCM).
- Pruebas unitarias e instrumentadas, CI con GitHub Actions (build + lint + test).
- Minificación y firma de `release`.
