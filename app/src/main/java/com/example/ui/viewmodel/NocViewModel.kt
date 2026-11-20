package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.demo.DemoChartSeries
import com.example.data.demo.DemoDataEngine
import com.example.data.model.Agency
import com.example.data.model.AlertEvent
import com.example.data.model.AuditLog
import com.example.data.model.Incident
import com.example.data.model.MonitoringThresholds
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.NocRepository
import com.example.ui.state.DemoSandboxController
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.state.computeDashboardSummary
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NocViewModel(
    private val repository: NocRepository,
    private val currentUserId: String,
    private val userEmail: String
) : ViewModel(), NocActions {

    private companion object {
        const val TAG = "NocViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    private val demoEnabled = MutableStateFlow(false)
    private val demoVersion = MutableStateFlow(0)
    private val demoController = DemoSandboxController()

    private val localBanner = MutableStateFlow<String?>(null)
    private val localError = MutableStateFlow<String?>(null)
    private val latestAlert = MutableStateFlow<AlertEvent?>(null)

    init {
        viewModelScope.launch {
            repository.ensureWorkspaceInitialized()
                .onFailure { e ->
                    localError.value = e.localizedMessage ?: "Error al inicializar espacio de trabajo"
                }
        }
    }

    private fun <T> Flow<T>.safeFirestoreFlow(fallback: T): Flow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Error observando Firestore", root)
            localError.value = root.localizedMessage ?: "Error de sincronización con Firestore"
            emit(fallback)
        }

    private val profileFlow = repository.observeUserProfile(currentUserId)
        .safeFirestoreFlow(UserProfile(userId = currentUserId, email = userEmail))

    private val agenciesFlow = repository.observeAgencies(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val devicesFlow = repository.observeDevices(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val incidentsFlow = repository.observeIncidents(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val alertsFlow = repository.observeAlerts(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val thresholdsFlow = repository.observeThresholds(currentUserId)
        .safeFirestoreFlow(MonitoringThresholds(userId = currentUserId))

    private val teamFlow = repository.observeTeamMembers(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val auditFlow = repository.observeAuditLogs(currentUserId)
        .safeFirestoreFlow(emptyList())

    private val realDataFlow = combine(
        profileFlow,
        agenciesFlow,
        devicesFlow,
        incidentsFlow,
        alertsFlow
    ) { profile, agencies, devices, incidents, alerts ->
        RealStreamBundle(profile, agencies, devices, incidents, alerts)
    }

    private val configFlow = combine(
        thresholdsFlow,
        teamFlow,
        auditFlow,
        demoEnabled,
        demoVersion
    ) { thresholds, team, audit, isDemo, version ->
        ConfigBundle(thresholds, team, audit, isDemo, version)
    }

    private val feedbackFlow = combine(localBanner, localError, latestAlert) { banner, err, alert ->
        Triple(banner, err, alert)
    }

    val uiState: StateFlow<NocWorkspaceState> = combine(
        realDataFlow,
        configFlow,
        feedbackFlow
    ) { real, cfg, feedback ->
        val (banner, err, alert) = feedback
        if (cfg.isDemo) {
            NocWorkspaceState(
                isLoading = false,
                errorMessage = err,
                statusBannerMessage = banner,
                isDemoMode = true,
                userProfile = (real.profile ?: UserProfile(userId = currentUserId, email = userEmail)).copy(
                    role = demoController.activeRole.name
                ),
                effectiveRole = demoController.activeRole,
                agencies = demoController.agencies,
                devices = demoController.devices,
                incidents = demoController.incidents,
                alerts = demoController.alerts,
                thresholds = demoController.thresholds,
                teamMembers = demoController.teamMembers,
                auditLogs = demoController.auditLogs,
                dashboard = computeDashboardSummary(
                    demoController.agencies,
                    demoController.devices,
                    demoController.incidents,
                    telemetryConnected = true
                ),
                chartSeries = DemoDataEngine.buildChartSeries(),
                latestTriggeredAlert = alert
            )
        } else {
            val resolvedProfile = real.profile ?: UserProfile(
                userId = currentUserId,
                displayName = "Operador NOC",
                email = userEmail,
                role = UserRole.MANAGER.name
            )
            val resolvedAgencies = real.agencies.ifEmpty { DemoDataEngine.buildEmptyRealAgencies(currentUserId) }
            val anyTelemetry = resolvedAgencies.any { it.lastCheckEpochMs > 0L }
            val realChartSeries = DemoChartSeries(
                incidentTrend30Days = listOf("Hoy" to real.incidents.size),
                monthlyAvailability = resolvedAgencies.map { it.name.take(4) to it.availabilityPct },
                faultDistribution = listOf(
                    "Enlaces WAN" to real.incidents.count { it.deviceType == "INTERNET_LINK" || it.deviceType == "ROUTER" },
                    "Switches L3" to real.incidents.count { it.deviceType == "SWITCH_L3" },
                    "Firewalls" to real.incidents.count { it.deviceType == "FIREWALL" },
                    "VPN" to real.incidents.count { it.deviceType == "VPN" }
                )
            )
            NocWorkspaceState(
                isLoading = false,
                errorMessage = err,
                statusBannerMessage = banner,
                isDemoMode = false,
                userProfile = resolvedProfile,
                effectiveRole = resolvedProfile.roleEnum,
                agencies = resolvedAgencies,
                devices = real.devices,
                incidents = real.incidents,
                alerts = real.alerts,
                thresholds = cfg.thresholds,
                teamMembers = cfg.team,
                auditLogs = cfg.audit,
                dashboard = computeDashboardSummary(
                    resolvedAgencies,
                    real.devices,
                    real.incidents,
                    telemetryConnected = anyTelemetry
                ),
                chartSeries = realChartSeries,
                latestTriggeredAlert = alert
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = NocWorkspaceState(isLoading = true)
    )

    override fun clearMessages() {
        localBanner.value = null
        localError.value = null
        latestAlert.value = null
    }

    override fun toggleDemoMode(enabled: Boolean) {
        demoEnabled.value = enabled
        localBanner.value = if (enabled) {
            "Panel de monitoreo en línea activado"
        } else {
            "Modo conectado a Firestore y backend institucional"
        }
    }

    override fun switchDemoRole(role: UserRole) {
        if (!demoEnabled.value) return
        demoController.activeRole = role
        demoVersion.value++
        localBanner.value = "Perfil activo cambiado a: ${role.displayNameEs}"
    }

    override fun simulateDemoLinkDrop(agencyId: String) {
        if (!demoEnabled.value) return
        val alert = demoController.dropLink(agencyId)
        latestAlert.value = alert
        demoVersion.value++
        localBanner.value = "Prueba de alerta: Caída de enlace en ${alert?.agencyName ?: agencyId}"
    }

    override fun simulateDemoLinkRecovery(agencyId: String) {
        if (!demoEnabled.value) return
        val alert = demoController.recoverLink(agencyId)
        latestAlert.value = alert
        demoVersion.value++
        localBanner.value = "Prueba de alerta: Recuperación de enlace en ${alert?.agencyName ?: agencyId}"
    }

    override fun triggerDemoTestAlert() {
        if (!demoEnabled.value) return
        val alert = demoController.dropLink("calderon")
        latestAlert.value = alert
        demoVersion.value++
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
        if (demoEnabled.value) {
            demoController.agencies = demoController.agencies.map {
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
            demoVersion.value++
            localBanner.value = "Agencia actualizada correctamente"
            return
        }
        val state = uiState.value
        viewModelScope.launch {
            repository.saveAgencyConfig(
                agencyId, cityZone, publicIp, gatewayIp, switchIp, firewallIp,
                ispProvider, bandwidthMbps, userEmail, state.effectiveRole
            ).onSuccess {
                localBanner.value = "Configuración de agencia guardada en Firestore"
            }.onFailure { e ->
                localError.value = e.localizedMessage ?: "Error al guardar agencia"
            }
        }
    }

    override fun saveDevice(device: NetworkDevice) {
        if (demoEnabled.value) {
            val id = device.id.ifBlank { "demo_dev_${System.currentTimeMillis()}" }
            val updated = device.copy(id = id)
            demoController.devices = listOf(updated) + demoController.devices.filterNot { it.id == id }
            demoVersion.value++
            localBanner.value = "Equipo guardado correctamente"
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.saveDevice(device, userEmail, role)
                .onSuccess { localBanner.value = "Dispositivo guardado en Firestore" }
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al guardar dispositivo" }
        }
    }

    override fun createIncident(incident: Incident) {
        if (demoEnabled.value) {
            val id = "demo_inc_${System.currentTimeMillis()}"
            demoController.incidents = listOf(incident.copy(id = id)) + demoController.incidents
            demoVersion.value++
            localBanner.value = "Incidente creado correctamente"
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.createIncident(incident, userEmail, role)
                .onSuccess { localBanner.value = "Incidente registrado en Firestore" }
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al registrar incidente" }
        }
    }

    override fun updateIncident(incident: Incident) {
        if (demoEnabled.value) {
            demoController.incidents = demoController.incidents.map {
                if (it.id == incident.id) incident else it
            }
            demoVersion.value++
            localBanner.value = "Incidente actualizado correctamente"
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.updateIncident(incident, userEmail, role)
                .onSuccess { localBanner.value = "Incidente actualizado" }
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al actualizar incidente" }
        }
    }

    override fun acknowledgeAlert(alertId: String) {
        if (demoEnabled.value) {
            demoController.alerts = demoController.alerts.map {
                if (it.id == alertId) it.copy(acknowledged = true, currentStatus = "RECONOCIDA") else it
            }
            demoVersion.value++
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId, userEmail, role)
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al reconocer alerta" }
        }
    }

    override fun saveThresholds(thresholds: MonitoringThresholds) {
        if (demoEnabled.value) {
            demoController.thresholds = thresholds
            demoVersion.value++
            localBanner.value = "Umbrales actualizados correctamente"
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.saveThresholds(thresholds, userEmail, role)
                .onSuccess { localBanner.value = "Umbrales guardados correctamente" }
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al guardar umbrales" }
        }
    }

    override fun saveTeamMember(member: TeamMember) {
        if (demoEnabled.value) {
            val id = member.id.ifBlank { "demo_usr_${System.currentTimeMillis()}" }
            demoController.teamMembers = listOf(member.copy(id = id)) + demoController.teamMembers.filterNot { it.id == id }
            demoVersion.value++
            localBanner.value = "Usuario guardado correctamente"
            return
        }
        val role = uiState.value.effectiveRole
        viewModelScope.launch {
            repository.saveTeamMember(member, userEmail, role)
                .onSuccess { localBanner.value = "Usuario autorizado guardado" }
                .onFailure { e -> localError.value = e.localizedMessage ?: "Error al guardar usuario" }
        }
    }

    override fun syncFromBackend() {
        val baseUrl = uiState.value.thresholds.backendBaseUrl
        if (baseUrl.isBlank()) {
            localError.value = "Configure primero la URL HTTPS del servidor backend en Umbrales."
            return
        }
        viewModelScope.launch {
            repository.syncTelemetryFromBackend(baseUrl)
                .onSuccess { msg -> localBanner.value = msg }
                .onFailure { e -> localError.value = "No se pudo conectar al backend: ${e.localizedMessage}" }
        }
    }

    private data class RealStreamBundle(
        val profile: UserProfile?,
        val agencies: List<Agency>,
        val devices: List<NetworkDevice>,
        val incidents: List<Incident>,
        val alerts: List<AlertEvent>
    )

    private data class ConfigBundle(
        val thresholds: MonitoringThresholds,
        val team: List<TeamMember>,
        val audit: List<AuditLog>,
        val isDemo: Boolean,
        val version: Int
    )
}
