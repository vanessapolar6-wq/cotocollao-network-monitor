package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AlertSeverity
import com.example.ui.components.EmptyTelemetryNotice
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowBg
import com.example.util.AvailabilityCalculator
import com.example.util.LocalNotificationHelper

@Composable
fun AlertsAndEventsScreen(
    state: NocWorkspaceState,
    actions: NocActions
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var severityFilter by remember { mutableStateOf<AlertSeverity?>(null) }
    var hasNotifPermission by remember {
        mutableStateOf(LocalNotificationHelper.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    val canViewIp = state.effectiveRole.canViewIp
    val filteredAlerts = remember(state.alerts, severityFilter, selectedTab) {
        state.alerts.filter { alert ->
            val tabMatch = if (selectedTab == 0) !alert.acknowledged else true
            val sevMatch = severityFilter == null || alert.severityEnum == severityFilter
            tabMatch && sevMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alerts_screen")
    ) {
        TabRow(selectedTabIndex = selectedTab, containerColor = PureWhite) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Centro de Alertas (${state.alerts.count { !it.acknowledged }})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Historial de Eventos (${state.alerts.size})") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusYellowBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = StatusYellow)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Activar notificaciones push locales", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Permita alertas en pantalla ante caídas de enlaces o equipos.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange),
                                modifier = Modifier.testTag("grant_notif_btn")
                            ) {
                                Text("Activar")
                            }
                        }
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
                    FilterChip(selected = severityFilter == null, onClick = { severityFilter = null }, label = { Text("Todas", maxLines = 1) })
                    FilterChip(selected = severityFilter == AlertSeverity.CRITICAL, onClick = { severityFilter = AlertSeverity.CRITICAL }, label = { Text("Críticas", maxLines = 1) })
                    FilterChip(selected = severityFilter == AlertSeverity.WARNING, onClick = { severityFilter = AlertSeverity.WARNING }, label = { Text("Adv.", maxLines = 1) })
                    FilterChip(selected = severityFilter == AlertSeverity.RECOVERY, onClick = { severityFilter = AlertSeverity.RECOVERY }, label = { Text("Recup.", maxLines = 1) })
                }
            }

            if (state.isDemoMode && selectedTab == 0) {
                item {
                    OutlinedButton(
                        onClick = { actions.triggerDemoTestAlert() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alerts_demo_trigger_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = CotocollaoOrange)
                        Spacer(Modifier.size(8.dp))
                        Text("Simular nueva alerta crítica")
                    }
                }
            }

            if (filteredAlerts.isEmpty()) {
                item {
                    EmptyTelemetryNotice(
                        title = if (selectedTab == 0) "Sin alertas pendientes" else "Historial de eventos vacío",
                        description = "Las alertas automáticas por caída WAN, SNMPv3, CPU/RAM/Temp y recuperaciones aparecerán aquí con control de duplicidad.",
                        icon = if (selectedTab == 0) Icons.Filled.NotificationsActive else Icons.Filled.History
                    )
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    val (bg, fg) = when (alert.severityEnum) {
                        AlertSeverity.CRITICAL -> StatusRedBg to StatusRed
                        AlertSeverity.WARNING -> StatusYellowBg to StatusYellow
                        AlertSeverity.RECOVERY -> StatusGreenBg to StatusGreen
                        AlertSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_card_${alert.id}"),
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
                                Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "${alert.severityEnum.labelEs} • x${alert.occurrences}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = fg,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = AvailabilityCalculator.formatTimestamp(alert.eventEpochMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "${alert.agencyName} • ${alert.incidentType}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Equipo: ${alert.deviceName} | IP: ${AvailabilityCalculator.maskIpIfUnauthorized(alert.deviceIp, canViewIp)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = alert.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Acción recomendada: ${alert.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CotocollaoOrange
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado: ${alert.currentStatus}" + if (alert.durationSeconds > 0) " (${AvailabilityCalculator.formatDuration(alert.durationSeconds)})" else "",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                if (!alert.acknowledged && state.effectiveRole.canManageIncidents) {
                                    OutlinedButton(
                                        onClick = { actions.acknowledgeAlert(alert.id) },
                                        modifier = Modifier.testTag("ack_alert_${alert.id}")
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.size(4.dp))
                                        Text("Reconocer")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
