# 12. Glosario

| Término | Significado |
|---------|-------------|
| **Solicitud** | Petición de envío creada por un cliente. Tiene remitente, receptor, direcciones, paquete, guía y estado |
| **Guía** | Documento asociado a una solicitud, con número de guía y número de rastreo (`trackingNumber`). Se descarga en PDF |
| **Tracking / rastreo** | Consulta pública del estado de un envío por su número de guía |
| **Sucursal** | Sede operativa con dirección y coordenadas. Se asigna a cada solicitud según cercanía a la recolección |
| **Remitente** | Persona que envía el paquete |
| **Receptor / destinatario** | Persona que recibe el paquete |
| **Recolección** | Recogida del paquete en la dirección del remitente |
| **Entrega / reparto** | Llegada del paquete a la dirección del receptor |
| **Franja horaria** | Rango del día para la recolección: Mañana (8-12), Tarde (12-6), Noche (6-10) |
| **Gestor** | Rol que revisa solicitudes de su sucursal y asigna conductores |
| **Funcionario** | Rol administrativo de sucursal |
| **Analista** | Rol de supervisión y análisis, sin funciones operativas |
| **Conductor / recolector** | Rol que recoge y entrega. El backend usa "recolector" en `recolectorId` y `recolectorName` |
| **Administrador** | Rol con acceso total a sucursales, usuarios y solicitudes |
| **Puerta de entrada** | Cada uno de los cuatro botones de login de `MainActivity`; agrupan roles compatibles |
| **Personal logístico** | Usuarios con rol distinto de `CLIENTE` (conductor, gestor, funcionario, analista) |
| **DTO** | *Data Transfer Object*; objeto que viaja por la API |
| **Backend** | Servicio Spring Boot (Kotlin) con MySQL, en repositorio aparte |
| **`10.0.2.2`** | Dirección especial del emulador Android que apunta al `localhost` del equipo anfitrión |
| **MVP** | Producto mínimo viable; estado actual del proyecto |
