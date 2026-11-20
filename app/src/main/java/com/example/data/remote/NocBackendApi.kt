package com.example.data.remote

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class RemoteDashboardDto(
    val totalAgencies: Int = 7,
    val operationalAgencies: Int = 0,
    val agenciesWithIncidents: Int = 0,
    val activeInternetLinks: Int = 0,
    val downInternetLinks: Int = 0,
    val operationalSwitches: Int = 0,
    val failedSwitches: Int = 0,
    val operationalFirewalls: Int = 0,
    val failedFirewalls: Int = 0,
    val pendingCriticalIncidents: Int = 0,
    val overallAvailabilityPct: Double = 0.0,
    val telemetryConnected: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RemoteAgencyDto(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val cityZone: String = "",
    val publicIp: String = "",
    val gatewayIp: String = "",
    val switchIp: String = "",
    val firewallIp: String = "",
    val ispProvider: String = "",
    val contractedBandwidthMbps: Long = 0L,
    val status: String = "GRAY",
    val linkStatus: String = "GRAY",
    val switchStatus: String = "GRAY",
    val firewallStatus: String = "GRAY",
    val latencyMs: Long = 0L,
    val packetLossPct: Double = 0.0,
    val availabilityPct: Double = 0.0,
    val downtimeSeconds: Long = 0L,
    val noTelemetrySeconds: Long = 0L,
    val lastCheckEpochMs: Long = 0L,
    val lastConnectionEpochMs: Long = 0L,
    val configured: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RemoteAgenciesResponse(
    val agencies: List<RemoteAgencyDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteDeviceDto(
    val id: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val deviceType: String = "SWITCH_L3",
    val hostname: String = "",
    val ipAddress: String = "",
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val firmwareVersion: String = "",
    val protocol: String = "SNMPV3",
    val status: String = "GRAY",
    val cpuPct: Long = 0L,
    val ramPct: Long = 0L,
    val tempCelsius: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val activeInterfaces: Long = 0L,
    val totalInterfaces: Long = 0L,
    val txErrors: Long = 0L,
    val droppedPackets: Long = 0L,
    val vpnTunnelsUp: Long = 0L,
    val vpnTunnelsTotal: Long = 0L,
    val lastCheckEpochMs: Long = 0L
)

@JsonClass(generateAdapter = true)
data class RemoteDevicesResponse(
    val devices: List<RemoteDeviceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteAvailabilityReportDto(
    val generatedAtEpochMs: Long = 0L,
    val mttrSeconds: Long = 0L,
    val mtbfSeconds: Long = 0L
)

interface NocBackendApi {
    @GET("/api/dashboard")
    suspend fun getDashboard(@Header("Authorization") bearerToken: String): RemoteDashboardDto

    @GET("/api/agencies")
    suspend fun getAgencies(@Header("Authorization") bearerToken: String): RemoteAgenciesResponse

    @GET("/api/devices")
    suspend fun getDevices(@Header("Authorization") bearerToken: String): RemoteDevicesResponse

    @GET("/api/reports/availability")
    suspend fun getAvailabilityReport(@Header("Authorization") bearerToken: String): RemoteAvailabilityReportDto

    companion object {
        fun create(baseUrl: String): NocBackendApi {
            val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            require(normalized.startsWith("https://")) {
                "La URL del servidor de monitoreo debe usar HTTPS cifrado."
            }
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            return Retrofit.Builder()
                .baseUrl(normalized)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NocBackendApi::class.java)
        }
    }
}
