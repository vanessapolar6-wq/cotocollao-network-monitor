package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DeviceType
import com.example.ui.components.EmptyTelemetryNotice
import com.example.ui.components.TrafficLightBadge
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.util.AvailabilityCalculator

@Composable
fun InternetLinksScreen(
    state: NocWorkspaceState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val canViewIp = state.effectiveRole.canViewIp
    val th = state.thresholds

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("internet_links_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("links_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Estado de Enlaces de Internet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "ICMP cada ${th.pingIntervalSec}s • Timeout ${th.pingTimeoutSec}s • Caída ${th.failuresForDown} fallos / Recup. ${th.successesForRecovery} éxitos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(state.agencies, key = { it.id }) { agency ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${agency.code} • ${agency.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        TrafficLightBadge(agency.linkLight, compact = true)
                    }
                    InfoRow("IP Pública WAN", AvailabilityCalculator.maskIpIfUnauthorized(agency.publicIp, canViewIp))
                    InfoRow("Gateway / Router", AvailabilityCalculator.maskIpIfUnauthorized(agency.gatewayIp, canViewIp))
                    InfoRow("Proveedor ISP", agency.ispProvider.ifBlank { "Sin configurar" })
                    InfoRow("Latencia ICMP", "${agency.latencyMs} ms")
                    InfoRow("Pérdida de paquetes", "${agency.packetLossPct}%")
                    InfoRow("Indisponibilidad acumulada", AvailabilityCalculator.formatDuration(agency.downtimeSeconds))
                    InfoRow("Última conexión exitosa", AvailabilityCalculator.formatTimestamp(agency.lastConnectionEpochMs))
                }
            }
        }
    }
}

@Composable
fun SwitchesScreen(
    state: NocWorkspaceState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val switches = state.devices.filter { it.typeEnum == DeviceType.SWITCH_L3 }
    val canViewIp = state.effectiveRole.canViewIp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("switches_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("switches_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monitoreo de Switches Capa 3", style = MaterialTheme.typography.titleLarge)
                    Text("Consultas SNMPv3 (authPriv) ejecutadas por el colector", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (switches.isEmpty()) {
            item {
                EmptyTelemetryNotice(
                    title = "Sin switches configurados",
                    description = "Registre los switches Capa 3 de cada agencia en Configuración > Administración de Dispositivos.",
                    icon = Icons.Filled.Dns
                )
            }
        } else {
            items(switches, key = { it.id }) { sw ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sw.hostname} (${sw.agencyName})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TrafficLightBadge(sw.statusLight, compact = true)
                        }
                        InfoRow("Modelo / IP", "${sw.brand} ${sw.model} • ${AvailabilityCalculator.maskIpIfUnauthorized(sw.ipAddress, canViewIp)}")
                        InfoRow("CPU / Memoria RAM", "${sw.cpuPct}% / ${sw.ramPct}%")
                        InfoRow("Temperatura", "${sw.tempCelsius} °C")
                        InfoRow("Puertos activos / Troncales", "${sw.activeInterfaces} / ${sw.totalInterfaces}")
                        InfoRow("Errores TX / Paq. descartados", "${sw.txErrors} / ${sw.droppedPackets}")
                        InfoRow("Tiempo de actividad (Uptime)", AvailabilityCalculator.formatDuration(sw.uptimeSeconds))
                    }
                }
            }
        }
    }
}

@Composable
fun FirewallsScreen(
    state: NocWorkspaceState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val firewalls = state.devices.filter { it.typeEnum == DeviceType.FIREWALL }
    val canViewIp = state.effectiveRole.canViewIp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("firewalls_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("firewalls_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monitoreo de Firewalls y VPN", style = MaterialTheme.typography.titleLarge)
                    Text("Integración SNMPv3, API HTTPS y Syslog sin almacenar tráfico privado", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (firewalls.isEmpty()) {
            item {
                EmptyTelemetryNotice(
                    title = "Sin firewalls configurados",
                    description = "Registre los firewalls de las 7 agencias desde Administración de Dispositivos.",
                    icon = Icons.Filled.Security
                )
            }
        } else {
            items(firewalls, key = { it.id }) { fw ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${fw.hostname} (${fw.agencyName})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TrafficLightBadge(fw.statusLight, compact = true)
                        }
                        InfoRow("Fabricante / Protocolo", "${fw.brand} ${fw.model} • ${fw.protocolEnum.labelEs}")
                        InfoRow("IP Administración", AvailabilityCalculator.maskIpIfUnauthorized(fw.ipAddress, canViewIp))
                        InfoRow("CPU / RAM / Temp", "${fw.cpuPct}% / ${fw.ramPct}% / ${fw.tempCelsius} °C")
                        InfoRow("Interfaces WAN/LAN activas", "${fw.activeInterfaces} / ${fw.totalInterfaces}")
                        InfoRow("Túneles VPN Site-to-Site", "${fw.vpnTunnelsUp} / ${fw.vpnTunnelsTotal} activos")
                        InfoRow("Uptime", AvailabilityCalculator.formatDuration(fw.uptimeSeconds))
                    }
                }
            }
        }
    }
}
