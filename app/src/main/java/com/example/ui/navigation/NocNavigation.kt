package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.repository.NocRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.OnlineStatusTag
import com.example.ui.screens.AgenciesScreen
import com.example.ui.screens.AgencyDetailScreen
import com.example.ui.screens.AlertsAndEventsScreen
import com.example.ui.screens.ConfigHubScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceAdminScreen
import com.example.ui.screens.FirewallsScreen
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.InternetLinksScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SwitchesScreen
import com.example.ui.screens.ThresholdsScreen
import com.example.ui.screens.UsersAdminScreen
import com.example.ui.state.NocActions
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.CotocollaoOrangeContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftSurface
import com.example.ui.viewmodel.DemoViewModel
import com.example.ui.viewmodel.NocViewModel
import com.example.util.LocalNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object NocRoutes {
    const val DASHBOARD = "dashboard"
    const val AGENCIES = "agencies"
    const val ALERTS = "alerts"
    const val INCIDENTS = "incidents"
    const val CONFIG = "config"
    const val AGENCY_DETAIL = "agency_detail/{agencyId}"
    const val INTERNET_LINKS = "internet_links"
    const val SWITCHES = "switches"
    const val FIREWALLS = "firewalls"
    const val REPORTS = "reports"
    const val DEVICE_ADMIN = "device_admin"
    const val THRESHOLDS = "thresholds"
    const val USERS_ADMIN = "users_admin"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val bottomTabs = listOf(
    BottomNavTab(NocRoutes.DASHBOARD, "Inicio", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_inicio"),
    BottomNavTab(NocRoutes.AGENCIES, "Agencias", Icons.Filled.Apartment, Icons.Outlined.Apartment, "tab_agencias"),
    BottomNavTab(NocRoutes.ALERTS, "Alertas", Icons.Filled.Notifications, Icons.Outlined.Notifications, "tab_alertas"),
    BottomNavTab(NocRoutes.INCIDENTS, "Incidentes", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment, "tab_incidentes"),
    BottomNavTab(NocRoutes.CONFIG, "Config.", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_config")
)

@Composable
fun AppNavigation() {
    val firebaseAuth = remember { runCatching { Firebase.auth }.getOrNull() }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth?.currentUser) }
    var demoPreviewActive by remember { mutableStateOf(true) }

    DisposableEffect(firebaseAuth) {
        if (firebaseAuth == null) {
            onDispose { }
        } else {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                currentUser = auth.currentUser
                if (auth.currentUser != null) {
                    demoPreviewActive = false
                }
            }
            firebaseAuth.addAuthStateListener(listener)
            onDispose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    val user = currentUser
    if (user == null && !demoPreviewActive) {
        AuthScreen(
            onAuthSuccess = { currentUser = firebaseAuth?.currentUser },
            onEnterDemoPreview = { demoPreviewActive = true }
        )
    } else if (user == null) {
        val demoVm: DemoViewModel = viewModel()
        val state by demoVm.uiState.collectAsStateWithLifecycle()
        NocWorkspaceShell(
            state = state,
            actions = demoVm,
            isUnauthDemoSandbox = true,
            onSignOutOrExitDemo = { demoPreviewActive = false }
        )
    } else {
        val uid = user.uid
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val credentialManager = remember { CredentialManager.create(context) }
        key(uid) {
            val vm: NocViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        NocViewModel(
                            repository = NocRepository(db),
                            currentUserId = uid,
                            userEmail = user.email ?: ""
                        )
                    }
                }
            )
            val state by vm.uiState.collectAsStateWithLifecycle()
            NocWorkspaceShell(
                state = state,
                actions = vm,
                isUnauthDemoSandbox = false,
                onSignOutOrExitDemo = {
                    signOutUser(credentialManager, scope) {
                        currentUser = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NocWorkspaceShell(
    state: NocWorkspaceState,
    actions: NocActions,
    isUnauthDemoSandbox: Boolean,
    onSignOutOrExitDemo: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NocRoutes.DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val unackAlerts = state.alerts.count { !it.acknowledged }

    LaunchedEffect(state.statusBannerMessage, state.errorMessage) {
        val msg = state.errorMessage ?: state.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            actions.clearMessages()
        }
    }

    LaunchedEffect(state.latestTriggeredAlert?.id) {
        val alert = state.latestTriggeredAlert ?: return@LaunchedEffect
        LocalNotificationHelper.showAlertNotification(context, alert, state.isDemoMode)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = SoftSurface,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = PureWhite,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        title = {
                            Column {
                                Text(
                                    text = "COTOCOLLAO NETWORK MONITOR",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CotocollaoOrange
                                )
                                Text(
                                    text = "Sistema de Monitoreo de Infraestructura",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            OnlineStatusTag(modifier = Modifier.padding(end = 12.dp))
                        }
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(containerColor = PureWhite) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(NocRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.testTag(tab.testTag),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CotocollaoOrange,
                                    selectedTextColor = CotocollaoOrange,
                                    indicatorColor = CotocollaoOrangeContainer
                                ),
                                icon = {
                                    if (tab.route == NocRoutes.ALERTS && unackAlerts > 0) {
                                        BadgedBox(badge = { Badge { Text("$unackAlerts") } }) {
                                            Icon(
                                                if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = { Text(tab.label, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(containerColor = PureWhite) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(NocRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(SoftSurface),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NocRoutes.DASHBOARD,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(NocRoutes.DASHBOARD) {
                            DashboardScreen(
                                state = state,
                                actions = actions,
                                onOpenInternetLinks = { navController.navigate(NocRoutes.INTERNET_LINKS) },
                                onOpenSwitches = { navController.navigate(NocRoutes.SWITCHES) },
                                onOpenFirewalls = { navController.navigate(NocRoutes.FIREWALLS) },
                                onOpenReports = { navController.navigate(NocRoutes.REPORTS) },
                                onOpenAgency = { agId -> navController.navigate("agency_detail/$agId") }
                            )
                        }
                        composable(NocRoutes.AGENCIES) {
                            AgenciesScreen(
                                state = state,
                                onSelectAgency = { agId -> navController.navigate("agency_detail/$agId") }
                            )
                        }
                        composable(NocRoutes.ALERTS) {
                            AlertsAndEventsScreen(state = state, actions = actions)
                        }
                        composable(NocRoutes.INCIDENTS) {
                            IncidentsScreen(state = state, actions = actions)
                        }
                        composable(NocRoutes.CONFIG) {
                            ConfigHubScreen(
                                state = state,
                                actions = actions,
                                isUnauthDemoSandbox = isUnauthDemoSandbox,
                                onOpenReports = { navController.navigate(NocRoutes.REPORTS) },
                                onOpenDeviceAdmin = { navController.navigate(NocRoutes.DEVICE_ADMIN) },
                                onOpenThresholds = { navController.navigate(NocRoutes.THRESHOLDS) },
                                onOpenUsersAdmin = { navController.navigate(NocRoutes.USERS_ADMIN) },
                                onSignOutOrExitDemo = onSignOutOrExitDemo
                            )
                        }
                        composable(
                            route = NocRoutes.AGENCY_DETAIL,
                            arguments = listOf(navArgument("agencyId") { type = NavType.StringType })
                        ) { entry ->
                            val agId = entry.arguments?.getString("agencyId") ?: "matriz"
                            AgencyDetailScreen(
                                agencyId = agId,
                                state = state,
                                actions = actions,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NocRoutes.INTERNET_LINKS) {
                            InternetLinksScreen(state = state, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.SWITCHES) {
                            SwitchesScreen(state = state, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.FIREWALLS) {
                            FirewallsScreen(state = state, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.REPORTS) {
                            ReportsScreen(state = state, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.DEVICE_ADMIN) {
                            DeviceAdminScreen(state = state, actions = actions, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.THRESHOLDS) {
                            ThresholdsScreen(state = state, actions = actions, onBack = { navController.popBackStack() })
                        }
                        composable(NocRoutes.USERS_ADMIN) {
                            UsersAdminScreen(state = state, actions = actions, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
