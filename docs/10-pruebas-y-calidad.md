# 10. Pruebas y calidad

## Estado actual

| Tipo | Archivo | Contenido |
|------|---------|-----------|
| Unitarias | `app/src/test/.../ExampleUnitTest.java` | Plantilla de Android Studio (`2 + 2 = 4`) |
| Instrumentadas | `app/src/androidTest/.../ExampleInstrumentedTest.java` | Plantilla (comprueba el `applicationId`) |

No hay pruebas de ViewModels, repositorios, mapeos ni UI. La verificación hasta hoy ha sido manual contra el backend local.

## Dónde conviene probar primero

La lógica con más valor y menos dependencias de Android:

| Objetivo | Qué probar | Tipo |
|----------|-----------|------|
| `SolicitudViewModel.isSolicitudActiva` | Clasificación de estados (incluye `FINALIZADA`) | Unitaria |
| `SolicitudResponse.toModel()` | Que los valores de relleno sean los esperados | Unitaria |
| `LoginActivity.isRoleAuthorized` | Matriz de roles por puerta de entrada | Unitaria (extraer a función pura) |
| `RegisterActivity.isValidPassword` | Reglas de contraseña | Unitaria (extraer a función pura) |
| `DriverDashboardActivity.getNextStatus` | Cadena de estados del conductor | Unitaria (extraer a función pura) |
| `AuthRepository`, `SolicitudRepository`, `SucursalRepository` | Traducción de `Response` a `Result` (200, 401, 500, excepción) | Unitaria con `MockWebServer` |
| `AuthViewModel`, `NewRequestCreationViewModel`, `ManagerDashboardViewModel` | Transiciones de estado con repositorio simulado | Unitaria con `kotlinx-coroutines-test` y `InstantTaskExecutorRule` |
| Login → dashboard por rol | Navegación | Instrumentada (Espresso) |
| Crear solicitud | Formulario + validaciones | Instrumentada (Espresso) |

Dependencias sugeridas: `kotlinx-coroutines-test`, `androidx.arch.core:core-testing`, `com.squareup.okhttp3:mockwebserver`, `mockk` o `mockito-kotlin`.

## Prueba de contrato con el backend

La principal fuente de errores observada es la desalineación de rutas entre cliente y backend (ver [API](06-api-y-contrato-backend.md)). Una forma barata de evitarla es una prueba con `MockWebServer` que, para cada método de las interfaces Retrofit, verifique método HTTP y ruta contra una lista mantenida junto a los controllers del backend; o publicar la especificación OpenAPI del backend y generar el cliente a partir de ella.

## Calidad estática

- No hay configuración de `ktlint`, `detekt` ni Android Lint personalizada.
- Hay comentarios de depuración residuales (`🚨 CORRECCIÓN…`, `Verifica este endpoint…`) y bloques duplicados que conviene limpiar.
- El proyecto no tiene integración continua.

## Lista de verificación manual (humo)

1. Registrar un cliente con contraseña válida e iniciar sesión.
2. Crear una solicitud (recolección en mapa, entrega, paquete). Verificar que se asigna sucursal y se genera la guía.
3. Descargar la guía PDF y abrirla.
4. Iniciar sesión como gestor/conductor de la misma sucursal; asignar el conductor.
5. Como conductor, avanzar el estado hasta `ENTREGADA` y comprobar la notificación.
6. Rastrear la guía sin sesión y comprobar el estado.
7. Como admin: ver contadores, crear una sucursal, registrar y editar un usuario logístico.
8. Cancelar una solicitud como cliente.
