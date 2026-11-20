# COTOCOLLAO NETWORK MONITOR

**Sistema de Monitoreo de Infraestructura Tecnológica** para la **Cooperativa de Ahorro y Crédito Cotocollao**.

## Agencias Monitoreadas (7)
1. MATRIZ (`AG-01`)
2. CALDERÓN (`AG-02`)
3. SANGOLQUÍ (`AG-03`)
4. SUR (`AG-04`)
5. POMASQUI (`AG-05`)
6. SAN ANTONIO (`AG-06`)
7. CENTRO (`AG-07`)

## Estructura del Proyecto
- `app/` — Aplicación nativa Android (Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit/Moshi, Firebase Auth + Credential Manager, Cloud Firestore).
- `backend/` — Servidor REST Node.js 20+ listo para Google Cloud Run (`Dockerfile`) con verificación de tokens Firebase y servicio de notificaciones push FCM (`notificationService.js`) con control de duplicidad y escalamiento.
- `collector/` — Agente colector on-premise para ejecución dentro de la red institucional (ICMP PING, SNMPv3 `authPriv`, Syslog UDP y reporte cifrado HTTPS).
- `firestore.rules` & `firebase-blueprint.json` — Reglas de seguridad Zero-Trust y esquema de entidades.
- `docs/DEPLOYMENT_AND_CONFIGURATION.md` — Guía completa de despliegue y configuración.
