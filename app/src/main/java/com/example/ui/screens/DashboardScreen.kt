package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TrafficLightStatus
import com.example.ui.components.AgencyAvailabilityChartCard
import com.example.ui.components.FaultDistributionDonutCard
import com.example.ui.components.IncidentEvolutionChartCard
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.LatencyComparisonChartCard
import com.example.ui.components.MonthlyAvailabilityBarChartCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrafficLightBadge
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.CotocollaoOrangeDark
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun DashboardScreen(
    state: NocWorkspaceState,
    actions: NocActions,
    onOpenInternetLinks: () -> Unit,
    onOpenSwitches: () -> Unit,
    onOpenFirewalls: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenAgency: (String) -> Unit
) {
    val dash = state.dashboard
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero availability card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(CotocollaoOrange, CotocollaoOrangeDark)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DISPONIBILIDAD INSTITUCIONAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite.copy(alpha = 0.88f)
                            )
                            Surface(
                                color = PureWhite.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (dash.telemetryConnected || state.isDemoMode) "TELEMETRÍA ACTIVA" else "SIN COLECTOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = if (dash.telemetryConnected || state.isDemoMode) {
                                "${dash.overallAvailabilityPct}%"
                            } else {
                                "Sin telemetría"
                            },
                            style = MaterialTheme.typography.displaySmall,
                            color = PureWhite
                        )

                        Text(
                            text = "${dash.totalAgencies} agencias monitoreadas • ${dash.operationalAgencies} operativas • ${dash.agenciesWithIncidents} con incidencias",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite.copy(alpha = 0.92f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (state.isDemoMode) {
                                Button(
                                    onClick = { actions.simulateDemoLinkDrop("calderon") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("demo_drop_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PureWhite,
                                        contentColor = CotocollaoOrangeDark
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Simular caída", style = MaterialTheme.typography.labelLarge)
                                }
                                OutlinedButton(
                                    onClick = { actions.simulateDemoLinkRecovery("calderon") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("demo_recover_button"),
                                    border = BorderStroke(1.dp, PureWhite),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Recuperar", style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                Button(
                                    onClick = { actions.syncFromBackend() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sync_backend_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PureWhite,
                                        contentColor = CotocollaoOrangeDark
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(6.dp))
                                    Text("Sincronizar servidor")
                                }
                                OutlinedButton(
                                    onClick = { actions.toggleDemoMode(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("enable_demo_button"),
                                    border = BorderStroke(1.dp, PureWhite),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Simulación")
                                }
                            }
                        }
                    }
                }
            }
        }

        // KPI Grid 2 columns
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Agencias Totales",
                        value = "${dash.totalAgencies}",
                        subtitle = "${dash.operationalAgencies} operativas / ${dash.agenciesWithIncidents} inc.",
                        icon = Icons.Filled.Apartment,
                        accentColor = CotocollaoOrange,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Incidentes Críticos",
                        value = "${dash.pendingCriticalIncidents}",
                        subtitle = "Pendientes de cierre",
                        icon = Icons.Filled.ReportProblem,
                        accentColor = if (dash.pendingCriticalIncidents > 0) StatusRed else StatusGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Enlaces Internet",
                        value = "${dash.activeInternetLinks}/${dash.totalAgencies}",
                        subtitle = "${dash.downInternetLinks} enlace(s) caído(s)",
                        icon = if (dash.downInternetLinks > 0) Icons.Filled.CloudOff else Icons.Filled.Wifi,
                        accentColor = if (dash.downInternetLinks > 0) StatusRed else StatusGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenInternetLinks
                    )
                    KpiMetricCard(
                        title = "Switches Capa 3",
                        value = "${dash.operationalSwitches}",
                        subtitle = "${dash.failedSwitches} con falla/alerta",
                        icon = Icons.Filled.Lan,
                        accentColor = if (dash.failedSwitches > 0) StatusYellow else StatusGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSwitches
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Firewalls",
                        value = "${dash.operationalFirewalls}",
                        subtitle = "${dash.failedFirewalls} con falla/alerta",
                        icon = Icons.Filled.Security,
                        accentColor = if (dash.failedFirewalls > 0) StatusYellow else StatusGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFirewalls
                    )
                    KpiMetricCard(
                        title = "Reportes NOC",
                        value = "PDF / XLS",
                        subtitle = "MTTR, MTBF y SLA",
                        icon = Icons.Filled.Assessment,
                        accentColor = CotocollaoOrange,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenReports
                    )
                }
            }
        }

        // Quick Module Shortcuts
        item {
            SectionHeader(
                title = "Supervisión por Capa de Red",
                subtitle = "Acceso directo a enlaces WAN, Switches SNMPv3 y Firewalls"
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickLayerButton("Enlaces WAN", Icons.Filled.Router, onOpenInternetLinks, Modifier.weight(1f), "nav_links_btn")
                QuickLayerButton("Switches L3", Icons.Filled.Dns, onOpenSwitches, Modifier.weight(1f), "nav_switches_btn")
                QuickLayerButton("Firewalls", Icons.Filled.Security, onOpenFirewalls, Modifier.weight(1f), "nav_firewalls_btn")
            }
        }

        // Traffic-light quick matrix of the 7 agencies
        item {
            SectionHeader(
                title = "Semáforo de las 7 Agencias",
                subtitle = "Estado de conectividad en tiempo real"
            )
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.agencies.forEach { agency ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAgency(agency.id) }
                            .testTag("dash_agency_${agency.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, OutlineSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${agency.code} • ${agency.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (agency.configured) {
                                        "ISP: ${agency.ispProvider.ifBlank { "No asignado" }} • Latencia: ${agency.latencyMs} ms"
                                    } else {
                                        "Sin configurar (I.P. y colector pendientes)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TrafficLightBadge(status = agency.overallLight, compact = true)
                        }
                    }
                }
            }
        }

        // 5 Required Charts
        item {
            SectionHeader(
                title = "Analítica de Infraestructura",
                subtitle = "Indicadores calculados sobre telemetría"
            )
        }
        item { AgencyAvailabilityChartCard(state.agencies, state.isDemoMode) }
        item { IncidentEvolutionChartCard(state.chartSeries.incidentTrend30Days, state.isDemoMode) }
        item { MonthlyAvailabilityBarChartCard(state.chartSeries.monthlyAvailability, state.isDemoMode) }
        item { LatencyComparisonChartCard(state.agencies, state.isDemoMode) }
        item { FaultDistributionDonutCard(state.chartSeries.faultDistribution, state.isDemoMode) }

        if (state.isDemoMode) {
            item {
                OutlinedButton(
                    onClick = { actions.triggerDemoTestAlert() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("demo_test_alert_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = CotocollaoOrange)
                    Spacer(Modifier.size(8.dp))
                    Text("Generar alerta push de prueba")
                }
            }
        }
    }
}

@Composable
private fun QuickLayerButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = PureWhite,
        border = BorderStroke(1.dp, OutlineSubtle),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = CotocollaoOrange, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}
