package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.alarm.AlarmSoundPlayer
import com.example.alarm.CharvikAlarmScheduler
import com.example.data.local.CharvikDatabase
import com.example.data.local.CharvikRepository
import com.example.ui.screens.AddAlarmSheet
import com.example.ui.screens.CareTimersScreen
import com.example.ui.screens.DigestionSafetyScreen
import com.example.ui.screens.LogDigestionSheet
import com.example.ui.screens.LogFeedSheet
import com.example.ui.screens.LogWeightSheet
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.VaccinesAlarmsScreen
import com.example.ui.theme.CharvikTheme
import com.example.ui.viewmodel.CharvikViewModel
import com.example.ui.viewmodel.UiState

private const val ROUTE_TODAY = "today"
private const val ROUTE_TIMERS = "timers"
private const val ROUTE_SAFETY = "safety"
private const val ROUTE_VACCINES = "vaccines"

private data class NavTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val charvikTabs = listOf(
  NavTab(ROUTE_TODAY, "Today", Icons.Filled.Home, Icons.Outlined.Home),
  NavTab(ROUTE_TIMERS, "Timers", Icons.Filled.Timer, Icons.Outlined.Timer),
  NavTab(ROUTE_SAFETY, "Safety", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety),
  NavTab(ROUTE_VACCINES, "Vaccines", Icons.Filled.Vaccines, Icons.Outlined.Vaccines),
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    CharvikAlarmScheduler.ensureNotificationChannels(this)
    setContent {
      val prefs = remember { getSharedPreferences("charvik_prefs", Context.MODE_PRIVATE) }
      val systemDark = isSystemInDarkTheme()
      var darkOverride by remember {
        mutableStateOf(
          if (prefs.contains("night_mode")) prefs.getBoolean("night_mode", false) else null
        )
      }
      val isDark = darkOverride ?: systemDark
      CharvikTheme(darkTheme = isDark) {
        CharvikApp(
          isDarkTheme = isDark,
          onToggleDarkTheme = {
            val next = !isDark
            darkOverride = next
            prefs.edit().putBoolean("night_mode", next).apply()
          },
        )
      }
    }
  }
}

@Composable
private fun CharvikApp(
  isDarkTheme: Boolean,
  onToggleDarkTheme: () -> Unit,
) {
  val context = LocalContext.current

  val viewModel: CharvikViewModel = viewModel(
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY])
        val db = CharvikDatabase.getInstance(app)
        CharvikViewModel(CharvikRepository(db.charvikDao()))
      }
    },
  )

  val notificationLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = {},
  )

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
      if (!granted) {
        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
  val nowMillis by viewModel.nowMillis.collectAsStateWithLifecycle()
  val refluxTimer by viewModel.refluxTimer.collectAsStateWithLifecycle()
  val pumpTimer by viewModel.pumpTimer.collectAsStateWithLifecycle()
  val breathingCheck by viewModel.breathingCheck.collectAsStateWithLifecycle()
  val actionBanner by viewModel.actionBanner.collectAsStateWithLifecycle()
  val isRinging by AlarmSoundPlayer.isRinging.collectAsStateWithLifecycle()
  val ringingTitle by AlarmSoundPlayer.ringingTitle.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(actionBanner) {
    val msg = actionBanner
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearBanner()
    }
  }

  var showFeedSheet by remember { mutableStateOf(false) }
  var showDigestionSheet by remember { mutableStateOf(false) }
  var showWeightSheet by remember { mutableStateOf(false) }
  var showAlarmSheet by remember { mutableStateOf(false) }

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_TODAY

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useRail) {
          NavigationBar(windowInsets = WindowInsets.navigationBars) {
            charvikTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationBarItem(
                selected = selected,
                onClick = {
                  navController.navigate(tab.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                  }
                },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("nav_${tab.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (useRail) {
          NavigationRail(modifier = Modifier.fillMaxHeight()) {
            Spacer(modifier = Modifier.height(12.dp))
            charvikTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationRailItem(
                selected = selected,
                onClick = {
                  navController.navigate(tab.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                  }
                },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("rail_${tab.route}"),
              )
            }
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.TopCenter,
        ) {
          Box(
            modifier = Modifier
              .widthIn(max = 760.dp)
              .fillMaxSize(),
          ) {
            when (val state = uiState) {
              UiState.Loading -> {
                Column(
                  modifier = Modifier.align(Alignment.Center),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  CircularProgressIndicator()
                  Spacer(modifier = Modifier.height(12.dp))
                  Text("Loading Charvik's care profile...", style = MaterialTheme.typography.bodyMedium)
                }
              }

              is UiState.Error -> {
                Column(
                  modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text("Unable to load data", style = MaterialTheme.typography.titleLarge)
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(state.message, style = MaterialTheme.typography.bodyMedium)
                }
              }

              is UiState.Success -> {
                val data = state.data
                NavHost(
                  navController = navController,
                  startDestination = ROUTE_TODAY,
                  modifier = Modifier.fillMaxSize(),
                ) {
                  composable(ROUTE_TODAY) {
                    TodayScreen(
                      data = data,
                      nowMillis = nowMillis,
                      refluxTimer = refluxTimer,
                      isAlarmRinging = isRinging,
                      ringingTitle = ringingTitle,
                      onStopRinging = { viewModel.stopAlarmSound() },
                      onSnoozeRinging = { viewModel.snoozeAlarm(context, ringingTitle) },
                      isDarkTheme = isDarkTheme,
                      onToggleDarkTheme = onToggleDarkTheme,
                      onOpenFeedSheet = { showFeedSheet = true },
                      onOpenDigestionSheet = { showDigestionSheet = true },
                      onOpenWeightSheet = { showWeightSheet = true },
                      onStartRefluxHold = { viewModel.startRefluxTimer(context, 25) },
                      onStopRefluxHold = { viewModel.stopRefluxTimer(context) },
                      onToggleRoutine = { key, completed ->
                        viewModel.toggleRoutineItem(key, completed, data.careEvents)
                      },
                      onNavigateToTimers = {
                        navController.navigate(ROUTE_TIMERS) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      },
                    )
                  }

                  composable(ROUTE_TIMERS) {
                    CareTimersScreen(
                      data = data,
                      refluxTimer = refluxTimer,
                      pumpTimer = pumpTimer,
                      isAlarmRinging = isRinging,
                      ringingTitle = ringingTitle,
                      onStopRinging = { viewModel.stopAlarmSound() },
                      onSnoozeRinging = { viewModel.snoozeAlarm(context, ringingTitle) },
                      onStartReflux = { mins -> viewModel.startRefluxTimer(context, mins) },
                      onStopReflux = { viewModel.stopRefluxTimer(context) },
                      onStartPump = { mins -> viewModel.startPumpTimer(context, mins) },
                      onStopPump = { viewModel.stopPumpTimer() },
                      onSavePumpSession = { mins, ml, sched ->
                        viewModel.savePumpSession(context, mins, ml, sched)
                      },
                      onLogNasalSuction = { viewModel.logNasalSuction(data.suctionsToday) },
                    )
                  }

                  composable(ROUTE_SAFETY) {
                    DigestionSafetyScreen(
                      data = data,
                      breathingCheck = breathingCheck,
                      isAlarmRinging = isRinging,
                      ringingTitle = ringingTitle,
                      onStopRinging = { viewModel.stopAlarmSound() },
                      onSnoozeRinging = { viewModel.snoozeAlarm(context, ringingTitle) },
                      onOpenDigestionSheet = { showDigestionSheet = true },
                      onOpenWeightSheet = { showWeightSheet = true },
                      onStartBreathingCheck = { viewModel.startBreathingCounter(context) },
                      onTapBreath = { viewModel.recordBreathTap() },
                      onUpdateBreathingFlags = { r, f, g -> viewModel.updateBreathingFlags(r, f, g) },
                      onResetBreathing = { viewModel.resetBreathingCounter() },
                      onSaveVitals = { tempF, bpm, redFlag ->
                        viewModel.logVitalsCheck(tempF, bpm, redFlag, null)
                      },
                    )
                  }

                  composable(ROUTE_VACCINES) {
                    VaccinesAlarmsScreen(
                      data = data,
                      isAlarmRinging = isRinging,
                      ringingTitle = ringingTitle,
                      onStopRinging = { viewModel.stopAlarmSound() },
                      onSnoozeRinging = { viewModel.snoozeAlarm(context, ringingTitle) },
                      onTestAlarmSound = { viewModel.testAlarmSound(context) },
                      onExportCalendarIcs = {
                        viewModel.exportCalendarIcs(context, data.vaccines, data.alarms)
                      },
                      onOpenAddAlarmSheet = { showAlarmSheet = true },
                      onToggleAlarm = { alarm, enabled -> viewModel.toggleAlarm(context, alarm, enabled) },
                      onDeleteAlarm = { alarmId -> viewModel.deleteAlarm(context, alarmId) },
                      onUpdateVaccine = { vax, done, dateIso, note, rem ->
                        viewModel.markVaccineStatus(context, vax, done, dateIso, note, rem)
                      },
                    )
                  }
                }

                if (showFeedSheet) {
                  LogFeedSheet(
                    targets = data.feedTargets,
                    onDismiss = { showFeedSheet = false },
                    onSave = { feedType, breastMin, bottleMl, burped, uprightMin, saline, note, autoReflux, schedAlarm ->
                      viewModel.logFeed(
                        context = context,
                        feedType = feedType,
                        breastMinutes = breastMin,
                        bottleMl = bottleMl,
                        burpedMidFeed = burped,
                        uprightHoldMinutes = uprightMin,
                        salineBeforeFeed = saline,
                        note = note,
                        autoStartRefluxHold = autoReflux,
                        scheduleNextFeedAlarm = schedAlarm,
                        intervalHours = data.profile.feedIntervalHours,
                      )
                      showFeedSheet = false
                    },
                  )
                }

                if (showDigestionSheet) {
                  LogDigestionSheet(
                    onDismiss = { showDigestionSheet = false },
                    onSave = { wet, stool, stoolColor, reflux, hiccups, gas, note ->
                      viewModel.logDigestion(wet, stool, stoolColor, reflux, hiccups, gas, note)
                      showDigestionSheet = false
                    },
                  )
                }

                if (showWeightSheet) {
                  LogWeightSheet(
                    currentWeightKg = data.profile.lastWeightKg,
                    onDismiss = { showWeightSheet = false },
                    onSave = { weightKg, note ->
                      viewModel.logWeight(weightKg, note)
                      showWeightSheet = false
                    },
                  )
                }

                if (showAlarmSheet) {
                  AddAlarmSheet(
                    onDismiss = { showAlarmSheet = false },
                    onSave = { type, label, hour, minute, repeatDaily ->
                      viewModel.createAlarm(context, type, label, hour, minute, repeatDaily)
                      showAlarmSheet = false
                    },
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
