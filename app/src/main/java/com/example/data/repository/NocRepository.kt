package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Agency
import com.example.data.model.AlertEvent
import com.example.data.model.AuditLog
import com.example.data.model.Incident
import com.example.data.model.MonitoringThresholds
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.TrafficLightStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.remote.NocBackendApi
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NocRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "vanessapolar6@gmail.com"
        val INITIAL_SEVEN_AGENCIES = listOf(
            Triple("matriz", "AG-01", "MATRIZ" to "Quito Norte - Cotocollao"),
            Triple("calderon", "AG-02", "CALDERÓN" to "Quito Norte - Calderón"),
            Triple("sangolqui", "AG-03", "SANGOLQUÍ" to "Valle de los Chillos"),
            Triple("sur", "AG-04", "SUR" to "Quito Sur - El Recreo"),
            Triple("pomasqui", "AG-05", "POMASQUI" to "Quito Norte - Pomasqui"),
            Triple("san_antonio", "AG-06", "SAN ANTONIO" to "Mitad del Mundo"),
            Triple("centro", "AG-07", "CENTRO" to "Quito Centro Histórico")
        )
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Debe iniciar sesión con Google antes de acceder a Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        val path = "users/$uid"
        emitAll(
            userDoc(uid).snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.GET, path, uid) }
        )
    }

    fun observeAgencies(uid: String): Flow<List<Agency>> = flow {
        val path = "users/$uid/agencies"
        emitAll(
            userDoc(uid).collection("agencies").snapshots()
                .map { snap ->
                    snap.toObjects(Agency::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.code }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeDevices(uid: String): Flow<List<NetworkDevice>> = flow {
        val path = "users/$uid/devices"
        emitAll(
            userDoc(uid).collection("devices").snapshots()
                .map { snap ->
                    snap.toObjects(NetworkDevice::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.agencyName }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeIncidents(uid: String): Flow<List<Incident>> = flow {
        val path = "users/$uid/incidents"
        emitAll(
            userDoc(uid).collection("incidents").snapshots()
                .map { snap ->
                    snap.toObjects(Incident::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.startedAtEpochMs }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeAlerts(uid: String): Flow<List<AlertEvent>> = flow {
        val path = "users/$uid/alerts"
        emitAll(
            userDoc(uid).collection("alerts").snapshots()
                .map { snap ->
                    snap.toObjects(AlertEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.eventEpochMs }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeThresholds(uid: String): Flow<MonitoringThresholds> = flow {
        val path = "users/$uid/thresholds/default"
        emitAll(
            userDoc(uid).collection("thresholds").document("default").snapshots()
                .map { snap ->
                    snap.toObject(MonitoringThresholds::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: MonitoringThresholds(userId = uid)
                }
                .catch { error -> handleStreamError(error, OperationType.GET, path, uid) }
        )
    }

    fun observeTeamMembers(uid: String): Flow<List<TeamMember>> = flow {
        val path = "users/$uid/team_members"
        emitAll(
            userDoc(uid).collection("team_members").snapshots()
                .map { snap ->
                    snap.toObjects(TeamMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.fullName }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeAuditLogs(uid: String): Flow<List<AuditLog>> = flow {
        val path = "users/$uid/audit_logs"
        emitAll(
            userDoc(uid).collection("audit_logs").snapshots()
                .map { snap ->
                    snap.toObjects(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.timestampEpochMs }
                }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    private fun handleStreamError(error: Throwable, op: OperationType, path: String, uid: String): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
        throw error
    }

    suspend fun ensureWorkspaceInitialized(): Result<Unit> = runFirestoreWrite(OperationType.CREATE, "users/init") {
        val user = auth.currentUser ?: throw IllegalStateException("Sesión no iniciada")
        val uid = user.uid
        val profileRef = userDoc(uid)
        val profileSnap = profileRef.get().await()
        if (!profileSnap.exists()) {
            val isBootstrappedAdmin = user.isEmailVerified && user.email == BOOTSTRAPPED_ADMIN_EMAIL
            val initialRole = if (isBootstrappedAdmin) UserRole.ADMIN.name else UserRole.MANAGER.name
            profileRef.set(
                mapOf(
                    "userId" to uid,
                    "displayName" to (user.displayName?.take(80)?.ifBlank { "Operador NOC" } ?: "Operador NOC"),
                    "email" to (user.email?.take(120) ?: "operador@cotocollao.fin.ec"),
                    "role" to initialRole,
                    "active" to true,
                    "department" to "Departamento de Tecnología",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        val existingAgencies = userDoc(uid).collection("agencies").get().await()
        if (existingAgencies.isEmpty) {
            val batch = db.batch()
            INITIAL_SEVEN_AGENCIES.forEach { (agencyId, code, nameZone) ->
                val ref = userDoc(uid).collection("agencies").document(agencyId)
                batch.set(
                    ref,
                    mapOf(
                        "userId" to uid,
                        "code" to code,
                        "name" to nameZone.first,
                        "cityZone" to nameZone.second,
                        "publicIp" to "",
                        "gatewayIp" to "",
                        "switchIp" to "",
                        "firewallIp" to "",
                        "ispProvider" to "",
                        "contractedBandwidthMbps" to 0L,
                        "status" to TrafficLightStatus.GRAY.name,
                        "linkStatus" to TrafficLightStatus.GRAY.name,
                        "switchStatus" to TrafficLightStatus.GRAY.name,
                        "firewallStatus" to TrafficLightStatus.GRAY.name,
                        "latencyMs" to 0L,
                        "packetLossPct" to 0.0,
                        "availabilityPct" to 0.0,
                        "downtimeSeconds" to 0L,
                        "noTelemetrySeconds" to 0L,
                        "lastCheckEpochMs" to 0L,
                        "lastConnectionEpochMs" to 0L,
                        "configured" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
        }

        val thresholdRef = userDoc(uid).collection("thresholds").document("default")
        if (!thresholdRef.get().await().exists()) {
            thresholdRef.set(
                mapOf(
                    "userId" to uid,
                    "pingIntervalSec" to 30L,
                    "pingTimeoutSec" to 3L,
                    "failuresForDown" to 3L,
                    "successesForRecovery" to 2L,
                    "cpuThresholdPct" to 85L,
                    "ramThresholdPct" to 85L,
                    "tempThresholdC" to 70L,
                    "packetLossThresholdPct" to 5.0,
                    "latencyThresholdMs" to 120L,
                    "backendBaseUrl" to "",
                    "dedupWindowMinutes" to 10L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun saveAgencyConfig(
        agencyId: String,
        cityZone: String,
        publicIp: String,
        gatewayIp: String,
        switchIp: String,
        firewallIp: String,
        ispProvider: String,
        bandwidthMbps: Long,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.UPDATE, "users/agencies/$agencyId") {
        val uid = requireUserId()
        require(agencyId.isNotBlank())
        val isConfigured = publicIp.isNotBlank() || gatewayIp.isNotBlank()
        userDoc(uid).collection("agencies").document(agencyId).update(
            mapOf(
                "cityZone" to cityZone.take(80),
                "publicIp" to publicIp.trim().take(64),
                "gatewayIp" to gatewayIp.trim().take(64),
                "switchIp" to switchIp.trim().take(64),
                "firewallIp" to firewallIp.trim().take(64),
                "ispProvider" to ispProvider.trim().take(80),
                "contractedBandwidthMbps" to bandwidthMbps.coerceIn(0L, 100000L),
                "configured" to isConfigured,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        appendAuditLog(uid, actorEmail, actorRole, "CONFIGURAR_AGENCIA", agencyId, "Actualización de direccionamiento IP y enlace ISP")
    }

    suspend fun saveDevice(
        device: NetworkDevice,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.WRITE, "users/devices") {
        val uid = requireUserId()
        val docId = device.id.ifBlank { "dev_${System.currentTimeMillis()}" }
        val docRef = userDoc(uid).collection("devices").document(docId)
        val snap = docRef.get().await()
        if (snap.exists()) {
            docRef.update(
                mapOf(
                    "agencyId" to device.agencyId,
                    "agencyName" to device.agencyName.take(60),
                    "deviceType" to device.deviceType,
                    "hostname" to device.hostname.trim().take(80),
                    "ipAddress" to device.ipAddress.trim().take(64),
                    "brand" to device.brand.trim().take(60),
                    "model" to device.model.trim().take(60),
                    "serialNumber" to device.serialNumber.trim().take(80),
                    "firmwareVersion" to device.firmwareVersion.trim().take(60),
                    "protocol" to device.protocol,
                    "secretRefId" to device.secretRefId.trim().take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "agencyId" to device.agencyId,
                    "agencyName" to device.agencyName.take(60),
                    "deviceType" to device.deviceType,
                    "hostname" to device.hostname.trim().take(80),
                    "ipAddress" to device.ipAddress.trim().take(64),
                    "brand" to device.brand.trim().take(60),
                    "model" to device.model.trim().take(60),
                    "serialNumber" to device.serialNumber.trim().take(80),
                    "firmwareVersion" to device.firmwareVersion.trim().take(60),
                    "protocol" to device.protocol,
                    "secretRefId" to device.secretRefId.trim().take(120),
                    "status" to TrafficLightStatus.GRAY.name,
                    "cpuPct" to 0L,
                    "ramPct" to 0L,
                    "tempCelsius" to 0L,
                    "uptimeSeconds" to 0L,
                    "activeInterfaces" to 0L,
                    "totalInterfaces" to device.totalInterfaces.coerceIn(0L, 512L),
                    "txErrors" to 0L,
                    "droppedPackets" to 0L,
                    "vpnTunnelsUp" to 0L,
                    "vpnTunnelsTotal" to device.vpnTunnelsTotal.coerceIn(0L, 256L),
                    "lastCheckEpochMs" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        appendAuditLog(uid, actorEmail, actorRole, "GUARDAR_DISPOSITIVO", device.hostname, "Equipo ${device.deviceType} en ${device.agencyName}")
    }

    suspend fun createIncident(
        incident: Incident,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.CREATE, "users/incidents") {
        val uid = requireUserId()
        val docId = incident.id.ifBlank { "inc_${System.currentTimeMillis()}" }
        userDoc(uid).collection("incidents").document(docId).set(
            mapOf(
                "userId" to uid,
                "code" to incident.code.take(32),
                "agencyId" to incident.agencyId,
                "agencyName" to incident.agencyName.take(60),
                "deviceId" to incident.deviceId.take(128),
                "deviceName" to incident.deviceName.take(80),
                "deviceType" to incident.deviceType,
                "failureType" to incident.failureType.take(80),
                "severity" to incident.severity,
                "status" to incident.status,
                "startedAtEpochMs" to incident.startedAtEpochMs.coerceAtLeast(0L),
                "recoveredAtEpochMs" to incident.recoveredAtEpochMs.coerceAtLeast(0L),
                "durationSeconds" to incident.durationSeconds.coerceAtLeast(0L),
                "assignedTo" to incident.assignedTo.take(80),
                "diagnosis" to incident.diagnosis.take(1000),
                "solution" to incident.solution.take(1000),
                "observations" to incident.observations.take(1000),
                "evidenceNote" to incident.evidenceNote.take(500),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        appendAuditLog(uid, actorEmail, actorRole, "REGISTRAR_INCIDENTE", incident.code, "${incident.agencyName} - ${incident.failureType}")
    }

    suspend fun updateIncident(
        incident: Incident,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.UPDATE, "users/incidents/${incident.id}") {
        val uid = requireUserId()
        require(incident.id.isNotBlank())
        userDoc(uid).collection("incidents").document(incident.id).update(
            mapOf(
                "status" to incident.status,
                "severity" to incident.severity,
                "recoveredAtEpochMs" to incident.recoveredAtEpochMs.coerceAtLeast(0L),
                "durationSeconds" to incident.durationSeconds.coerceAtLeast(0L),
                "assignedTo" to incident.assignedTo.take(80),
                "diagnosis" to incident.diagnosis.take(1000),
                "solution" to incident.solution.take(1000),
                "observations" to incident.observations.take(1000),
                "evidenceNote" to incident.evidenceNote.take(500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        appendAuditLog(uid, actorEmail, actorRole, "ACTUALIZAR_INCIDENTE", incident.code, "Estado: ${incident.status}")
    }

    suspend fun acknowledgeAlert(alertId: String, actorEmail: String, actorRole: UserRole): Result<Unit> =
        runFirestoreWrite(OperationType.UPDATE, "users/alerts/$alertId") {
            val uid = requireUserId()
            userDoc(uid).collection("alerts").document(alertId).update(
                mapOf(
                    "acknowledged" to true,
                    "currentStatus" to "RECONOCIDA",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, actorEmail, actorRole, "RECONOCER_ALERTA", alertId, "Alerta marcada como atendida")
        }

    suspend fun saveThresholds(
        thresholds: MonitoringThresholds,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.UPDATE, "users/thresholds/default") {
        val uid = requireUserId()
        userDoc(uid).collection("thresholds").document("default").update(
            mapOf(
                "pingIntervalSec" to thresholds.pingIntervalSec.coerceIn(5L, 3600L),
                "pingTimeoutSec" to thresholds.pingTimeoutSec.coerceIn(1L, 60L),
                "failuresForDown" to thresholds.failuresForDown.coerceIn(1L, 20L),
                "successesForRecovery" to thresholds.successesForRecovery.coerceIn(1L, 20L),
                "cpuThresholdPct" to thresholds.cpuThresholdPct.coerceIn(10L, 100L),
                "ramThresholdPct" to thresholds.ramThresholdPct.coerceIn(10L, 100L),
                "tempThresholdC" to thresholds.tempThresholdC.coerceIn(30L, 120L),
                "packetLossThresholdPct" to thresholds.packetLossThresholdPct.coerceIn(0.5, 100.0),
                "latencyThresholdMs" to thresholds.latencyThresholdMs.coerceIn(10L, 10000L),
                "backendBaseUrl" to thresholds.backendBaseUrl.trim().take(250),
                "dedupWindowMinutes" to thresholds.dedupWindowMinutes.coerceIn(1L, 1440L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        appendAuditLog(uid, actorEmail, actorRole, "ACTUALIZAR_UMBRALES", "default", "Intervalo ${thresholds.pingIntervalSec}s, CPU ${thresholds.cpuThresholdPct}%")
    }

    suspend fun saveTeamMember(
        member: TeamMember,
        actorEmail: String,
        actorRole: UserRole
    ): Result<Unit> = runFirestoreWrite(OperationType.WRITE, "users/team_members") {
        val uid = requireUserId()
        val docId = member.id.ifBlank { "usr_${System.currentTimeMillis()}" }
        val ref = userDoc(uid).collection("team_members").document(docId)
        if (ref.get().await().exists()) {
            ref.update(
                mapOf(
                    "fullName" to member.fullName.trim().take(80),
                    "email" to member.email.trim().take(120),
                    "role" to member.role,
                    "active" to member.active,
                    "department" to member.department.trim().take(80),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            ref.set(
                mapOf(
                    "userId" to uid,
                    "fullName" to member.fullName.trim().take(80),
                    "email" to member.email.trim().take(120),
                    "role" to member.role,
                    "active" to member.active,
                    "department" to member.department.trim().take(80),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        appendAuditLog(uid, actorEmail, actorRole, "GESTION_USUARIO", member.email, "Rol asignado: ${member.role}")
    }

    suspend fun syncTelemetryFromBackend(baseUrl: String): Result<String> {
        return try {
            val uid = requireUserId()
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: throw IllegalStateException("Token Firebase no disponible")
            val api = NocBackendApi.create(baseUrl)
            val bearer = "Bearer $token"
            val dashboard = api.getDashboard(bearer)
            val remoteAgencies = api.getAgencies(bearer).agencies

            val batch = db.batch()
            remoteAgencies.forEach { remote ->
                if (remote.id.isNotBlank()) {
                    val ref = userDoc(uid).collection("agencies").document(remote.id)
                    batch.update(
                        ref,
                        mapOf(
                            "status" to remote.status,
                            "linkStatus" to remote.linkStatus,
                            "switchStatus" to remote.switchStatus,
                            "firewallStatus" to remote.firewallStatus,
                            "latencyMs" to remote.latencyMs,
                            "packetLossPct" to remote.packetLossPct,
                            "availabilityPct" to remote.availabilityPct,
                            "downtimeSeconds" to remote.downtimeSeconds,
                            "noTelemetrySeconds" to remote.noTelemetrySeconds,
                            "lastCheckEpochMs" to remote.lastCheckEpochMs,
                            "lastConnectionEpochMs" to remote.lastConnectionEpochMs,
                            "configured" to remote.configured,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.commit().await()
            val statusMsg = if (dashboard.telemetryConnected) {
                "Telemetría sincronizada (${remoteAgencies.size} agencias, colector activo)"
            } else {
                "Backend conectado; esperando primer heartbeat del colector on-premise"
            }
            Result.success(statusMsg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun appendAuditLog(
        uid: String,
        actorEmail: String,
        actorRole: UserRole,
        action: String,
        target: String,
        details: String
    ) {
        val logId = "log_${System.currentTimeMillis()}"
        userDoc(uid).collection("audit_logs").document(logId).set(
            mapOf(
                "userId" to uid,
                "actorEmail" to actorEmail.take(120),
                "actorRole" to actorRole.name,
                "action" to action.take(80),
                "target" to target.take(120),
                "details" to details.take(500),
                "timestampEpochMs" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private suspend inline fun runFirestoreWrite(
        op: OperationType,
        path: String,
        crossinline block: suspend () -> Unit
    ): Result<Unit> {
        return try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, op, path)
            Result.failure(e)
        }
    }
}
