package com.example.ui.state

import com.example.data.demo.DemoChartSeries
import com.example.data.demo.DemoDataEngine
import com.example.data.model.Agency
import com.example.data.model.AlertEvent
import com.example.data.model.AlertSeverity
import com.example.data.model.AuditLog
import com.example.data.model.DashboardSummary
import com.example.data.model.Incident
import com.example.data.model.IncidentSeverity
import com.example.data.model.IncidentStatus
import com.example.data.model.MonitoringThresholds
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.TrafficLightStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

data class NocWorkspaceState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusBannerMessage: String? = null,
    val isDemoMode: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val effectiveRole: UserRole = UserRole.MANAGER,
    val agencies: List<Agency> = emptyList(),
    val devices: List<NetworkDevice> = emptyList(),
    val incidents: List<Incident> = emptyList(),
    val alerts: List<AlertEvent> = emptyList(),
    val thresholds: MonitoringThresholds = MonitoringThresholds(),
    val teamMembers: List<TeamMember> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val dashboard: DashboardSummary = DashboardSummary(),
    val chartSeries: DemoChartSeries = DemoChartSeries(emptyList(), emptyList(), emptyList()),
    val latestTriggeredAlert: AlertEvent? = null
)

interface NocActions {
    fun clearMessages()
    fun toggleDemoMode(enabled: Boolean)
    fun switchDemoRole(role: UserRole)
    fun simulateDemoLinkDrop(agencyId: String)
    fun simulateDemoLinkRecovery(agencyId: String)
    fun triggerDemoTestAlert()
    fun saveAgencyConfig(
        agencyId: String,
        cityZone: String,
        publicIp: String,
        gatewayIp: String,
        switchIp: String,
        firewallIp: String,
        ispProvider: String,
        bandwidthMbps: Long
    )
    fun saveDevice(device: NetworkDevice)
    fun createIncident(incident: Incident)
    fun updateIncident(incident: Incident)
    fun acknowledgeAlert(alertId: String)
    fun saveThresholds(thresholds: MonitoringThresholds)
    fun saveTeamMember(member: TeamMember)
    fun syncFromBackend()
}

fun computeDashboardSummary(
    agencies: List<Agency>,
    devices: List<NetworkDevice>,
    incidents: List<Incident>,
    telemetryConnected: Boolean
): DashboardSummary {
    val switches = devices.filter { it.deviceType == "SWITCH_L3" }
    val firewalls = devices.filter { it.deviceType == "FIREWALL" }
    val configuredAgencies = agencies.filter { it.configured }
    val avgAvail = if (configuredAgencies.isNotEmpty()) {
        val raw = configuredAgencies.sumOf { it.availabilityPct } / configuredAgencies.size
        kotlin.math.round(raw * 100.0) / 100.0
    } else 0.0

    return DashboardSummary(
        totalAgencies = agencies.size.coerceAtLeast(7),
        operationalAgencies = agencies.count { it.overallLight == TrafficLightStatus.GREEN },
        agenciesWithIncidents = agencies.count {
            it.overallLight == TrafficLightStatus.RED || it.overallLight == TrafficLightStatus.YELLOW
        },
        activeInternetLinks = agencies.count {
            it.linkLight == TrafficLightStatus.GREEN || it.linkLight == TrafficLightStatus.YELLOW
        },
        downInternetLinks = agencies.count { it.linkLight == TrafficLightStatus.RED },
        operationalSwitches = switches.count { it.statusLight == TrafficLightStatus.GREEN },
        failedSwitches = switches.count {
            it.statusLight == TrafficLightStatus.RED || it.statusLight == TrafficLightStatus.YELLOW
        },
        operationalFirewalls = firewalls.count { it.statusLight == TrafficLightStatus.GREEN },
        failedFirewalls = firewalls.count {
            it.statusLight == TrafficLightStatus.RED || it.statusLight == TrafficLightStatus.YELLOW
        },
        pendingCriticalIncidents = incidents.count {
            it.severityEnum == IncidentSeverity.CRITICAL &&
                it.statusEnum != IncidentStatus.RESOLVED &&
                it.statusEnum != IncidentStatus.CLOSED
        },
        overallAvailabilityPct = avgAvail,
        telemetryConnected = telemetryConnected
    )
}

class DemoSandboxController {
    var agencies: List<Agency> = DemoDataEngine.buildInitialAgencies()
    var devices: List<NetworkDevice> = DemoDataEngine.buildInitialDevices()
    var incidents: List<Incident> = DemoDataEngine.buildInitialIncidents()
    var alerts: List<AlertEvent> = DemoDataEngine.buildInitialAlerts()
    var thresholds: MonitoringThresholds = MonitoringThresholds(
        backendBaseUrl = "https://cotocollao-noc-backend.run.app"
    )
    var teamMembers: List<TeamMember> = DemoDataEngine.buildInitialTeam()
    var auditLogs: List<AuditLog> = DemoDataEngine.buildInitialAuditLogs()
    var activeRole: UserRole = UserRole.ADMIN

    fun dropLink(agencyId: String): AlertEvent? {
        val now = System.currentTimeMillis()
        val target = agencies.firstOrNull { it.id == agencyId } ?: return null
        agencies = agencies.map {
            if (it.id == agencyId) {
                it.copy(
                    status = TrafficLightStatus.RED.name,
                    linkStatus = TrafficLightStatus.RED.name,
                    latencyMs = 0,
                    packetLossPct = 100.0,
                    downtimeSeconds = it.downtimeSeconds + 90,
                    availabilityPct = (it.availabilityPct - 0.35).coerceAtLeast(85.0),
                    lastCheckEpochMs = now
                )
            } else it
        }
        val alert = AlertEvent(
            id = "demo_al_${now}",
            agencyId = target.id,
            agencyName = target.name,
            deviceName = "Router principal",
            deviceIp = target.publicIp,
            incidentType = "ENLACE DE INTERNET CAÍDO",
            severity = AlertSeverity.CRITICAL.name,
            description = "3 comprobaciones ICMP consecutivas fallidas (timeout 3s).",
            currentStatus = "SIN CONECTIVIDAD",
            recommendation = "Verificar proveedor de internet (${target.ispProvider}), gateway y conectividad WAN.",
            occurrences = 1,
            acknowledged = false,
            eventEpochMs = now
        )
        alerts = listOf(alert) + alerts
        incidents = listOf(
            Incident(
                id = "demo_inc_${now}",
                code = "INC-00${incidents.size + 1}",
                agencyId = target.id,
                agencyName = target.name,
                deviceId = "demo_rtr_${target.id}",
                deviceName = "Router principal",
                deviceType = "INTERNET_LINK",
                failureType = "ENLACE DE INTERNET CAÍDO",
                severity = IncidentSeverity.CRITICAL.name,
                status = IncidentStatus.NEW.name,
                startedAtEpochMs = now,
                assignedTo = "Guardia NOC",
                diagnosis = "Pérdida total de paquetes ICMP hacia puerta de enlace WAN."
            )
        ) + incidents
        auditLogs = listOf(
            AuditLog(
                id = "demo_log_${now}",
                actorEmail = "noc@cotocollao.fin.ec",
                actorRole = activeRole.name,
                action = "SIMULAR_CAIDA",
                target = target.name,
                details = "Caída de enlace WAN registrada en panel de pruebas",
                timestampEpochMs = now
            )
        ) + auditLogs
        return alert
    }

    fun recoverLink(agencyId: String): AlertEvent? {
        val now = System.currentTimeMillis()
        val target = agencies.firstOrNull { it.id == agencyId } ?: return null
        agencies = agencies.map {
            if (it.id == agencyId) {
                it.copy(
                    status = TrafficLightStatus.GREEN.name,
                    linkStatus = TrafficLightStatus.GREEN.name,
                    firewallStatus = TrafficLightStatus.GREEN.name,
                    latencyMs = 15,
                    packetLossPct = 0.0,
                    lastCheckEpochMs = now,
                    lastConnectionEpochMs = now
                )
            } else it
        }
        val recoveryAlert = AlertEvent(
            id = "demo_rec_${now}",
            agencyId = target.id,
            agencyName = target.name,
            deviceName = "Router principal",
            deviceIp = target.publicIp,
            incidentType = "RECUPERACIÓN DE SERVICIO",
            severity = AlertSeverity.RECOVERY.name,
            description = "2 comprobaciones ICMP consecutivas exitosas (15 ms, 0% pérdida).",
            currentStatus = "OPERATIVO",
            recommendation = "Servicio normalizado. Validar estabilidad de túneles VPN.",
            durationSeconds = target.downtimeSeconds.coerceAtLeast(90),
            occurrences = 1,
            acknowledged = false,
            eventEpochMs = now
        )
        alerts = listOf(recoveryAlert) + alerts
        incidents = incidents.map { inc ->
            if (inc.agencyId == agencyId && inc.statusEnum != IncidentStatus.CLOSED && inc.statusEnum != IncidentStatus.RESOLVED) {
                inc.copy(
                    status = IncidentStatus.RESOLVED.name,
                    recoveredAtEpochMs = now,
                    durationSeconds = ((now - inc.startedAtEpochMs) / 1000L).coerceAtLeast(90L),
                    solution = "Enlace WAN restablecido automáticamente tras 2 pruebas ICMP exitosas."
                )
            } else inc
        }
        return recoveryAlert
    }
}
