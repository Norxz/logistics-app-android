# 7. Modelos y DTOs

Todos son `data class` de Kotlin con `@SerializedName` (Gson). Las fechas llegan como `String` (ISO 8601, el backend serializa `Instant`).

## Modelos de dominio (`model/`)

### `User`
| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `Long?` | |
| `documento` | `String?` | |
| `fullName` | `String` | |
| `email` | `String` | |
| `phoneNumber` | `String?` | |
| `role` | `String` | Valor de `Role` |
| `sucursal` | `Sucursal?` | Solo personal logístico |
| `fechaCreacion`, `ultimoLogin` | `String?` | |
| `isActive` | `Boolean` | Por defecto `true` |

### `Solicitud`
| Campo | Tipo |
|-------|------|
| `id` | `Long?` |
| `client` | `User` |
| `remitente`, `receptor` | `Cliente` |
| `sucursal` | `Sucursal` |
| `direccionRecoleccion` | `Direccion?` |
| `direccionEntrega` | `Direccion` |
| `paquete` | `Paquete` |
| `guia` | `Guia` |
| `conductor`, `gestor`, `funcionario` | `User?` |
| `fechaAsignacionConductor`, `fechaRecoleccionReal`, `fechaEntregaReal` | `String?` |
| `fechaRecoleccion`, `franjaHoraria` | `String` (programadas) |
| `estado` | `String` |
| `createdAt` | `String` |
| `motivoCancelacion` | `String?` |

### Otros modelos
| Modelo | Campos |
|--------|--------|
| `Cliente` | `id`, `nombre`, `tipoId`, `numeroId`, `telefono`, `codigoPais`, `tipoCliente`, `fechaCreacion` |
| `Direccion` | `id`, `direccionCompleta`, `ciudad`, `latitud`, `longitud`, `pisoApto`, `notasEntrega`, `barrio`, `codigoPostal`, `tipoDireccion` |
| `Guia` | `id`, `numeroGuia`, `trackingNumber`, `fechaCreacion`, `costoEnvio`, `estadoGuia` (`"CREADA"` por defecto), `ultimaActualizacion` |
| `Paquete` | `id`, `peso`, `alto`, `ancho`, `largo`, `contenido`, `categoria` |
| `Sucursal` | `id`, `nombre`, `direccion` |

## DTOs de petición (`dto/`)

| DTO | Se usa en | Campos |
|-----|-----------|--------|
| `LoginRequest` | `POST auth/login` | `email`, `password` |
| `RegisterRequest` | `POST auth/register` | `fullName`, `email`, `password`, `phoneNumber?`, `role`, `sucursalId?`, `isActive` |
| `SolicitudRequest` | `POST solicitudes` | `clientId`, `remitente`, `receptor`, `direccionRecoleccion?`, `direccionEntrega`, `paquete`, `fechaRecoleccion`, `franjaHoraria`, `sucursalId` |
| `ClienteRequest` | dentro de `SolicitudRequest` | `id?`, `nombre`, `tipoId?`, `numeroId?`, `telefono?`, `codigoPais?` |
| `DireccionRequest` | dentro de `SolicitudRequest` y `SucursalRequest` | igual que `Direccion` sin `id` |
| `PaqueteRequest` | dentro de `SolicitudRequest` | `peso`, `alto?`, `ancho?`, `largo?`, `contenido?` |
| `SucursalRequest` | `POST/PUT sucursales` | `nombre`, `direccion` |

## DTOs de respuesta

| DTO | Campos |
|-----|--------|
| `UserResponse` | `id`, `fullName`, `email`, `phoneNumber?`, `role`, `sucursal: SucursalResponse?`, `isActive` |
| `SucursalResponse` | `id`, `nombre`, `direccion: Direccion` |
| `SolicitudResponse` | `id`, `clientId`, `estado`, `fechaRecoleccion`, `franjaHoraria`, `direccionRecoleccion: String?`, `direccionCompleta: String`, `guia: GuiaResponse`, `recolectorId?`, `recolectorName?`, `createdAt?` |
| `AssignedSolicitudResponse` | Igual que `SolicitudResponse` sin `direccionRecoleccion` |
| `GuiaResponse` | `numeroGuia`, `trackingNumber`, `fechaCreacion` |
| `DireccionResponse` | `id`, `direccionCompleta`, `ciudad`, `latitud`, `longitud`, `pisoApto?`, `notasEntrega?` (sin referencias en el código revisado) |
| `ShippingStatus` | Objeto local del rastreo: `status`, `trackingNumber`, `destinationAddress`, `estimatedDate`, `timeFranja` |

## Mapeos (`dto/*Mappers.kt`)

| Función | Resultado |
|---------|-----------|
| `SucursalResponse.toModel()` | `Sucursal` |
| `UserResponse.toModel()` | `User` (solo `id`, `fullName`, `email`, `role`, `sucursal`) |
| `SolicitudResponse.toModel()` | `Solicitud` con **relleno** |

### Qué rellena `SolicitudResponse.toModel()`

El DTO de la lista no incluye todo el detalle, así que el mapeo inventa lo que falta:

| Campo del modelo | Valor |
|------------------|-------|
| `client` | `User` con nombre `"Cliente ID: {clientId}"` y email sintético |
| `conductor` | `User` con `recolectorName` si hay `recolectorId`; si no, `null` |
| `remitente`, `receptor` | `Cliente(nombre = "N/A")` |
| `sucursal` | `Sucursal(nombre = "N/A")` |
| `direccionRecoleccion`, `direccionEntrega` | `Direccion` con `direccionCompleta` del DTO y ciudad `"N/A"` |
| `paquete` | `Paquete(peso = 0.0)` |
| `guia.estadoGuia` | siempre `"ASIGNADA"` |

**Consecuencia:** una `Solicitud` obtenida por la red no tiene datos reales de remitente, receptor, paquete ni sucursal. La UI solo debe leer `id`, `estado`, `fechaRecoleccion`, `franjaHoraria`, `guia`, `conductor` y `createdAt` de ella.

## Estados del `Result` y de UI

| Tipo | Valores |
|------|---------|
| `AuthViewModel.AuthState` | `Idle`, `Success(user)`, `Error(message)` |
| Repositorios | `Result<T>` de Kotlin (`success` / `failure`) |
| Resultados puntuales de ViewModels | `LiveData<Result<…>>`, `LiveData<String?>` (error / éxito) |
