package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.TrafficLightStatus
import com.example.ui.components.AgencyCompactCard
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.SecondarySlate

@Composable
fun AgenciesScreen(
    state: NocWorkspaceState,
    onSelectAgency: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<TrafficLightStatus?>(null) }

    val filtered = remember(state.agencies, search, filterStatus) {
        state.agencies.filter { agency ->
            val matchesQuery = search.isBlank() ||
                agency.name.contains(search, ignoreCase = true) ||
                agency.ispProvider.contains(search, ignoreCase = true) ||
                agency.cityZone.contains(search, ignoreCase = true)
            val matchesFilter = filterStatus == null || agency.overallLight == filterStatus
            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("agencies_list"),
        contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agencias Monitoreadas (${state.agencies.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "MATRIZ, CALDERÓN, SANGOLQUÍ, SUR, POMASQUI, SAN ANTONIO y CENTRO",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondarySlate
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("agencies_search_input"),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                    placeholder = { Text("Buscar agencia, zona o ISP...", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick = { filterStatus = null },
                        label = { Text("Todas (${state.agencies.size})", maxLines = 1) }
                    )
                    FilterChip(
                        selected = filterStatus == TrafficLightStatus.GREEN,
                        onClick = { filterStatus = TrafficLightStatus.GREEN },
                        label = { Text("Operativas", maxLines = 1) }
                    )
                    FilterChip(
                        selected = filterStatus == TrafficLightStatus.RED,
                        onClick = { filterStatus = TrafficLightStatus.RED },
                        label = { Text("Críticas", maxLines = 1) }
                    )
                    FilterChip(
                        selected = filterStatus == TrafficLightStatus.YELLOW,
                        onClick = { filterStatus = TrafficLightStatus.YELLOW },
                        label = { Text("Degrad.", maxLines = 1) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { agency ->
            AgencyCompactCard(
                agency = agency,
                canViewIp = state.effectiveRole.canViewIp,
                onClick = { onSelectAgency(agency.id) }
            )
        }
    }
}
