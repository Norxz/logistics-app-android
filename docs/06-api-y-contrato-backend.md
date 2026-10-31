# 6. API y contrato con el backend

## Conexión

| Dato | Valor |
|------|-------|
| URL base | `http://10.0.2.2:8080/api/v1/` (constante `BASE_URL` en `dto/RetrofitClient.kt`) |
| Formato | JSON (Gson) |
| Autenticación | Ninguna: las peticiones no llevan token |
| Logging | `HttpLoggingInterceptor` nivel `BODY` |
| Tiempos de espera | Valores por defecto de OkHttp (10 s) |

El backend es un proyecto Spring Boot + Kotlin con MySQL (`logistica_db`, puerto 3306). Los controllers están en `co.edu.unipiloto.backend.controller` (`AuthController`, `SolicitudController`, `SucursalController`, `UserController`, `PdfController`).

## Interfaces Retrofit

| Interfaz | Acceso | Dominio |
|----------|--------|---------|
| `AuthApi` | `RetrofitClient.getAuthApi()` | Registro y login |
| `SolicitudApi` | `getSolicitudApi()` / `solicitudService` | Solicitudes de envío |
| `SucursalApi` | `getSucursalApi()` / `sucursalService` | Sucursales |
| `UserApi` | `getUserApi()` | Usuarios logísticos |
| `GuideApi` | `getGuideApi()` | Descarga de guía PDF |

## Endpoints y estado de coincidencia

Leyenda: ✅ existe en el backend · ❌ no existe · ⚠️ existe con otra ruta · 🗑 sin uso en el cliente.

Comparación hecha leyendo los controllers del backend (rama `Cambiando-logica-back`) contra las interfaces del cliente.

### Autenticación (`/auth`)

| Método | Ruta | Cuerpo → respuesta | Estado |
|--------|------|--------------------|--------|
| POST | `auth/register` | `RegisterRequest` → `UserResponse` | ✅ |
| POST | `auth/login` | `LoginRequest` → `UserResponse` | ✅ |

`401` en el login se traduce a "Credenciales inválidas…". Un email repetido en el registro se detecta en `RegisterActivity` por el código de respuesta.

### Solicitudes (`/solicitudes`)

| Método | Ruta | Uso en el cliente | Estado |
|--------|------|-------------------|--------|
| POST | `solicitudes` | Crear solicitud (`SolicitudRequest` → `SolicitudResponse`) | ✅ |
| GET | `solicitudes` | Todas las solicitudes (admin) | ✅ |
| GET | `solicitudes/client/{clientId}` | Dashboard del cliente | ✅ |
| GET | `solicitudes/tracking/{trackingNumber}` | Rastreo público | ✅ |
| GET | `solicitudes/{id}/pdf` | Guía PDF | ✅ |
| GET | `solicitudes/driver/{driverId}/routes` | Rutas del conductor (versiones `suspend` y `Call`) | ✅ |
| GET | `solicitudes/branch/{sucursalId}` | Solicitudes de la sucursal (versiones `suspend` y `Call`) | ✅ |
| GET | `solicitudes/branch/{sucursalId}/assigned` | Asignadas de la sucursal | ✅ |
| GET | `solicitudes/branch/{sucursalId}/completed` | Pestaña "Completadas" (`BranchCompletedFragment`, sin uso) | ❌ |
| PUT | `solicitudes/{id}/estado` | Cambiar estado, cuerpo `{"estado": "..."}` → 204 | ✅ |
| PUT | `solicitudes/{id}/status` | `RequestDetailActivity` (`actualizarEstado`) | ❌ (el nombre correcto es `/estado`) |
| POST | `solicitudes/{id}/asignar-gestor/{gestorId}` | Asignar gestor | ✅ |
| POST | `solicitudes/{id}/asignar-conductor?gestorId&conductorId` | Asignar conductor (repositorio) | ✅ |
| PUT | `solicitudes/{id}/assign-driver` | Asignar conductor `{"recolectorId": "…"}` (`AssignDriverActivity`) | ✅ |
| PUT | `api/v1/solicitudes/{id}/assign` | `SolicitudRepository.assignDriver` | ❌ (además duplica el prefijo `api/v1/`) |
| PUT | `api/v1/solicitudes/{id}/assignGestor` | `assignGestorEndpoint` | ❌ 🗑 |

### Sucursales (`/sucursales`)

| Método | Ruta | Uso | Estado |
|--------|------|-----|--------|
| GET | `sucursales` | Listado (registro, admin) | ✅ |
| POST | `sucursales` | Crear (`SucursalRequest`) | ✅ |
| GET | `sucursales/{id}` | Obtener | ✅ |
| PUT | `sucursales/{id}` | Actualizar | ✅ |
| DELETE | `sucursales/{id}` | Eliminar (204) | ✅ |
| GET | `sucursales/cercana?lat&lon` | Sucursal más cercana a la recolección | ✅ |

### Usuarios (`/users`)

| Método | Ruta | Uso | Estado |
|--------|------|-----|--------|
| GET | `users/logistic` | Lista de usuarios logísticos | ✅ |
| GET | `users/gestores/sucursal/{id}` | Gestores de la sucursal | ✅ |
| GET | `users/conductores/sucursal/{id}` | Conductores de la sucursal | ✅ |
| GET | `users/drivers/available` | Conductores para asignar (`RequestDetailActivity`) | ✅ |
| PUT | `users/{userId}/sucursal/{sucursalId}` | Asignar sucursal | ✅ |
| PUT | `users/{userId}/desactivar` | Desactivar usuario | ✅ |
| DELETE | `users/{userId}` | Eliminar usuario (204) | ✅ |
| GET | `users/logistic/{id}` | Cargar usuario para editar (`EditLogisticUserActivity`) | ❌ |
| PUT | `users/logistic/{id}` | Guardar edición | ❌ |
| DELETE | `users/logistic/{id}` | Eliminar desde las pantallas de gestión | ⚠️ existe `DELETE users/{userId}` |
| GET | `api/v1/users/conductores` | `UserRepository.getDrivers` | ❌ (duplica el prefijo) |
| GET | `drivers/available` | `getAvailableDriverBySucursal` | ❌ 🗑 |
| GET | `sucursales` (en `UserApi`) | Duplicado de `SucursalApi` | ✅ |

### Guía PDF

| Método | Ruta | Uso | Estado |
|--------|------|-----|--------|
| GET | `guia/download/{id}` | `GuideApi.downloadGuidePdf` (`@Streaming`), implementado en `GuideRepository` | ✅ (el backend lo expone como `/api/v1/guia/download/{id}` en `PdfController`); sin uso desde las pantallas |

> La app usa `solicitudes/{id}/pdf` para descargar la guía, no `guia/download/{id}`.

### Resumen

- Coinciden: autenticación completa, sucursales completo, la mayoría de solicitudes y de usuarios.
- Fallan con 404 en uso real: cambio de estado desde `RequestDetailActivity`, edición y borrado de usuarios logísticos, pestaña "Completadas" (sin pantalla que la use), `assignDriver` del repositorio.
- Sin uso: `assignGestorEndpoint`, `getAvailableDriverBySucursal`, `getDrivers` / `getAllConductores`.

Plan de corrección en [Deuda técnica](11-deuda-tecnica-y-roadmap.md).

## Cuerpos de petición relevantes

**Login** — `POST auth/login`
```json
{ "email": "ana@correo.com", "password": "Secreta123" }
```

**Registro** — `POST auth/register`
```json
{
  "fullName": "Ana Pérez", "email": "ana@correo.com", "password": "Secreta123",
  "phoneNumber": "3001234567", "role": "CLIENTE", "sucursalId": null, "isActive": true
}
```

**Crear solicitud** — `POST solicitudes`
```json
{
  "clientId": 12,
  "remitente": { "nombre": "Ana", "tipoId": "CC", "numeroId": "1000", "telefono": "3001234567", "codigoPais": "+57" },
  "receptor":  { "nombre": "Luis", "tipoId": "CC", "numeroId": "2000", "telefono": "3109876543", "codigoPais": "+57" },
  "direccionRecoleccion": { "direccionCompleta": "Cra 7 #40-10", "ciudad": "Bogotá", "latitud": 4.63, "longitud": -74.06, "pisoApto": null, "notasEntrega": null },
  "direccionEntrega":     { "direccionCompleta": "Cl 80 #20-15", "ciudad": "Bogotá", "latitud": 4.68, "longitud": -74.05, "pisoApto": "Apto 301", "notasEntrega": "Portería" },
  "paquete": { "peso": 2.5, "alto": 20.0, "ancho": 30.0, "largo": 40.0, "contenido": "Libros" },
  "fechaRecoleccion": "2025-01-01",
  "franjaHoraria": "Mañana (8-12)",
  "sucursalId": 1
}
```

**Cambiar estado** — `PUT solicitudes/{id}/estado`
```json
{ "estado": "EN_RUTA_RECOLECCION" }
```

**Asignar conductor (gestor)** — `PUT solicitudes/{id}/assign-driver`
```json
{ "recolectorId": "7" }
```

## Manejo de errores en el cliente

| Situación | Mensaje o comportamiento |
|-----------|-------------------------|
| `401` en login | "Credenciales inválidas. Verifica tu email y contraseña." |
| Otro error HTTP | Se muestra el cuerpo del error o "Error de servidor (código)" |
| Excepción de red | "Error de conexión…" (los repositorios prefijan el texto de la excepción) |
| `404` en rastreo | "La guía … no fue encontrada o es inválida" |
| Sucursal más cercana no encontrada | "Error al buscar sucursal: …" y no se crea la solicitud |

## Nota sobre tráfico en claro

`usesCleartextTraffic="true"` está activado en el manifest para permitir `http://10.0.2.2`. En producción debe sustituirse por HTTPS. Ver [Seguridad](09-seguridad.md).
