package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TrafficLightStatus(val labelEs: String) {
    GREEN("Operativo"),
    YELLOW("Degradado"),
    RED("Caído / Crítico"),
    GRAY("Sin telemetría");

    companion object {
        fun fromRaw(raw: String?): TrafficLightStatus =
            entries.firstOrNull { it.name == raw } ?: GRAY
    }
}

enum class UserRole(val displayNameEs: String, val canEditInfra: Boolean, val canManageIncidents: Boolean, val canViewIp: Boolean) {
    ADMIN("Administrador de Tecnología", true, true, true),
    TECHNICIAN("Técnico de Infraestructura", false, true, true),
    MANAGER("Gerencia / Consulta", false, false, false);

    companion object {
        fun fromRaw(raw: String?): UserRole =
            entries.firstOrNull { it.name == raw } ?: MANAGER
    }
}

enum class DeviceType(val labelEs: String) {
    ROUTER("Router / Gateway"),
    SWITCH_L3("Switch Capa 3"),
    FIREWALL("Firewall");

    companion object {
        fun fromRaw(raw: String?): DeviceType =
            entries.firstOrNull { it.name == raw } ?: SWITCH_L3
    }
}

enum class MonitoringProtocol(val labelEs: String) {
    ICMP("ICMP (PING)"),
    SNMPV3("SNMPv3 (authPriv)"),
    HTTPS_API("API HTTPS"),
    SYSLOG("Syslog TLS/UDP");

    companion object {
        fun fromRaw(raw: String?): MonitoringProtocol =
            entries.firstOrNull { it.name == raw } ?: ICMP
    }
}

enum class IncidentStatus(val labelEs: String) {
    NEW("Nuevo"),
    IN_PROGRESS("En proceso"),
    ESCALATED("Escalado"),
    RESOLVED("Resuelto"),
    CLOSED("Cerrado");

    companion object {
        fun fromRaw(raw: String?): IncidentStatus =
            entries.firstOrNull { it.name == raw } ?: NEW
    }
}

enum class IncidentSeverity(val labelEs: String) {
    CRITICAL("Crítica"),
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja");

    companion object {
        fun fromRaw(raw: String?): IncidentSeverity =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class AlertSeverity(val labelEs: String) {
    CRITICAL("CRÍTICA"),
    WARNING("ADVERTENCIA"),
    INFO("INFORMATIVA"),
    RECOVERY("RECUPERACIÓN");

    companion object {
        fun fromRaw(raw: String?): AlertSeverity =
            entries.firstOrNull { it.name == raw } ?: INFO
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = UserRole.MANAGER.name,
    val active: Boolean = true,
    val department: String = "Departamento de Tecnología",
    val fcmToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val roleEnum: UserRole get() = UserRole.fromRaw(role)
}

@Keep
@IgnoreExtraProperties
data class Agency(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val name: String = "",
    val cityZone: String = "",
    val publicIp: String = "",
    val gatewayIp: String = "",
    val switchIp: String = "",
    val firewallIp: String = "",
    val ispProvider: String = "",
    val contractedBandwidthMbps: Long = 0L,
    val status: String = TrafficLightStatus.GRAY.name,
    val linkStatus: String = TrafficLightStatus.GRAY.name,
    val switchStatus: String = TrafficLightStatus.GRAY.name,
    val firewallStatus: String = TrafficLightStatus.GRAY.name,
    val latencyMs: Long = 0L,
    val packetLossPct: Double = 0.0,
    val availabilityPct: Double = 0.0,
    val downtimeSeconds: Long = 0L,
    val noTelemetrySeconds: Long = 0L,
    val lastCheckEpochMs: Long = 0L,
    val lastConnectionEpochMs: Long = 0L,
    val configured: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val overallLight: TrafficLightStatus get() = TrafficLightStatus.fromRaw(status)
    val linkLight: TrafficLightStatus get() = TrafficLightStatus.fromRaw(linkStatus)
    val switchLight: TrafficLightStatus get() = TrafficLightStatus.fromRaw(switchStatus)
    val firewallLight: TrafficLightStatus get() = TrafficLightStatus.fromRaw(firewallStatus)
}

@Keep
@IgnoreExtraProperties
data class NetworkDevice(
    @DocumentId val id: String = "",
    val userId: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val deviceType: String = DeviceType.SWITCH_L3.name,
    val hostname: String = "",
    val ipAddress: String = "",
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val firmwareVersion: String = "",
    val protocol: String = MonitoringProtocol.SNMPV3.name,
    val secretRefId: String = "",
    val status: String = TrafficLightStatus.GRAY.name,
    val cpuPct: Long = 0L,
    val ramPct: Long = 0L,
    val tempCelsius: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val activeInterfaces: Long = 0L,
    val totalInterfaces: Long = 0L,
    val txErrors: Long = 0L,
    val droppedPackets: Long = 0L,
    val vpnTunnelsUp: Long = 0L,
    val vpnTunnelsTotal: Long = 0L,
    val lastCheckEpochMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val typeEnum: DeviceType get() = DeviceType.fromRaw(deviceType)
    val protocolEnum: MonitoringProtocol get() = MonitoringProtocol.fromRaw(protocol)
    val statusLight: TrafficLightStatus get() = TrafficLightStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class Incident(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceType: String = "INTERNET_LINK",
    val failureType: String = "",
    val severity: String = IncidentSeverity.CRITICAL.name,
    val status: String = IncidentStatus.NEW.name,
    val startedAtEpochMs: Long = 0L,
    val recoveredAtEpochMs: Long = 0L,
    val durationSeconds: Long = 0L,
    val assignedTo: String = "",
    val diagnosis: String = "",
    val solution: String = "",
    val observations: String = "",
    val evidenceNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: IncidentStatus get() = IncidentStatus.fromRaw(status)
    val severityEnum: IncidentSeverity get() = IncidentSeverity.fromRaw(severity)
}

@Keep
@IgnoreExtraProperties
data class AlertEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val deviceName: String = "",
    val deviceIp: String = "",
    val incidentType: String = "",
    val severity: String = AlertSeverity.CRITICAL.name,
    val description: String = "",
    val currentStatus: String = "",
    val recommendation: String = "",
    val durationSeconds: Long = 0L,
    val occurrences: Long = 1L,
    val acknowledged: Boolean = false,
    val eventEpochMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val severityEnum: AlertSeverity get() = AlertSeverity.fromRaw(severity)
}

@Keep
@IgnoreExtraProperties
data class MonitoringThresholds(
    @DocumentId val id: String = "default",
    val userId: String = "",
    val pingIntervalSec: Long = 30L,
    val pingTimeoutSec: Long = 3L,
    val failuresForDown: Long = 3L,
    val successesForRecovery: Long = 2L,
    val cpuThresholdPct: Long = 85L,
    val ramThresholdPct: Long = 85L,
    val tempThresholdC: Long = 70L,
    val packetLossThresholdPct: Double = 5.0,
    val latencyThresholdMs: Long = 120L,
    val backendBaseUrl: String = "",
    val dedupWindowMinutes: Long = 10L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TeamMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = UserRole.TECHNICIAN.name,
    val active: Boolean = true,
    val department: String = "Infraestructura TI",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val roleEnum: UserRole get() = UserRole.fromRaw(role)
}

@Keep
@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorEmail: String = "",
    val actorRole: String = UserRole.MANAGER.name,
    val action: String = "",
    val target: String = "",
    val details: String = "",
    val timestampEpochMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class DashboardSummary(
    val totalAgencies: Int = 7,
    val operationalAgencies: Int = 0,
    val agenciesWithIncidents: Int = 0,
    val activeInternetLinks: Int = 0,
    val downInternetLinks: Int = 0,
    val operationalSwitches: Int = 0,
    val failedSwitches: Int = 0,
    val operationalFirewalls: Int = 0,
    val failedFirewalls: Int = 0,
    val pendingCriticalIncidents: Int = 0,
    val overallAvailabilityPct: Double = 0.0,
    val telemetryConnected: Boolean = false
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
