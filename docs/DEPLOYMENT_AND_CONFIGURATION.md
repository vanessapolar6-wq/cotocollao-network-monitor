# Cotocollao Network Monitor – Guía de Instalación, Arquitectura y Despliegue

## 1. Arquitectura General
- **Aplicación Android (Kotlin + Jetpack Compose + Material 3)**:
  - Autenticación con Google Sign-In vía Jetpack Credential Manager y Firebase Authentication.
  - Persistencia en Cloud Firestore (aislada por perfil y validada con reglas Zero-Trust en `firestore.rules`).
  - Comunicación HTTPS vía Retrofit hacia el Backend REST Node.js (`GET /api/dashboard`, `GET /api/agencies`, `GET /api/devices`, `GET /api/incidents`, `GET /api/reports/availability`).
  - Modo **DEMO** aislado en memoria (identificado siempre con etiqueta `DEMO` y sin mezclar con registros reales).
- **Backend REST (`backend/`)**:
  - Node.js 20+ listo para **Google Cloud Run** (`backend/Dockerfile`).
  - Endpoints para el colector: `POST /api/monitoring/heartbeat`, `POST /api/monitoring/metrics`, `POST /api/monitoring/events`.
  - Endpoints para la app móvil: `GET /api/dashboard`, `GET /api/agencies`, `GET /api/devices`, `GET /api/incidents`, `GET /api/reports/availability`.
  - Servicio FCM (`backend/src/services/notificationService.js`) con control de duplicidad, agrupación y escalamiento automático.
- **Agente Colector On-Premise (`collector/`)**:
  - Se ejecuta dentro de la red institucional con conectividad privada hacia las 7 agencias.
  - Ejecuta ICMP (`ping`), consultas **SNMPv3 (authPriv SHA-256/AES)**, receptor **Syslog UDP** y envío cifrado HTTPS al backend usando `COLLECTOR_API_TOKEN`.
  - Las credenciales SNMPv3 y tokens de firewalls residen **exclusivamente en variables de entorno del servidor/colector**, nunca en la app Android.

## 2. Despliegue del Backend en Google Cloud Run
```bash
cd backend
gcloud run deploy cotocollao-noc-backend \
  --source . \
  --region us-east1 \
  --no-allow-unauthenticated \
  --set-env-vars "COLLECTOR_API_TOKEN=REEMPLAZAR_TOKEN_SEGURO"
```

## 3. Configuración del Colector On-Premise
```bash
cd collector
cp config.example.json config.json
npm install
export COLLECTOR_API_TOKEN="REEMPLAZAR_TOKEN_SEGURO"
export SNMPV3_USER="noc_monitor"
export SNMPV3_AUTH_KEY="CLAVE_AUTH_SHA256"
export SNMPV3_PRIV_KEY="CLAVE_PRIV_AES"
npm start
```

## 4. Notificaciones Push (Firebase Cloud Messaging)
Para habilitar envíos push reales desde el servidor:
1. En Firebase Console, habilite **Cloud Messaging** en su proyecto y descargue la cuenta de servicio para `GOOGLE_APPLICATION_CREDENTIALS` en Cloud Run.
2. En la app Android, registre el token FCM del dispositivo autorizado en el perfil `/users/{userId}` cuando agregue el cliente `firebase-messaging`.

## 5. Ejecución de Pruebas
- Reglas Firestore: `FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js`
- Backend Node.js: `node --test backend/test/api.test.js`
- Android (Unit + Robolectric): `gradle :app:testDebugUnitTest`
