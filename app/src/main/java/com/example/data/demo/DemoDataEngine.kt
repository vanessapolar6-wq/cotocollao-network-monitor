package com.example.data.demo

import com.example.data.model.Agency
import com.example.data.model.AlertEvent
import com.example.data.model.AlertSeverity
import com.example.data.model.AuditLog
import com.example.data.model.DeviceType
import com.example.data.model.Incident
import com.example.data.model.IncidentSeverity
import com.example.data.model.IncidentStatus
import com.example.data.model.MonitoringProtocol
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.TrafficLightStatus
import com.example.data.model.UserRole

data class DemoChartSeries(
    val incidentTrend30Days: List<Pair<String, Int>>,
    val monthlyAvailability: List<Pair<String, Double>>,
    val faultDistribution: List<Pair<String, Int>>
)

object DemoDataEngine {

    fun buildInitialAgencies(): List<Agency> {
        val now = System.currentTimeMillis()
        return listOf(
            Agency(
                id = "matriz", code = "AG-01", name = "MATRIZ", cityZone = "Quito Norte - Cotocollao",
                publicIp = "190.152.10.10", gatewayIp = "10.10.1.1",
                switchIp = "10.10.1.2", firewallIp = "10.10.1.254",
                ispProvider = "Telconet Fibra", contractedBandwidthMbps = 500,
                status = TrafficLightStatus.GREEN.name, linkStatus = TrafficLightStatus.GREEN.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 11, packetLossPct = 0.0, availabilityPct = 99.94,
                downtimeSeconds = 120, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            ),
            Agency(
                id = "calderon", code = "AG-02", name = "CALDERÓN", cityZone = "Quito Norte - Calderón",
                publicIp = "190.152.10.20", gatewayIp = "10.10.2.1",
                switchIp = "10.10.2.2", firewallIp = "10.10.2.254",
                ispProvider = "CNT Corp", contractedBandwidthMbps = 200,
                status = TrafficLightStatus.RED.name, linkStatus = TrafficLightStatus.RED.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.YELLOW.name,
                latencyMs = 0, packetLossPct = 100.0, availabilityPct = 96.40,
                downtimeSeconds = 2480, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now - 2480_000, configured = true
            ),
            Agency(
                id = "sangolqui", code = "AG-03", name = "SANGOLQUÍ", cityZone = "Valle de los Chillos",
                publicIp = "190.152.10.30", gatewayIp = "10.10.3.1",
                switchIp = "10.10.3.2", firewallIp = "10.10.3.254",
                ispProvider = "Telconet Fibra", contractedBandwidthMbps = 300,
                status = TrafficLightStatus.YELLOW.name, linkStatus = TrafficLightStatus.YELLOW.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 148, packetLossPct = 3.2, availabilityPct = 99.12,
                downtimeSeconds = 420, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            ),
            Agency(
                id = "sur", code = "AG-04", name = "SUR", cityZone = "Quito Sur - El Recreo",
                publicIp = "190.152.10.40", gatewayIp = "10.10.4.1",
                switchIp = "10.10.4.2", firewallIp = "10.10.4.254",
                ispProvider = "Puntonet MPLS", contractedBandwidthMbps = 300,
                status = TrafficLightStatus.GREEN.name, linkStatus = TrafficLightStatus.GREEN.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 16, packetLossPct = 0.0, availabilityPct = 99.85,
                downtimeSeconds = 180, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            ),
            Agency(
                id = "pomasqui", code = "AG-05", name = "POMASQUI", cityZone = "Quito Norte - Pomasqui",
                publicIp = "190.152.10.50", gatewayIp = "10.10.5.1",
                switchIp = "10.10.5.2", firewallIp = "10.10.5.254",
                ispProvider = "Telconet Fibra", contractedBandwidthMbps = 150,
                status = TrafficLightStatus.YELLOW.name, linkStatus = TrafficLightStatus.GREEN.name,
                switchStatus = TrafficLightStatus.YELLOW.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 19, packetLossPct = 0.0, availabilityPct = 99.51,
                downtimeSeconds = 300, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            ),
            Agency(
                id = "san_antonio", code = "AG-06", name = "SAN ANTONIO", cityZone = "Mitad del Mundo",
                publicIp = "190.152.10.60", gatewayIp = "10.10.6.1",
                switchIp = "10.10.6.2", firewallIp = "10.10.6.254",
                ispProvider = "CNT Corp", contractedBandwidthMbps = 150,
                status = TrafficLightStatus.GREEN.name, linkStatus = TrafficLightStatus.GREEN.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 24, packetLossPct = 0.0, availabilityPct = 99.78,
                downtimeSeconds = 210, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            ),
            Agency(
                id = "centro", code = "AG-07", name = "CENTRO", cityZone = "Quito Centro Histórico",
                publicIp = "190.152.10.70", gatewayIp = "10.10.7.1",
                switchIp = "10.10.7.2", firewallIp = "10.10.7.254",
                ispProvider = "Puntonet MPLS", contractedBandwidthMbps = 250,
                status = TrafficLightStatus.GREEN.name, linkStatus = TrafficLightStatus.GREEN.name,
                switchStatus = TrafficLightStatus.GREEN.name, firewallStatus = TrafficLightStatus.GREEN.name,
                latencyMs = 14, packetLossPct = 0.0, availabilityPct = 99.89,
                downtimeSeconds = 90, noTelemetrySeconds = 0,
                lastCheckEpochMs = now, lastConnectionEpochMs = now, configured = true
            )
        )
    }

    fun buildInitialDevices(): List<NetworkDevice> {
        val now = System.currentTimeMillis()
        return listOf(
            NetworkDevice(
                id = "demo_sw_matriz", agencyId = "matriz", agencyName = "MATRIZ",
                deviceType = DeviceType.SWITCH_L3.name, hostname = "SW-MATRIZ-CORE",
                ipAddress = "10.10.1.2", brand = "Cisco", model = "Catalyst 9300-48P",
                serialNumber = "FCW2310Q8L", firmwareVersion = "IOS-XE 17.9.4",
                protocol = MonitoringProtocol.SNMPV3.name, secretRefId = "vault://snmpv3/matriz-core",
                status = TrafficLightStatus.GREEN.name, cpuPct = 28, ramPct = 44, tempCelsius = 41,
                uptimeSeconds = 4_230_000, activeInterfaces = 42, totalInterfaces = 48,
                txErrors = 0, droppedPackets = 2, lastCheckEpochMs = now
            ),
            NetworkDevice(
                id = "demo_fw_matriz", agencyId = "matriz", agencyName = "MATRIZ",
                deviceType = DeviceType.FIREWALL.name, hostname = "FW-MATRIZ-HA",
                ipAddress = "10.10.1.254", brand = "Fortinet", model = "FortiGate 200F",
                serialNumber = "FG200F991A", firmwareVersion = "FortiOS 7.4.3",
                protocol = MonitoringProtocol.HTTPS_API.name, secretRefId = "vault://api/fw-matriz",
                status = TrafficLightStatus.GREEN.name, cpuPct = 35, ramPct = 52, tempCelsius = 46,
                uptimeSeconds = 5_100_000, activeInterfaces = 10, totalInterfaces = 12,
                vpnTunnelsUp = 6, vpnTunnelsTotal = 7, lastCheckEpochMs = now
            ),
            NetworkDevice(
                id = "demo_sw_pomasqui", agencyId = "pomasqui", agencyName = "POMASQUI",
                deviceType = DeviceType.SWITCH_L3.name, hostname = "SW-POMASQUI-01",
                ipAddress = "10.10.5.2", brand = "HPE Aruba", model = "CX 6300M",
                serialNumber = "CN9921AR4", firmwareVersion = "AOS-CX 10.13",
                protocol = MonitoringProtocol.SNMPV3.name, secretRefId = "vault://snmpv3/pomasqui",
                status = TrafficLightStatus.YELLOW.name, cpuPct = 89, ramPct = 76, tempCelsius = 64,
                uptimeSeconds = 812_000, activeInterfaces = 20, totalInterfaces = 24,
                txErrors = 34, droppedPackets = 128, lastCheckEpochMs = now
            ),
            NetworkDevice(
                id = "demo_fw_calderon", agencyId = "calderon", agencyName = "CALDERÓN",
                deviceType = DeviceType.FIREWALL.name, hostname = "FW-CALDERON-01",
                ipAddress = "10.10.2.254", brand = "Fortinet", model = "FortiGate 80F",
                serialNumber = "FG80F127B", firmwareVersion = "FortiOS 7.4.3",
                protocol = MonitoringProtocol.SYSLOG.name, secretRefId = "vault://syslog/calderon",
                status = TrafficLightStatus.YELLOW.name, cpuPct = 42, ramPct = 58, tempCelsius = 49,
                uptimeSeconds = 1_240_000, activeInterfaces = 6, totalInterfaces = 8,
                vpnTunnelsUp = 0, vpnTunnelsTotal = 1, lastCheckEpochMs = now
            ),
            NetworkDevice(
                id = "demo_sw_sangolqui", agencyId = "sangolqui", agencyName = "SANGOLQUÍ",
                deviceType = DeviceType.SWITCH_L3.name, hostname = "SW-SANGOLQUI-01",
                ipAddress = "10.10.3.2", brand = "Cisco", model = "Catalyst 9200L",
                serialNumber = "JAE4412C2", firmwareVersion = "IOS-XE 17.9.4",
                protocol = MonitoringProtocol.SNMPV3.name, secretRefId = "vault://snmpv3/sangolqui",
                status = TrafficLightStatus.GREEN.name, cpuPct = 21, ramPct = 38, tempCelsius = 39,
                uptimeSeconds = 2_890_000, activeInterfaces = 22, totalInterfaces = 24,
                txErrors = 0, droppedPackets = 0, lastCheckEpochMs = now
            ),
            NetworkDevice(
                id = "demo_fw_sur", agencyId = "sur", agencyName = "SUR",
                deviceType = DeviceType.FIREWALL.name, hostname = "FW-SUR-01",
                ipAddress = "10.10.4.254", brand = "Sophos", model = "XGS 136",
                serialNumber = "XGS13604S", firmwareVersion = "SFOS 20.0",
                protocol = MonitoringProtocol.SNMPV3.name, secretRefId = "vault://snmpv3/fw-sur",
                status = TrafficLightStatus.GREEN.name, cpuPct = 31, ramPct = 47, tempCelsius = 43,
                uptimeSeconds = 3_410_000, activeInterfaces = 8, totalInterfaces = 8,
                vpnTunnelsUp = 1, vpnTunnelsTotal = 1, lastCheckEpochMs = now
            )
        )
    }

    fun buildInitialIncidents(): List<Incident> {
        val now = System.currentTimeMillis()
        return listOf(
            Incident(
                id = "demo_inc_1", code = "INC-001", agencyId = "calderon", agencyName = "CALDERÓN",
                deviceId = "demo_rtr_calderon", deviceName = "Router Principal",
                deviceType = "INTERNET_LINK", failureType = "ENLACE DE INTERNET CAÍDO",
                severity = IncidentSeverity.CRITICAL.name, status = IncidentStatus.IN_PROGRESS.name,
                startedAtEpochMs = now - 2_480_000, recoveredAtEpochMs = 0L, durationSeconds = 2480,
                assignedTo = "Ing. Carlos Méndez",
                diagnosis = "3 comprobaciones ICMP consecutivas fallidas hacia IP WAN; pérdida 100%.",
                solution = "", observations = "Ticket escalado con proveedor CNT Corp."
            ),
            Incident(
                id = "demo_inc_2", code = "INC-002", agencyId = "pomasqui", agencyName = "POMASQUI",
                deviceId = "demo_sw_pomasqui", deviceName = "SW-POMASQUI-01",
                deviceType = "SWITCH_L3", failureType = "CPU SUPERIOR AL UMBRAL (89%)",
                severity = IncidentSeverity.HIGH.name, status = IncidentStatus.ESCALATED.name,
                startedAtEpochMs = now - 1_100_000, recoveredAtEpochMs = 0L, durationSeconds = 1100,
                assignedTo = "Ing. Valeria Torres",
                diagnosis = "Incremento de tráfico broadcast en VLAN 20 (Cajas).",
                solution = "", observations = "Revisando control de tormentas en puerto troncal Gi1/0/24."
            ),
            Incident(
                id = "demo_inc_3", code = "INC-003", agencyId = "centro", agencyName = "CENTRO",
                deviceId = "demo_fw_centro", deviceName = "FW-CENTRO-01",
                deviceType = "VPN", failureType = "INTERRUPCIÓN DE TÚNEL VPN SITE-TO-SITE",
                severity = IncidentSeverity.MEDIUM.name, status = IncidentStatus.RESOLVED.name,
                startedAtEpochMs = now - 86_400_000, recoveredAtEpochMs = now - 85_920_000,
                durationSeconds = 480, assignedTo = "Ing. Carlos Méndez",
                diagnosis = "Renegociación IKEv2 interrumpida por microcorte WAN.",
                solution = "Reinicio de fase IKEv2 y verificación de tráfico cifrado hacia Matriz.",
                observations = "Túnel operativo sin pérdidas posteriores."
            )
        )
    }

    fun buildInitialAlerts(): List<AlertEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            AlertEvent(
                id = "demo_al_1", agencyId = "calderon", agencyName = "CALDERÓN",
                deviceName = "Router principal", deviceIp = "190.152.10.20",
                incidentType = "ENLACE DE INTERNET CAÍDO", severity = AlertSeverity.CRITICAL.name,
                description = "Sin respuesta ICMP tras 3 comprobaciones consecutivas (intervalo 30s).",
                currentStatus = "SIN CONECTIVIDAD",
                recommendation = "Verificar proveedor de internet, gateway y conectividad WAN.",
                durationSeconds = 2480, occurrences = 3, acknowledged = false, eventEpochMs = now - 2_480_000
            ),
            AlertEvent(
                id = "demo_al_2", agencyId = "pomasqui", agencyName = "POMASQUI",
                deviceName = "SW-POMASQUI-01", deviceIp = "10.10.5.2",
                incidentType = "CPU SUPERIOR AL UMBRAL", severity = AlertSeverity.WARNING.name,
                description = "Uso de CPU en 89% (umbral configurado: 85%).",
                currentStatus = "DEGRADADO",
                recommendation = "Inspeccionar interfaces troncales y errores TX vía SNMPv3.",
                durationSeconds = 1100, occurrences = 2, acknowledged = false, eventEpochMs = now - 1_100_000
            ),
            AlertEvent(
                id = "demo_al_3", agencyId = "sangolqui", agencyName = "SANGOLQUÍ",
                deviceName = "Enlace WAN Telconet", deviceIp = "190.152.10.30",
                incidentType = "LATENCIA EXCESIVA", severity = AlertSeverity.WARNING.name,
                description = "Latencia promedio 148 ms (umbral: 120 ms) y pérdida 3.2%.",
                currentStatus = "DEGRADADO",
                recommendation = "Validar saturación de ancho de banda y calidad de enlace con ISP.",
                durationSeconds = 420, occurrences = 1, acknowledged = true, eventEpochMs = now - 420_000
            )
        )
    }

    fun buildInitialTeam(): List<TeamMember> = listOf(
        TeamMember(
            id = "demo_usr_1", fullName = "Ing. Valeria Torres",
            email = "vtorres@cotocollao.fin.ec", role = UserRole.ADMIN.name,
            active = true, department = "Jefatura de Tecnología"
        ),
        TeamMember(
            id = "demo_usr_2", fullName = "Ing. Carlos Méndez",
            email = "cmendez@cotocollao.fin.ec", role = UserRole.TECHNICIAN.name,
            active = true, department = "Infraestructura y Redes"
        ),
        TeamMember(
            id = "demo_usr_3", fullName = "Econ. Sofía Paredes",
            email = "sparedes@cotocollao.fin.ec", role = UserRole.MANAGER.name,
            active = true, department = "Gerencia General"
        )
    )

    fun buildInitialAuditLogs(): List<AuditLog> {
        val now = System.currentTimeMillis()
        return listOf(
            AuditLog(
                id = "demo_log_1", actorEmail = "vtorres@cotocollao.fin.ec",
                actorRole = UserRole.ADMIN.name, action = "INICIO_SISTEMA",
                target = "7 Agencias", details = "Sesión de monitoreo centralizado NOC inicializada.",
                timestampEpochMs = now - 60_000
            )
        )
    }

    fun buildChartSeries(): DemoChartSeries = DemoChartSeries(
        incidentTrend30Days = listOf(
            "S1" to 4, "S2" to 2, "S3" to 5, "S4" to 3, "Hoy" to 2
        ),
        monthlyAvailability = listOf(
            "Jun" to 99.7, "Jul" to 99.8, "Ago" to 99.4, "Sep" to 99.9, "Oct" to 99.5, "Nov" to 99.2
        ),
        faultDistribution = listOf(
            "Enlaces WAN" to 6,
            "Switches L3" to 3,
            "Firewalls" to 2,
            "Túneles VPN" to 3
        )
    )

    fun buildEmptyRealAgencies(uid: String): List<Agency> = listOf(
        Agency(id = "matriz", userId = uid, code = "AG-01", name = "MATRIZ", cityZone = "Quito Norte - Cotocollao"),
        Agency(id = "calderon", userId = uid, code = "AG-02", name = "CALDERÓN", cityZone = "Quito Norte - Calderón"),
        Agency(id = "sangolqui", userId = uid, code = "AG-03", name = "SANGOLQUÍ", cityZone = "Valle de los Chillos"),
        Agency(id = "sur", userId = uid, code = "AG-04", name = "SUR", cityZone = "Quito Sur - El Recreo"),
        Agency(id = "pomasqui", userId = uid, code = "AG-05", name = "POMASQUI", cityZone = "Quito Norte - Pomasqui"),
        Agency(id = "san_antonio", userId = uid, code = "AG-06", name = "SAN ANTONIO", cityZone = "Mitad del Mundo"),
        Agency(id = "centro", userId = uid, code = "AG-07", name = "CENTRO", cityZone = "Quito Centro Histórico")
    )
}
