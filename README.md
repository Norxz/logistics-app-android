# Logistics App Android

Aplicación Android multirol para la gestión de envíos y logística. Cubre el ciclo completo de una solicitud: el cliente la crea, el gestor de sucursal la asigna a un conductor y el conductor la recoge y entrega, con seguimiento por número de guía.

Proyecto académico desarrollado en equipo para la asignatura Convergentes (Universidad Piloto de Colombia, 7.º semestre).

## Documentación

La documentación completa está en [`docs/`](docs/README.md): arquitectura, pantallas, roles, flujos, contrato con el backend, modelos, configuración, seguridad, pruebas y deuda técnica.

## Funcionalidades

| Rol | Qué puede hacer |
|-----|-----------------|
| **Cliente** | Crear solicitudes de envío (paquete y direcciones), descargar la guía en PDF, rastrear el envío por número de guía |
| **Gestor / funcionario** | Ver solicitudes de su sucursal por estado (pendientes, asignadas, en ruta, completadas) y asignar conductores |
| **Conductor** | Ver sus rutas asignadas, actualizar el estado de las entregas, GPS, odómetro y notificaciones |
| **Administrador** | Gestionar sucursales y usuarios logísticos, ver estadísticas generales |

## Stack

- **Lenguaje:** Kotlin (más un adaptador en Java), interfaz con XML y ViewBinding
- **Arquitectura:** capas (`api`, `dto`, `repository`, `viewmodel`, `ui`, `adapters`, `model`, `storage`) con MVVM, LiveData y corrutinas
- **Red:** Retrofit 2, OkHttp 4, Gson
- **Mapas y ubicación:** Google Maps SDK y Fused Location (Play Services)
- **SDK:** `minSdk 24`, `targetSdk 36`
- **Backend:** API REST en Spring Boot (`/api/v1`), en un repositorio aparte

## Estructura

```
app/src/main/java/co/edu/unipiloto/myapplication/
├── api/          Interfaces Retrofit (Auth, Solicitud, Sucursal, User, Guide)
├── dto/          Requests/responses, mappers y RetrofitClient
├── repository/   Acceso a datos y manejo de errores
├── viewmodel/    Estado de UI y lógica de presentación
├── ui/           Activities por rol y pantalla
├── fragment/     Pestañas del gestor
├── adapters/     Adapters de RecyclerView / ViewPager
├── storage/      SessionManager (SharedPreferences)
└── utils/        Ubicación, notificaciones, archivos
```

## Cómo ejecutarla

1. Levanta el backend Spring Boot en el puerto `8080`.
2. Abre el proyecto en Android Studio y ejecútalo en un emulador. La URL base está en `RetrofitClient` (`http://10.0.2.2:8080/api/v1/`; `10.0.2.2` es el localhost del host visto desde el emulador).
3. Configura tu propia API key de Google Maps en `AndroidManifest.xml` (`com.google.android.geo.API_KEY`).

## Estado actual

Prototipo funcional (producto mínimo viable) de diciembre de 2025. Flujo cliente → gestor → conductor operativo contra el backend local.

### Limitaciones conocidas

- Faltan en el repositorio las clases `services.NotificationService` y `services.OdometerService` (referenciadas por el conductor y el manifest), por lo que no compila desde un checkout limpio hasta restaurarlas.
- La API key de Google Maps está en el manifest y en el historial de git; debe revocarse y moverse a `local.properties`.
- Seguridad de desarrollo: sin token de autenticación, tráfico HTTP sin cifrar, hash SHA-256 sin sal y logging HTTP en nivel `BODY`.
- Parte de la capa de red mezcla corrutinas y callbacks (`enqueue`), y algunas pantallas llaman a Retrofit sin pasar por ViewModel.
- Sin pruebas automatizadas (solo las plantillas por defecto).

### Próximos pasos

- Restaurar el paquete `services` y rotar la API key.
- Autenticación con JWT sobre HTTPS y contraseñas con bcrypt/argon2 en el backend.
- Unificar la capa de red en corrutinas y completar MVVM.
- Pruebas unitarias de ViewModels y repositorios.

## Equipo

Desarrollado por Lokcis, Andrés Espinosa y Norxz.
