# Documentación — Logistics App Android

Documentación técnica del cliente Android (`co.edu.unipiloto.myapplication`). El backend (Spring Boot + Kotlin + MySQL) vive en un repositorio aparte; aquí solo se documenta el contrato que este cliente consume.

> Última revisión del código: diciembre de 2025 (rama `main`, commit `656cd9c`).

## Índice

| # | Documento | Contenido |
|---|-----------|-----------|
| 1 | [Visión general](01-vision-general.md) | Qué hace la app, roles, alcance y stack |
| 2 | [Arquitectura](02-arquitectura.md) | Capas, paquetes, patrones, flujo de datos, inyección de dependencias |
| 3 | [Pantallas y navegación](03-pantallas-y-navegacion.md) | Catálogo de Activities/Fragments y mapa de navegación |
| 4 | [Roles y sesión](04-roles-y-sesion.md) | Roles, control de acceso y `SessionManager` |
| 5 | [Flujos de negocio](05-flujos-de-negocio.md) | Ciclo de vida de una solicitud y flujos por rol |
| 6 | [API y contrato con el backend](06-api-y-contrato-backend.md) | Endpoints, estado de coincidencia con el backend |
| 7 | [Modelos y DTOs](07-modelos-y-dtos.md) | Modelos de dominio, DTOs y mapeos |
| 8 | [Configuración y ejecución](08-configuracion-y-ejecucion.md) | Requisitos, build, permisos, cómo correr la app |
| 9 | [Seguridad](09-seguridad.md) | Estado actual, riesgos y recomendaciones |
| 10 | [Pruebas y calidad](10-pruebas-y-calidad.md) | Estado de las pruebas y estrategia sugerida |
| 11 | [Deuda técnica y hoja de ruta](11-deuda-tecnica-y-roadmap.md) | Problemas conocidos priorizados y próximos pasos |
| 12 | [Glosario](12-glosario.md) | Términos del dominio |
| — | [Tarjetas de trabajo](tarjetas-de-trabajo.csv) | 38 tarjetas (LOG-001 a LOG-038) para importar en Trello, Jira, Notion o Excel; derivadas del documento 11 |

## Cómo leer esta documentación

- Si llegas por primera vez: **1 → 2 → 3**.
- Si vas a integrar con el backend: **6 → 7 → 5**.
- Si vas a poner la app en producción: **9 → 11**.

## Convenciones

- Los nombres de clases, paquetes y endpoints se dejan tal cual aparecen en el código (en español e inglés mezclados).
- Las rutas de archivo son relativas a `app/src/main/java/co/edu/unipiloto/myapplication/` salvo que se indique otra cosa.
- Los apartados marcados como **Estado** describen lo que el código hace hoy, no lo que debería hacer.
