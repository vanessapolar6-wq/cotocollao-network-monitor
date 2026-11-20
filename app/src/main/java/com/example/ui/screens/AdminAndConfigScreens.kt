package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DeviceType
import com.example.data.model.MonitoringProtocol
import com.example.data.model.NetworkDevice
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import com.example.ui.components.TrafficLightBadge
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.StatusRed
import com.example.util.AvailabilityCalculator

@Composable
fun ConfigHubScreen(
    state: NocWorkspaceState,
    actions: NocActions,
    isUnauthDemoSandbox: Boolean,
    onOpenReports: () -> Unit,
    onOpenDeviceAdmin: () -> Unit,
    onOpenThresholds: () -> Unit,
    onOpenUsersAdmin: () -> Unit,
    onSignOutOrExitDemo: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_hub_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.userProfile.displayName.ifBlank { "Operador NOC" }, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(state.userProfile.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Perfil activo: ${state.effectiveRole.displayNameEs}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CotocollaoOrange
                    )
                    if (state.isDemoMode) {
                        Text("Cambiar rol de operación:", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserRole.entries.forEach { role ->
                                FilterChip(
                                    selected = state.effectiveRole == role,
                                    onClick = { actions.switchDemoRole(role) },
                                    label = { Text(role.displayNameEs.substringBefore(" /"), maxLines = 1) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isUnauthDemoSandbox) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Simulación Local Activa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Simula las 7 agencias y fallas sin alterar los registros reales en Firestore.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isDemoMode,
                            onCheckedChange = { actions.toggleDemoMode(it) },
                            modifier = Modifier.testTag("demo_mode_switch")
                        )
                    }
                }
            }
        }

        item {
            ConfigMenuRow(
                title = "Reportes Estadísticos y Exportación",
                subtitle = "Disponibilidad diaria/semanal/mensual, MTTR, MTBF, PDF y Excel",
                icon = Icons.Filled.Assessment,
                onClick = onOpenReports,
                tag = "menu_reports"
            )
        }
        item {
            ConfigMenuRow(
                title = "Administración de Dispositivos",
                subtitle = "Routers, Switches Capa 3 (SNMPv3) y Firewalls por agencia",
                icon = Icons.Filled.Router,
                onClick = onOpenDeviceAdmin,
                tag = "menu_devices"
            )
        }
        item {
            ConfigMenuRow(
                title = "Configuración de Umbrales y Colector",
                subtitle = "Intervalo PING (30s), CPU/RAM/Temp, URL Backend Cloud Run",
                icon = Icons.Filled.Tune,
                onClick = onOpenThresholds,
                tag = "menu_thresholds"
            )
        }
        item {
            ConfigMenuRow(
                title = "Administración de Usuarios y Auditoría",
                subtitle = "Roles RBAC (${state.teamMembers.size}) y bitácora de auditoría (${state.auditLogs.size})",
                icon = Icons.Filled.People,
                onClick = onOpenUsersAdmin,
                tag = "menu_users"
            )
        }

        if (!state.isDemoMode) {
            item {
                OutlinedButton(
                    onClick = { actions.syncFromBackend() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("config_sync_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, tint = CotocollaoOrange)
                    Spacer(Modifier.size(8.dp))
                    Text("Sincronizar telemetría con Backend REST")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOutOrExitDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = StatusRed)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (isUnauthDemoSandbox) "Ir a Inicio de Sesión" else "Cerrar sesión institucional",
                    color = StatusRed
                )
            }
        }
    }
}

@Composable
private fun ConfigMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = title, tint = CotocollaoOrange)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun DeviceAdminScreen(
    state: NocWorkspaceState,
    actions: NocActions,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val canEdit = state.effectiveRole.canEditInfra
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = SoftSurface,
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CotocollaoOrange,
                    contentColor = PureWhite,
                    modifier = Modifier.testTag("add_device_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar dispositivo")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("device_admin_screen"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Administración de Dispositivos", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (canEdit) "Secretos SNMPv3/API referenciados al servidor (nunca en la app)" else "Modo solo lectura para tu perfil",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(state.devices, key = { it.id }) { dev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${dev.hostname} • ${dev.agencyName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TrafficLightBadge(dev.statusLight, compact = true)
                        }
                        InfoRow("Tipo / Protocolo", "${dev.typeEnum.labelEs} • ${dev.protocolEnum.labelEs}")
                        InfoRow("Marca / Modelo", "${dev.brand} ${dev.model}")
                        InfoRow("IP", AvailabilityCalculator.maskIpIfUnauthorized(dev.ipAddress, state.effectiveRole.canViewIp))
                        InfoRow("Ref. Secreto Servidor", dev.secretRefId.ifBlank { "Sin asignar" })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var agencyId by remember { mutableStateOf(state.agencies.firstOrNull()?.id ?: "matriz") }
        var deviceType by remember { mutableStateOf(DeviceType.SWITCH_L3) }
        var protocol by remember { mutableStateOf(MonitoringProtocol.SNMPV3) }
        var hostname by remember { mutableStateOf("") }
        var ip by remember { mutableStateOf("") }
        var brand by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("") }
        var serial by remember { mutableStateOf("") }
        var firmware by remember { mutableStateOf("") }
        var secretRef by remember { mutableStateOf("vault://snmpv3/") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Registrar Dispositivo") },
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
                            FilterChip(selected = agencyId == ag.id, onClick = { agencyId = ag.id }, label = { Text(ag.name, maxLines = 1) })
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DeviceType.entries.forEach { dt ->
                            FilterChip(selected = deviceType == dt, onClick = { deviceType = dt }, label = { Text(dt.labelEs, maxLines = 1) })
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MonitoringProtocol.entries.forEach { p ->
                            FilterChip(selected = protocol == p, onClick = { protocol = p }, label = { Text(p.labelEs, maxLines = 1) })
                        }
                    }
                    OutlinedTextField(value = hostname, onValueChange = { hostname = it }, label = { Text("Hostname (ej. SW-MATRIZ-01)") }, singleLine = true)
                    OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("Dirección IP de gestión") }, singleLine = true)
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") }, singleLine = true)
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo") }, singleLine = true)
                    OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Número de serie") }, singleLine = true)
                    OutlinedTextField(value = firmware, onValueChange = { firmware = it }, label = { Text("Versión de firmware") }, singleLine = true)
                    OutlinedTextField(value = secretRef, onValueChange = { secretRef = it }, label = { Text("ID Referencia Secreto en Servidor") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ag = state.agencies.firstOrNull { it.id == agencyId }
                        if (hostname.isNotBlank()) {
                            actions.saveDevice(
                                NetworkDevice(
                                    agencyId = agencyId,
                                    agencyName = ag?.name ?: "MATRIZ",
                                    deviceType = deviceType.name,
                                    hostname = hostname,
                                    ipAddress = ip,
                                    brand = brand,
                                    model = model,
                                    serialNumber = serial,
                                    firmwareVersion = firmware,
                                    protocol = protocol.name,
                                    secretRefId = secretRef
                                )
                            )
                        }
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun ThresholdsScreen(
    state: NocWorkspaceState,
    actions: NocActions,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val th = state.thresholds
    val canEdit = state.effectiveRole.canEditInfra

    var pingInterval by remember(th) { mutableStateOf(th.pingIntervalSec.toString()) }
    var pingTimeout by remember(th) { mutableStateOf(th.pingTimeoutSec.toString()) }
    var failDown by remember(th) { mutableStateOf(th.failuresForDown.toString()) }
    var okRecover by remember(th) { mutableStateOf(th.successesForRecovery.toString()) }
    var cpu by remember(th) { mutableStateOf(th.cpuThresholdPct.toString()) }
    var ram by remember(th) { mutableStateOf(th.ramThresholdPct.toString()) }
    var temp by remember(th) { mutableStateOf(th.tempThresholdC.toString()) }
    var loss by remember(th) { mutableStateOf(th.packetLossThresholdPct.toString()) }
    var latency by remember(th) { mutableStateOf(th.latencyThresholdMs.toString()) }
    var backendUrl by remember(th) { mutableStateOf(th.backendBaseUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("thresholds_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
            Text("Configuración de Umbrales", style = MaterialTheme.typography.titleLarge)
        }

        OutlinedTextField(value = pingInterval, onValueChange = { pingInterval = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Intervalo comprobación ICMP (seg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pingTimeout, onValueChange = { pingTimeout = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Tiempo máximo respuesta (seg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = failDown, onValueChange = { failDown = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Fallos consecutivos para caída") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = okRecover, onValueChange = { okRecover = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Éxitos consecutivos para recuperación") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cpu, onValueChange = { cpu = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Umbral CPU (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ram, onValueChange = { ram = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Umbral RAM (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = temp, onValueChange = { temp = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Umbral Temperatura (°C)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = loss, onValueChange = { loss = it }, enabled = canEdit, label = { Text("Umbral Pérdida Paquetes (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = latency, onValueChange = { latency = it.filter(Char::isDigit) }, enabled = canEdit, label = { Text("Umbral Latencia excesiva (ms)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = backendUrl, onValueChange = { backendUrl = it }, enabled = canEdit, label = { Text("URL HTTPS Backend Cloud Run") }, modifier = Modifier.fillMaxWidth())

        if (canEdit) {
            Button(
                onClick = {
                    actions.saveThresholds(
                        th.copy(
                            pingIntervalSec = pingInterval.toLongOrNull() ?: 30L,
                            pingTimeoutSec = pingTimeout.toLongOrNull() ?: 3L,
                            failuresForDown = failDown.toLongOrNull() ?: 3L,
                            successesForRecovery = okRecover.toLongOrNull() ?: 2L,
                            cpuThresholdPct = cpu.toLongOrNull() ?: 85L,
                            ramThresholdPct = ram.toLongOrNull() ?: 85L,
                            tempThresholdC = temp.toLongOrNull() ?: 70L,
                            packetLossThresholdPct = loss.toDoubleOrNull() ?: 5.0,
                            latencyThresholdMs = latency.toLongOrNull() ?: 120L,
                            backendBaseUrl = backendUrl.trim()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_thresholds_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
            ) {
                Text("Guardar Parámetros")
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
fun UsersAdminScreen(
    state: NocWorkspaceState,
    actions: NocActions,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val canEdit = state.effectiveRole.canEditInfra
    var showAddUser by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = SoftSurface,
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showAddUser = true },
                    containerColor = CotocollaoOrange,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar usuario")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("users_admin_screen"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                    Text("Usuarios y Auditoría", style = MaterialTheme.typography.titleLarge)
                }
            }

            item {
                Text("Personal Autorizado (${state.teamMembers.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(state.teamMembers, key = { it.id }) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(member.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(member.email, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${member.roleEnum.displayNameEs} • ${member.department}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CotocollaoOrange
                        )
                    }
                }
            }

            item {
                Text("Registro de Auditoría Inmutable (${state.auditLogs.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(state.auditLogs.take(25), key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${log.action} • ${log.target}", style = MaterialTheme.typography.labelLarge)
                        Text(log.details, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${log.actorEmail} (${log.actorRole}) • ${AvailabilityCalculator.formatTimestamp(log.timestampEpochMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddUser) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Infraestructura TI") }
        var role by remember { mutableStateOf(UserRole.TECHNICIAN) }

        AlertDialog(
            onDismissRequest = { showAddUser = false },
            title = { Text("Autorizar Usuario") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre completo") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo institucional") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Área / Departamento") }, singleLine = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.entries.forEach { r ->
                            FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r.displayNameEs.substringBefore(" /"), maxLines = 1) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.contains("@")) {
                            actions.saveTeamMember(
                                TeamMember(fullName = name, email = email, department = dept, role = role.name, active = true)
                            )
                        }
                        showAddUser = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange)
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddUser = false }) { Text("Cancelar") } }
        )
    }
}
