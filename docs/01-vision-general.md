# 1. Visión general

## Propósito

Aplicación Android multirol para gestionar el ciclo completo de un envío: el cliente solicita la recolección, la sucursal asigna a un conductor, el conductor recoge y entrega, y cualquier persona puede rastrear el envío con el número de guía.

Proyecto académico del curso Convergentes (Universidad Piloto de Colombia, 7.º semestre), desarrollado en equipo.

## Roles y qué puede hacer cada uno

| Rol | Pantalla de entrada | Capacidades principales |
|-----|--------------------|-------------------------|
| **Público (sin sesión)** | `MainActivity` | Rastrear un envío por número de guía |
| **CLIENTE** | `ClientDashboardActivity` | Crear solicitudes, ver activas y finalizadas, cancelar, confirmar entrega, descargar la guía PDF |
| **GESTOR** | `AssignDriverActivity` (tras login) o `ManagerDashboardActivity` (sesión ya abierta) | Ver solicitudes de su sucursal y asignar conductor |
| **CONDUCTOR** | `DriverDashboardActivity` | Ver rutas asignadas, avanzar el estado, abrir Google Maps, odómetro GPS |
| **FUNCIONARIO / ANALISTA** | `ManagerDashboardActivity` | Ver solicitudes pendientes y asignadas de su sucursal (pestañas) |
| **ADMIN** | `AdminPanelActivity` | Estadísticas globales, gestión de sucursales, usuarios logísticos y todas las solicitudes |

Detalle de permisos en [Roles y sesión](04-roles-y-sesion.md).

## Funcionalidades

- Registro e inicio de sesión (con validación de contraseña y de email).
- Creación de solicitudes de envío con mapa: dirección de recolección (GPS, búsqueda o toque en el mapa), dirección de entrega, remitente, receptor y paquete.
- Asignación automática de la sucursal más cercana a la dirección de recolección.
- Generación y descarga de la guía en PDF, abierta con un visor externo.
- Seguimiento público por número de guía.
- Gestión de sucursales (crear, editar, eliminar, buscar) con selección de ubicación en el mapa.
- Gestión de usuarios logísticos (alta, edición, eliminación).
- Panel de administración con contadores por estado.
- Notificaciones locales y odómetro por GPS para el conductor.

## Stack

| Área | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.2.0 (más un adaptador en Java) |
| UI | Vistas XML, ViewBinding (activado; usado en `MainActivity`, `LoginActivity`, `ClientDashboardActivity`) y `findViewById` en el resto, Material Components 1.12 |
| Arquitectura | Capas con MVVM parcial (ViewModel + LiveData + corrutinas) |
| Red | Retrofit 2.11, OkHttp 4.12, Gson, `HttpLoggingInterceptor` |
| Mapas y ubicación | Google Maps SDK (`play-services-maps`), Fused Location (`play-services-location`), `Geocoder` |
| Persistencia local | `SharedPreferences` (solo sesión) |
| Otros | `android-mail` 1.6.7 (declarado, sin uso encontrado), Navigation 2.9.5 (plantilla sin uso real), GridLayout |
| Build | Gradle (Kotlin DSL), AGP 8.13.1, `compileSdk`/`targetSdk` 36, `minSdk` 24, Java 11 |

## Alcance y estado

Es un **producto mínimo viable** (versión `1.0`, `versionCode 1`). El flujo principal cliente → gestor → conductor → entrega funciona contra el backend local. Varias piezas son simuladas o están incompletas; ver [Deuda técnica](11-deuda-tecnica-y-roadmap.md).

## Relación con el backend

```
┌──────────────────┐   HTTP/JSON    ┌───────────────────────────┐   JDBC   ┌───────┐
│  App Android     │ ─────────────► │ Spring Boot (Kotlin)      │ ───────► │ MySQL │
│  (este repo)     │  /api/v1/...   │ puerto 8080               │          │       │
└──────────────────┘                └───────────────────────────┘          └───────┘
```

El emulador accede al `localhost` del equipo anfitrión mediante `10.0.2.2`. La URL base está fija en `dto/RetrofitClient.kt`.
