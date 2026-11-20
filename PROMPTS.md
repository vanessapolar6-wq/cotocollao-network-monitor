# PROMPT MAESTRO: DESARROLLO DE APLICACIÓN MÓVIL DE MONITOREO DE INFRAESTRUCTURA DE RED – COOPERATIVA COTOCOLLAO

## 1. ROL DEL DESARROLLADOR

Actúa como un arquitecto senior de software, especialista en redes y telecomunicaciones, ciberseguridad, infraestructura tecnológica, monitoreo de redes empresariales y desarrollo de aplicaciones móviles Android.

Desarrolla una aplicación móvil profesional, funcional y escalable para la Cooperativa de Ahorro y Crédito Cotocollao, destinada al monitoreo centralizado de los enlaces de internet y dispositivos de infraestructura tecnológica de todas sus agencias.

La aplicación debe funcionar como un centro de operaciones de red (NOC), proporcionando supervisión continua, alertas automáticas, registro de incidentes, estadísticas de disponibilidad y diagnóstico de fallas.

## 2. NOMBRE DE LA APLICACIÓN

**COTOCOLLAO NETWORK MONITOR**

Subtítulo: Sistema de Monitoreo de Infraestructura Tecnológica.

La aplicación debe utilizar la identidad visual institucional de la Cooperativa Cotocollao.

Colores institucionales:
- Naranja: color principal de identificación institucional.
- Blanco: color predominante de las pantallas.
- Gris oscuro: textos y elementos secundarios.
- Verde: equipos y enlaces operativos.
- Rojo: fallas críticas y equipos desconectados.
- Amarillo: advertencias y degradación del servicio.

Diseñar una interfaz moderna, minimalista, profesional, intuitiva y adaptable a teléfonos Android.

## 3. TECNOLOGÍAS DE DESARROLLO

Construir una aplicación Android nativa utilizando:

- Kotlin.
- Jetpack Compose.
- Material Design 3.
- Firebase Authentication para autenticación de usuarios.
- Cloud Firestore para almacenamiento de información.
- Firebase Cloud Messaging (FCM) para notificaciones push.
- API REST para comunicación con el servidor de monitoreo.
- Backend Node.js para procesamiento de información.
- Google Cloud Run para alojar los servicios backend, cuando la arquitectura de red lo permita.

La aplicación debe mantener una arquitectura modular, segura y escalable.

## 4. AGENCIAS QUE DEBEN SER MONITOREADAS

Crear automáticamente las siguientes siete agencias:

1. MATRIZ.
2. CALDERÓN.
3. SANGOLQUÍ.
4. SUR.
5. POMASQUI.
6. SAN ANTONIO.
7. CENTRO.

Cada agencia debe contar con una ficha individual que permita registrar:

- Nombre de la agencia.
- Dirección IP pública del enlace de internet.
- Dirección IP del router o gateway.
- Dirección IP del switch de capa 3.
- Dirección IP del firewall.
- Proveedor de internet.
- Ancho de banda contratado.
- Marca y modelo de los equipos.
- Número de serie.
- Versión de firmware.
- Estado de conectividad.
- Fecha y hora de última comprobación.

Los datos de direccionamiento IP y características de los equipos deben ser configurables desde un módulo de administración. No inventar direcciones IP ni credenciales.

## 5. DASHBOARD PRINCIPAL

Crear una pantalla inicial con indicadores en tiempo real:

- Total de agencias monitoreadas: 7.
- Agencias operativas.
- Agencias con incidencias.
- Enlaces de internet activos.
- Enlaces de internet caídos.
- Switches operativos.
- Switches con fallas.
- Firewalls operativos.
- Firewalls con fallas.
- Incidentes críticos pendientes.
- Disponibilidad general de la infraestructura.

Incluir indicadores visuales tipo semáforo:

VERDE: infraestructura operativa.

AMARILLO: degradación, latencia elevada o pérdida parcial de paquetes.

ROJO: enlace caído, equipo inaccesible o falla crítica.

GRIS: sin información o equipo no configurado.

Agregar gráficos de:
- Disponibilidad de internet por agencia.
- Evolución de incidentes durante los últimos 30 días.
- Disponibilidad mensual de los enlaces.
- Comparación de latencia entre agencias.
- Distribución de fallas por tipo de dispositivo.

Los indicadores deben calcularse utilizando información real obtenida del servidor de monitoreo. No mostrar datos ficticios como si fueran reales.

## 6. MONITOREO DE ENLACES DE INTERNET

Implementar supervisión continua mediante ICMP (PING) y pruebas adicionales de conectividad.

Monitorear:

- Disponibilidad del enlace.
- Tiempo de respuesta (latencia en milisegundos).
- Pérdida de paquetes.
- Tiempo de indisponibilidad.
- IP pública.
- Estado del gateway.
- Fecha y hora de la última conexión.

Configurar los siguientes parámetros iniciales, todos modificables:

- Intervalo de comprobación: 30 segundos.
- Tiempo máximo de respuesta: 3 segundos.
- Considerar caída cuando existan tres comprobaciones consecutivas fallidas.
- Considerar recuperación cuando existan dos comprobaciones consecutivas exitosas.

Identificar por separado una caída del enlace de internet y una falla de comunicación con un equipo interno.

## 7. MONITOREO DE SWITCHES CAPA 3

Implementar monitoreo mediante SNMPv3, utilizando credenciales seguras.

Obtener, cuando el fabricante y el dispositivo lo permitan:

- Estado de disponibilidad.
- Uso de CPU.
- Uso de memoria RAM.
- Temperatura.
- Estado de interfaces físicas.
- Velocidad de interfaces.
- Tráfico de entrada y salida.
- Errores de transmisión.
- Paquetes descartados.
- Estado de VLAN.
- Estado de interfaces troncales.
- Estado de puertos críticos.
- Tiempo de actividad (uptime).
- Reinicios inesperados.

Generar alertas cuando se detecten condiciones anormales.

## 8. MONITOREO DE FIREWALLS

Implementar integración mediante SNMPv3, API HTTPS o Syslog, según las capacidades del fabricante.

Monitorear:

- Disponibilidad del firewall.
- Estado de interfaces WAN y LAN.
- Uso de CPU y memoria.
- Temperatura, si está disponible.
- Estado de VPN site-to-site.
- Estado de túneles VPN.
- Eventos de reinicio.
- Errores de interfaces.
- Uso de ancho de banda.
- Cambios de estado de servicios críticos.
- Eventos de seguridad relevantes.

Registrar los eventos sin almacenar información sensible innecesaria, contraseñas ni contenido privado del tráfico.

## 9. SISTEMA DE NOTIFICACIONES AUTOMÁTICAS

Implementar Firebase Cloud Messaging para enviar notificaciones push a los teléfonos Android autorizados.

Las notificaciones deben generarse automáticamente ante:

1. Caída del enlace de internet de cualquier agencia.
2. Pérdida de conectividad del switch capa 3.
3. Pérdida de conectividad del firewall.
4. CPU superior al umbral configurado.
5. Memoria RAM superior al umbral configurado.
6. Temperatura fuera del rango permitido por el fabricante.
7. Pérdida de paquetes superior al umbral establecido.
8. Latencia excesiva.
9. Interrupción de una VPN.
10. Recuperación del servicio.

Cada notificación debe incluir:

- Nombre de la agencia.
- Equipo afectado.
- Tipo de incidente.
- Nivel de criticidad.
- Dirección IP del dispositivo, visible únicamente para usuarios autorizados.
- Fecha y hora.
- Descripción del problema.
- Duración de la interrupción, cuando corresponda.
- Estado actual.
- Recomendación técnica inicial.

Ejemplo de notificación:

ALERTA CRÍTICA – COTOCOLLAO NETWORK MONITOR

Agencia: CALDERÓN

Evento: ENLACE DE INTERNET CAÍDO.

Equipo afectado: Router principal.

Hora del incidente: 10:35:20.

Estado: SIN CONECTIVIDAD.

Acción requerida: Verificar proveedor de internet, gateway y conectividad WAN.

Enviar otra notificación cuando el servicio se recupere.

Implementar control de duplicidad de alertas, agrupación de incidentes y escalamiento de notificaciones. No enviar alertas repetitivas por una misma falla que continúa activa.

## 10. MÓDULO DE INCIDENTES

Crear una sección para administrar incidentes con los siguientes estados:

- Nuevo.
- En proceso.
- Escalado.
- Resuelto.
- Cerrado.

Registrar:

- Código del incidente.
- Agencia.
- Dispositivo afectado.
- Tipo de falla.
- Fecha y hora de inicio.
- Fecha y hora de recuperación.
- Duración total.
- Responsable asignado.
- Diagnóstico técnico.
- Solución aplicada.
- Observaciones.
- Evidencia documental, cuando corresponda.

Permitir consultar el historial de incidentes por agencia, fecha, equipo y criticidad.

## 11. MÓDULO DE REPORTES

Generar reportes automáticos:

- Disponibilidad diaria, semanal y mensual.
- Porcentaje de disponibilidad por agencia.
- Historial de caídas de internet.
- Historial de fallas de switches.
- Historial de fallas de firewalls.
- Tiempo promedio de recuperación (MTTR).
- Tiempo promedio entre fallas (MTBF).
- Número de incidentes por agencia.
- Incidentes por proveedor de internet.
- Identificación de agencias con mayor recurrencia de fallas.

Permitir exportar los reportes a Excel y PDF.

Incluir gráficos y conclusiones técnicas.

## 12. CÁLCULO DE DISPONIBILIDAD

Calcular la disponibilidad de cada enlace y dispositivo utilizando:

Disponibilidad (%) = ((Tiempo total del período - Tiempo de indisponibilidad) / Tiempo total del período) × 100.

Mostrar los resultados por agencia y consolidado institucional.

No contabilizar los períodos sin información como períodos operativos. Diferenciar indisponibilidad real de falta de telemetría.

## 13. SEGURIDAD Y CONTROL DE ACCESOS

Implementar autenticación segura y perfiles:

**Administrador de Tecnología**
- Acceso completo.
- Configuración de agencias.
- Registro de dispositivos.
- Administración de usuarios.
- Configuración de alertas.
- Consulta de reportes.

**Técnico de Infraestructura**
- Consulta de equipos.
- Diagnóstico de fallas.
- Gestión de incidentes.
- Registro de soluciones.

**Gerencia / Administrador de consulta**
- Consulta de dashboard.
- Consulta de indicadores.
- Visualización de reportes.
- Sin permisos de modificación.

Aplicar:
- Control de acceso por roles.
- Firebase Authentication.
- Reglas de seguridad de Firestore.
- Registro de auditoría de actividades.
- Protección de credenciales.
- Comunicación cifrada HTTPS.
- SNMPv3 con autenticación y privacidad.
- Gestión de secretos exclusivamente en el servidor.

Nunca incluir contraseñas SNMP, claves de API, usuarios administrativos ni credenciales de firewalls dentro del código de la aplicación móvil.

## 14. ARQUITECTURA DE MONITOREO REAL

Implementar una arquitectura que permita obtener datos reales desde las agencias.

El sistema debe contar con un agente o colector de monitoreo ubicado dentro de la infraestructura tecnológica institucional o en un entorno con conectividad privada hacia las redes de las agencias.

Este colector debe:

1. Ejecutar comprobaciones ICMP.
2. Consultar los equipos mediante SNMPv3.
3. Recibir eventos Syslog cuando corresponda.
4. Consultar las API de los fabricantes.
5. Procesar las mediciones.
6. Enviar resultados al backend central mediante HTTPS.
7. Detectar caídas y recuperaciones.
8. Generar eventos para las notificaciones push.

Diseñar una API REST para integrar el colector con el backend.

Endpoints sugeridos:

POST /api/monitoring/heartbeat

POST /api/monitoring/metrics

POST /api/monitoring/events

GET /api/agencies

GET /api/devices

GET /api/incidents

GET /api/dashboard

GET /api/reports/availability

El backend debe validar autenticación, integridad y autorización antes de aceptar información.

La aplicación móvil debe consultar el backend y recibir actualizaciones sin realizar consultas SNMP directamente desde el teléfono.

## 15. MODO DE DEMOSTRACIÓN

Crear un modo DEMO para visualizar el funcionamiento de la aplicación sin conexión a los equipos reales.

En este modo:

- Simular las siete agencias.
- Mostrar estados operativos y fallas.
- Simular caída y recuperación de enlaces.
- Generar alertas de prueba.
- Mostrar gráficos de disponibilidad.
- Permitir probar la gestión de incidentes.

Identificar siempre los datos simulados con la etiqueta DEMO.

No mezclar información de demostración con los registros reales.

## 16. DISEÑO DE INTERFAZ

Desarrollar las siguientes pantallas:

1. Inicio de sesión institucional.
2. Dashboard general.
3. Listado de las siete agencias.
4. Detalle de cada agencia.
5. Estado de enlaces de internet.
6. Monitoreo de switches.
7. Monitoreo de firewalls.
8. Centro de notificaciones.
9. Gestión de incidentes.
10. Historial de eventos.
11. Reportes estadísticos.
12. Administración de dispositivos.
13. Configuración de umbrales.
14. Administración de usuarios.

La navegación debe ser sencilla, mediante una barra inferior con accesos a:

- Inicio.
- Agencias.
- Alertas.
- Incidentes.
- Configuración.

Utilizar tarjetas de información, gráficos, indicadores circulares, semáforos y etiquetas de estado.

El diseño debe ser profesional, con predominio de blanco y naranja institucional.

## 17. REQUISITOS DE DESARROLLO

No generar únicamente una maqueta visual.

Construir una aplicación funcional, con estructura de archivos organizada, componentes reutilizables y separación entre frontend, backend y servicios de monitoreo.

Generar:

- Código Kotlin completo.
- Pantallas Jetpack Compose.
- Modelos de datos.
- Repositorios.
- Servicios de comunicación API.
- Configuración de Firebase.
- Backend Node.js.
- Endpoints REST.
- Servicio de notificaciones.
- Modelo de almacenamiento.
- Reglas de seguridad.
- Documentación de instalación.
- Instrucciones de configuración.
- Pruebas unitarias y de integración.

Cuando falten datos de infraestructura, crear formularios de configuración y valores de ejemplo claramente identificados como ficticios.

No afirmar que existe monitoreo real hasta verificar la comunicación con los equipos y recibir telemetría válida.

## 18. RESULTADO FINAL ESPERADO

Entregar una aplicación Android nativa, moderna, segura y escalable que permita al Departamento de Tecnología de la Cooperativa Cotocollao supervisar centralizadamente la infraestructura de red de sus siete agencias.

La aplicación debe permitir identificar inmediatamente dónde existe una falla, qué equipo está afectado, cuánto tiempo lleva la interrupción y qué acciones técnicas deben ejecutarse.

Priorizar la disponibilidad de los servicios tecnológicos institucionales, la detección temprana de incidentes, la continuidad operativa y la reducción de los tiempos de respuesta.

Comienza construyendo la aplicación completa, no solamente describiendo cómo desarrollarla. Genera los archivos del proyecto, implementa las funcionalidades y verifica que el código compile y que las pantallas funcionen correctamente.
