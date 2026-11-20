package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.data.model.Agency
import com.example.data.model.TrafficLightStatus
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.CotocollaoOrangeContainer
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGrayBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowBg
import com.example.util.AvailabilityCalculator

fun TrafficLightStatus.foregroundColor(): Color = when (this) {
    TrafficLightStatus.GREEN -> StatusGreen
    TrafficLightStatus.YELLOW -> StatusYellow
    TrafficLightStatus.RED -> StatusRed
    TrafficLightStatus.GRAY -> StatusGray
}

fun TrafficLightStatus.backgroundColor(): Color = when (this) {
    TrafficLightStatus.GREEN -> StatusGreenBg
    TrafficLightStatus.YELLOW -> StatusYellowBg
    TrafficLightStatus.RED -> StatusRedBg
    TrafficLightStatus.GRAY -> StatusGrayBg
}

fun TrafficLightStatus.statusIcon(): ImageVector = when (this) {
    TrafficLightStatus.GREEN -> Icons.Filled.CheckCircle
    TrafficLightStatus.YELLOW -> Icons.Filled.Warning
    TrafficLightStatus.RED -> Icons.Filled.Error
    TrafficLightStatus.GRAY -> Icons.AutoMirrored.Filled.HelpOutline
}

@Composable
fun TrafficLightBadge(
    status: TrafficLightStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = status.backgroundColor(),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 7.dp else 10.dp,
                vertical = if (compact) 2.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 7.dp else 8.dp)
                    .clip(CircleShape)
                    .background(status.foregroundColor())
            )
            Text(
                text = status.labelEs,
                style = MaterialTheme.typography.labelSmall,
                color = status.foregroundColor(),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OnlineStatusTag(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = StatusGreenBg,
        border = BorderStroke(1.dp, StatusGreen),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(StatusGreen)
            )
            Text(
                text = "EN LÍNEA",
                style = MaterialTheme.typography.labelSmall,
                color = StatusGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CompactLayerDot(
    label: String,
    status: TrafficLightStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(status.foregroundColor())
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DarkSlate
        )
    }
}

@Composable
fun AgencyCompactCard(
    agency: Agency,
    canViewIp: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("agency_card_${agency.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, OutlineSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Apartment,
                        contentDescription = agency.name,
                        tint = CotocollaoOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkSlate)) {
                                append("${agency.code} • ${agency.name}")
                            }
                            if (agency.cityZone.isNotBlank()) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = SecondarySlate)) {
                                    append(" · ${agency.cityZone}")
                                }
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                TrafficLightBadge(status = agency.overallLight, compact = true)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WAN: ${AvailabilityCalculator.maskIpIfUnauthorized(agency.publicIp, canViewIp)}" +
                        if (agency.configured && agency.contractedBandwidthMbps > 0) " • ${agency.contractedBandwidthMbps}M" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactLayerDot("WAN", agency.linkLight)
                    CompactLayerDot("SW", agency.switchLight)
                    CompactLayerDot("FW", agency.firewallLight)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latencia: ${agency.latencyMs} ms  •  Pérdida: ${agency.packetLossPct}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondarySlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (agency.configured) "Disp: ${agency.availabilityPct}%" else "Sin telemetría",
                    style = MaterialTheme.typography.labelMedium,
                    color = agency.overallLight.foregroundColor(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardMod = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Card(
        modifier = cardMod,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, OutlineSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondarySlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = DarkSlate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyTelemetryNotice(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CotocollaoOrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CotocollaoOrange
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SecondarySlate
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkSlate
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondarySlate
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
