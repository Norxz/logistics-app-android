# 4. Roles y sesión

## Roles del sistema

Definidos en el backend (`Role`) y usados como `String` en el cliente.

| Rol | Descripción |
|-----|-------------|
| `CLIENTE` | Solicita y rastrea envíos |
| `FUNCIONARIO` | Personal administrativo de sucursal |
| `GESTOR` | Revisa y asigna solicitudes a conductores dentro de su sucursal |
| `CONDUCTOR` | Recoge y entrega paquetes, actualiza el estado de la ruta |
| `ANALISTA` | Supervisión y análisis, sin funciones operativas |
| `ADMIN` | Acceso total |

## Inicio de sesión por "puerta de entrada"

`MainActivity` ofrece cuatro puertas de login. `LoginActivity` recibe el rol de la puerta y valida que el rol real del usuario sea compatible (`isRoleAuthorized`):

| Puerta elegida | Roles reales aceptados |
|----------------|-----------------------|
| Solicitar envío (`CLIENTE`) | `CLIENTE` |
| Funcionarios (`FUNCIONARIO`) | `FUNCIONARIO`, `ANALISTA` |
| Conductores (`CONDUCTOR`) | `CONDUCTOR`, `GESTOR` |
| Admin (`ADMIN`) | `ADMIN` |

Si el rol no es compatible, el login se rechaza.

## Destino según rol

| Rol | Tras login (`LoginActivity`) | Con sesión ya abierta (`MainActivity`) |
|-----|------------------------------|----------------------------------------|
| CLIENTE | `ClientDashboardActivity` | `ClientDashboardActivity` |
| CONDUCTOR | `DriverDashboardActivity` | `DriverDashboardActivity` |
| GESTOR | `AssignDriverActivity` | `ManagerDashboardActivity` |
| FUNCIONARIO / ANALISTA | `ManagerDashboardActivity` | `ManagerDashboardActivity` |
| ADMIN | `AdminPanelActivity` | `AdminPanelActivity` |
| otro | cierra sesión | cierra sesión |

> **Inconsistencia:** el `GESTOR` aterriza en pantallas distintas según cómo entre. Además `ManagerDashboardActivity` solo admite `FUNCIONARIO` y `ANALISTA`, así que un `GESTOR` con sesión abierta es expulsado de esa pantalla.

## Control de acceso por pantalla

El control se hace en el cliente, al abrir cada pantalla (`onCreate`):

| Pantalla | Condición |
|----------|-----------|
| `ClientDashboardActivity` | sesión iniciada y rol `CLIENTE` |
| `ManagerDashboardActivity` | sesión iniciada y rol `FUNCIONARIO` o `ANALISTA` |
| `AdminPanelActivity` | sesión iniciada y rol `ADMIN` |
| `ViewAllRequestsActivity` | rol `ADMIN` |
| Resto | sin comprobación explícita |

> Es solo una barrera de interfaz. La seguridad real debe aplicarla el backend; hoy no hay token en las peticiones (ver [Seguridad](09-seguridad.md)).

## `SessionManager`

Archivo: `storage/SessionManager.kt`. Guarda la sesión en `SharedPreferences` con nombre `LogiAppSession`, modo privado.

| Clave | Tipo | Descripción |
|-------|------|-------------|
| `isLoggedIn` | Boolean | Hay sesión activa |
| `userId` | Long | ID del usuario (`-1` si no hay) |
| `role` | String | Rol real |
| `name` | String | Nombre completo |
| `USER_EMAIL` | String | Email |
| `sucursal_name` | String? | Nombre de la sucursal (personal logístico) |
| `branch_id` | Long | ID de la sucursal |

| Método | Función |
|--------|---------|
| `createLoginSession(userId, role, sucursal, sucursalId, name, email)` | Guarda la sesión tras un login correcto |
| `isLoggedIn()`, `getUserId()`, `getRole()`, `getName()`, `getUserFullName()`, `getUserEmail()`, `getSucursal()` | Lectura |
| `getBranchId()` / `getSucursalId()` | ID de sucursal, `null` si no existe |
| `saveSucursalId(id)` | Guarda el ID de sucursal |
| `logoutUser()` / `logout()` | Borra todos los datos (alias) |

**Notas**
- `getName()` y `getUserFullName()` leen la misma clave; devuelven `""` y `null` respectivamente cuando no hay dato.
- `getBranchId()` y `getSucursalId()` son sinónimos.
- No se guarda token: el backend no emite ninguno hoy.
- Los datos están en texto claro dentro de `SharedPreferences` (no hay datos sensibles como contraseñas).
