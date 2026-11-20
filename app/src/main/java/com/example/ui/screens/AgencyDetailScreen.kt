package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.TrafficLightBadge
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.util.AvailabilityCalculator

@Composable
fun AgencyDetailScreen(
    agencyId: String,
    state: NocWorkspaceState,
    actions: NocActions,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val agency = state.agencies.firstOrNull { it.id == agencyId }
    val agencyDevices = remember(state.devices, agencyId) {
        state.devices.filter { it.agencyId == agencyId }
    }
    var showEditDialog by remember { mutableStateOf(false) }

    if (agency == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Agencia no encontrada.")
            TextButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    val canEdit = state.effectiveRole.canEditInfra
    val canViewIp = state.effectiveRole.canViewIp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("agency_detail_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("agency_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                    Column {
                        Text("${agency.code} • ${agency.name}", style = MaterialTheme.typography.titleLarge)
                        Text(agency.cityZone, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (canEdit) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("edit_agency_btn")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Configurar Agencia", tint = CotocollaoOrange)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ficha Técnica de Infraestructura", style = MaterialTheme.typography.titleMedium)
                        TrafficLightBadge(agency.overallLight)
                    }
                    InfoRow("IP Pública (Enlace WAN)", AvailabilityCalculator.maskIpIfUnauthorized(agency.publicIp, canViewIp))
                    InfoRow("IP Router / Gateway", AvailabilityCalculator.maskIpIfUnauthorized(agency.gatewayIp, canViewIp))
                    InfoRow("IP Switch Capa 3", AvailabilityCalculator.maskIpIfUnauthorized(agency.switchIp, canViewIp))
                    InfoRow("IP Firewall", AvailabilityCalculator.maskIpIfUnauthorized(agency.firewallIp, canViewIp))
                    InfoRow("Proveedor de Internet", agency.ispProvider.ifBlank { "Sin configurar" })
                    InfoRow("Ancho de banda contratado", if (agency.contractedBandwidthMbps > 0) "${agency.contractedBandwidthMbps} Mbps" else "Sin configurar")
                    InfoRow("Latencia / Pérdida paquetes", "${agency.latencyMs} ms / ${agency.packetLossPct}%")
                    InfoRow("Disponibilidad", if (agency.configured) "${agency.availabilityPct}%" else "Sin telemetría")
                    InfoRow("Última comprobación", AvailabilityCalculator.formatTimestamp(agency.lastCheckEpochMs))
                }
            }
        }

        item {
            Text(
                text = "Equipos Registrados en ${agency.name} (${agencyDevices.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (agencyDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Text(
                        text = "No hay switches o firewalls registrados en esta agencia. Agréguelos desde Configuración > Administración de Dispositivos.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(agencyDevices, key = { it.id }) { dev ->
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
                            Text("${dev.hostname} (${dev.typeEnum.labelEs})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TrafficLightBadge(dev.statusLight, compact = true)
                        }
                        InfoRow("Marca / Modelo", "${dev.brand.ifBlank { "-" }} ${dev.model}")
                        InfoRow("Nro. Serie / Firmware", "${dev.serialNumber.ifBlank { "-" }} • ${dev.firmwareVersion.ifBlank { "-" }}")
                        InfoRow("Dirección IP", AvailabilityCalculator.maskIpIfUnauthorized(dev.ipAddress, canViewIp))
                        InfoRow("Protocolo / Secreto", "${dev.protocolEnum.labelEs} (${dev.secretRefId.ifBlank { "Sin ref" }})")
                        InfoRow("CPU / RAM / Temp", "${dev.cpuPct}% / ${dev.ramPct}% / ${dev.tempCelsius}°C")
                        InfoRow("Uptime", AvailabilityCalculator.formatDuration(dev.uptimeSeconds))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var cityZone by remember { mutableStateOf(agency.cityZone) }
        var publicIp by remember { mutableStateOf(agency.publicIp) }
        var gatewayIp by remember { mutableStateOf(agency.gatewayIp) }
        var switchIp by remember { mutableStateOf(agency.switchIp) }
        var firewallIp by remember { mutableStateOf(agency.firewallIp) }
        var isp by remember { mutableStateOf(agency.ispProvider) }
        var bw by remember { mutableStateOf(if (agency.contractedBandwidthMbps > 0) agency.contractedBandwidthMbps.toString() else "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Configurar ${agency.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = cityZone, onValueChange = { cityZone = it }, label = { Text("Zona / Dirección") }, singleLine = true)
                    OutlinedTextField(value = publicIp, onValueChange = { publicIp = it }, label = { Text("IP Pública Enlace WAN") }, singleLine = true)
                    OutlinedTextField(value = gatewayIp, onValueChange = { gatewayIp = it }, label = { Text("IP Router / Gateway") }, singleLine = true)
                    OutlinedTextField(value = switchIp, onValueChange = { switchIp = it }, label = { Text("IP Switch Capa 3") }, singleLine = true)
                    OutlinedTextField(value = firewallIp, onValueChange = { firewallIp = it }, label = { Text("IP Firewall") }, singleLine = true)
                    OutlinedTextField(value = isp, onValueChange = { isp = it }, label = { Text("Proveedor ISP") }, singleLine = true)
                    OutlinedTextField(
                        value = bw,
                        onValueChange = { bw = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Ancho de banda (Mbps)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        actions.saveAgencyConfig(
                            agencyId = agency.id,
                            cityZone = cityZone,
                            publicIp = publicIp,
                            gatewayIp = gatewayIp,
                            switchIp = switchIp,
                            firewallIp = firewallIp,
                            ispProvider = isp,
                            bandwidthMbps = bw.toLongOrNull() ?: 0L
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}
