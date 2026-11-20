package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.demo.DemoDataEngine
import com.example.data.model.Incident
import com.example.data.model.MonitoringThresholds
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.state.DemoSandboxController
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.state.computeDashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel completamente aislado en memoria para explorar el modo DEMO antes de iniciar sesión,
 * sin instanciar Firestore ni ejecutar consultas no autenticadas.
 */
class DemoViewModel : ViewModel(), NocActions {

    private val controller = DemoSandboxController()
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<NocWorkspaceState> = _uiState.asStateFlow()

    private fun buildState(
        banner: String? = null,
        error: String? = null,
        latestAlert: com.example.data.model.AlertEvent? = null
    ): NocWorkspaceState {
        return NocWorkspaceState(
            isLoading = false,
            errorMessage = error,
            statusBannerMessage = banner,
            isDemoMode = true,
            userProfile = UserProfile(
                userId = "demo-sandbox",
                displayName = "Operador NOC",
                email = "noc@cotocollao.fin.ec",
                role = controller.activeRole.name
            ),
            effectiveRole = controller.activeRole,
            agencies = controller.agencies,
            devices = controller.devices,
            incidents = controller.incidents,
            alerts = controller.alerts,
            thresholds = controller.thresholds,
            teamMembers = controller.teamMembers,
            auditLogs = controller.auditLogs,
            dashboard = computeDashboardSummary(
                controller.agencies,
                controller.devices,
                controller.incidents,
                telemetryConnected = true
            ),
            chartSeries = DemoDataEngine.buildChartSeries(),
            latestTriggeredAlert = latestAlert
        )
    }

    override fun clearMessages() {
        _uiState.value = buildState()
    }

    override fun toggleDemoMode(enabled: Boolean) {
        _uiState.value = buildState(
            banner = "Panel de monitoreo local activo."
        )
    }

    override fun switchDemoRole(role: UserRole) {
        controller.activeRole = role
        _uiState.value = buildState(banner = "Perfil cambiado a ${role.displayNameEs}")
    }

    override fun simulateDemoLinkDrop(agencyId: String) {
        val alert = controller.dropLink(agencyId)
        _uiState.value = buildState(
            banner = "Caída detectada en ${alert?.agencyName ?: agencyId}",
            latestAlert = alert
        )
    }

    override fun simulateDemoLinkRecovery(agencyId: String) {
        val alert = controller.recoverLink(agencyId)
        _uiState.value = buildState(
            banner = "Enlace recuperado en ${alert?.agencyName ?: agencyId}",
            latestAlert = alert
        )
    }

    override fun triggerDemoTestAlert() {
        val alert = controller.dropLink("calderon")
        _uiState.value = buildState(
            banner = "Alerta de prueba generada",
            latestAlert = alert
        )
    }

    override fun saveAgencyConfig(
        agencyId: String,
        cityZone: String,
        publicIp: String,
        gatewayIp: String,
        switchIp: String,
        firewallIp: String,
        ispProvider: String,
        bandwidthMbps: Long
    ) {
        controller.agencies = controller.agencies.map {
            if (it.id == agencyId) {
                it.copy(
                    cityZone = cityZone,
                    publicIp = publicIp,
                    gatewayIp = gatewayIp,
                    switchIp = switchIp,
                    firewallIp = firewallIp,
                    ispProvider = ispProvider,
                    contractedBandwidthMbps = bandwidthMbps,
                    configured = true
                )
            } else it
        }
        _uiState.value = buildState(banner = "Agencia actualizada")
    }

    override fun saveDevice(device: NetworkDevice) {
        val id = device.id.ifBlank { "demo_dev_${System.currentTimeMillis()}" }
        controller.devices = listOf(device.copy(id = id)) + controller.devices.filterNot { it.id == id }
        _uiState.value = buildState(banner = "Dispositivo guardado")
    }

    override fun createIncident(incident: Incident) {
        val id = "demo_inc_${System.currentTimeMillis()}"
        controller.incidents = listOf(incident.copy(id = id)) + controller.incidents
        _uiState.value = buildState(banner = "Incidente creado")
    }

    override fun updateIncident(incident: Incident) {
        controller.incidents = controller.incidents.map { if (it.id == incident.id) incident else it }
        _uiState.value = buildState(banner = "Incidente actualizado")
    }

    override fun acknowledgeAlert(alertId: String) {
        controller.alerts = controller.alerts.map {
            if (it.id == alertId) it.copy(acknowledged = true, currentStatus = "RECONOCIDA") else it
        }
        _uiState.value = buildState()
    }

    override fun saveThresholds(thresholds: MonitoringThresholds) {
        controller.thresholds = thresholds
        _uiState.value = buildState(banner = "Umbrales actualizados")
    }

    override fun saveTeamMember(member: TeamMember) {
        val id = member.id.ifBlank { "demo_usr_${System.currentTimeMillis()}" }
        controller.teamMembers = listOf(member.copy(id = id)) + controller.teamMembers.filterNot { it.id == id }
        _uiState.value = buildState(banner = "Usuario actualizado")
    }

    override fun syncFromBackend() {
        _uiState.value = buildState(
            banner = "Telemetría local actualizada."
        )
    }
}
