package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Incident
import com.example.data.model.IncidentSeverity
import com.example.data.model.IncidentStatus
import com.example.ui.components.EmptyTelemetryNotice
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowBg
import com.example.util.AvailabilityCalculator

@Composable
fun IncidentsScreen(
    state: NocWorkspaceState,
    actions: NocActions
) {
    var statusFilter by remember { mutableStateOf<IncidentStatus?>(null) }
    var agencyFilter by remember { mutableStateOf<String?>(null) }
    var editingIncident by remember { mutableStateOf<Incident?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val canManage = state.effectiveRole.canManageIncidents

    val filtered = remember(state.incidents, statusFilter, agencyFilter) {
        state.incidents.filter { inc ->
            (statusFilter == null || inc.statusEnum == statusFilter) &&
                (agencyFilter == null || inc.agencyId == agencyFilter)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = SoftSurface,
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = CotocollaoOrange,
                    contentColor = PureWhite,
                    modifier = Modifier.testTag("add_incident_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar incidente")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("incidents_list"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gestión de Incidentes", style = MaterialTheme.typography.titleLarge)
                        Text("Ciclo: Nuevo → En proceso → Escalado → Resuelto → Cerrado", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("Todos (${state.incidents.size})", maxLines = 1) }
                    )
                    IncidentStatus.entries.forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = if (statusFilter == st) null else st },
                            label = { Text(st.labelEs, maxLines = 1) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = agencyFilter == null,
                        onClick = { agencyFilter = null },
                        label = { Text("Todas las agencias", maxLines = 1) }
                    )
                    state.agencies.forEach { ag ->
                        FilterChip(
                            selected = agencyFilter == ag.id,
                            onClick = { agencyFilter = if (agencyFilter == ag.id) null else ag.id },
                            label = { Text(ag.name, maxLines = 1) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyTelemetryNotice(
                        title = "Sin incidentes para los filtros aplicados",
                        description = "Los incidentes automáticos o registrados manualmente se listan aquí con su diagnóstico y solución.",
                        icon = Icons.AutoMirrored.Filled.Assignment
                    )
                }
            } else {
                items(filtered, key = { it.id }) { inc ->
                    val (badgeBg, badgeFg) = when (inc.severityEnum) {
                        IncidentSeverity.CRITICAL -> StatusRedBg to StatusRed
                        IncidentSeverity.HIGH -> StatusYellowBg to StatusYellow
                        else -> StatusGreenBg to StatusGreen
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canManage) { editingIncident = inc }
                            .testTag("incident_card_${inc.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, OutlineSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${inc.code} • ${inc.agencyName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "${inc.statusEnum.labelEs} • ${inc.severityEnum.labelEs}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(inc.failureType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            InfoRow("Equipo", "${inc.deviceName} (${inc.deviceType})")
                            InfoRow("Inicio", AvailabilityCalculator.formatTimestamp(inc.startedAtEpochMs))
                            InfoRow("Duración", AvailabilityCalculator.formatDuration(inc.durationSeconds))
                            InfoRow("Responsable", inc.assignedTo.ifBlank { "Sin asignar" })
                            if (inc.diagnosis.isNotBlank()) {
                                Text("Diagnóstico: ${inc.diagnosis}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (inc.solution.isNotBlank()) {
                                Text("Solución: ${inc.solution}", style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val defaultAgency = state.agencies.firstOrNull()
        var selectedAgencyId by remember { mutableStateOf(defaultAgency?.id ?: "matriz") }
        var deviceName by remember { mutableStateOf("Router Principal") }
        var failureType by remember { mutableStateOf("DEGRADACIÓN DE ENLACE WAN") }
        var assignedTo by remember { mutableStateOf(state.userProfile.displayName.ifBlank { "Técnico NOC" }) }
        var diagnosis by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Registrar Incidente") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.agencies.forEach { ag ->
                            FilterChip(
                                selected = selectedAgencyId == ag.id,
                                onClick = { selectedAgencyId = ag.id },
                                label = { Text(ag.name, maxLines = 1) }
                            )
                        }
                    }
                    OutlinedTextField(value = deviceName, onValueChange = { deviceName = it }, label = { Text("Dispositivo afectado") }, singleLine = true)
                    OutlinedTextField(value = failureType, onValueChange = { failureType = it }, label = { Text("Tipo de falla") }, singleLine = true)
                    OutlinedTextField(value = assignedTo, onValueChange = { assignedTo = it }, label = { Text("Responsable asignado") }, singleLine = true)
                    OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico inicial") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ag = state.agencies.firstOrNull { it.id == selectedAgencyId }
                        actions.createIncident(
                            Incident(
                                code = "INC-${System.currentTimeMillis().toString().takeLast(4)}",
                                agencyId = ag?.id ?: "matriz",
                                agencyName = ag?.name ?: "MATRIZ",
                                deviceId = "manual_${System.currentTimeMillis()}",
                                deviceName = deviceName,
                                deviceType = "INTERNET_LINK",
                                failureType = failureType,
                                severity = IncidentSeverity.HIGH.name,
                                status = IncidentStatus.NEW.name,
                                startedAtEpochMs = System.currentTimeMillis(),
                                assignedTo = assignedTo,
                                diagnosis = diagnosis
                            )
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    editingIncident?.let { inc ->
        var selectedStatus by remember(inc.id) { mutableStateOf(inc.statusEnum) }
        var diagnosis by remember(inc.id) { mutableStateOf(inc.diagnosis) }
        var solution by remember(inc.id) { mutableStateOf(inc.solution) }
        var observations by remember(inc.id) { mutableStateOf(inc.observations) }
        var evidenceNote by remember(inc.id) { mutableStateOf(inc.evidenceNote) }

        val photoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                evidenceNote = "Evidencia adjunta: ${uri.lastPathSegment ?: uri.toString()}"
            }
        }

        AlertDialog(
            onDismissRequest = { editingIncident = null },
            title = { Text("${inc.code} • ${inc.agencyName}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Estado del incidente:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IncidentStatus.entries.forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.labelEs, maxLines = 1) }
                            )
                        }
                    }
                    OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico técnico") })
                    OutlinedTextField(value = solution, onValueChange = { solution = it }, label = { Text("Solución aplicada") })
                    OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones") })
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Adjuntar evidencia (Photo Picker)")
                    }
                    if (evidenceNote.isNotBlank()) {
                        Text(evidenceNote, style = MaterialTheme.typography.bodySmall, color = CotocollaoOrange)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val isResolved = selectedStatus == IncidentStatus.RESOLVED || selectedStatus == IncidentStatus.CLOSED
                        val recoveredAt = if (isResolved && inc.recoveredAtEpochMs == 0L) now else inc.recoveredAtEpochMs
                        val duration = if (isResolved && inc.startedAtEpochMs > 0L) {
                            ((recoveredAt - inc.startedAtEpochMs) / 1000L).coerceAtLeast(1L)
                        } else inc.durationSeconds
                        actions.updateIncident(
                            inc.copy(
                                status = selectedStatus.name,
                                recoveredAtEpochMs = recoveredAt,
                                durationSeconds = duration,
                                diagnosis = diagnosis,
                                solution = solution,
                                observations = observations,
                                evidenceNote = evidenceNote
                            )
                        )
                        editingIncident = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingIncident = null }) { Text("Cancelar") }
            }
        )
    }
}
