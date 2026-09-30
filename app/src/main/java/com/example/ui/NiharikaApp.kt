package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.EarningRepository
import com.example.data.repository.WalletRepository
import com.example.ui.components.NavDestination
import com.example.ui.components.NiharikaBottomNav
import com.example.ui.components.NiharikaTopBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.earning.EarningScreen
import com.example.ui.screens.games.GamesScreen
import com.example.ui.screens.games.carrom.CarromGameScreen
import com.example.ui.screens.games.dragontiger.DragonTigerScreen
import com.example.ui.screens.games.ludo.LudoGameScreen
import com.example.ui.screens.games.spin.SpinWheelScreen
import com.example.ui.screens.games.teenpatti.TeenPattiScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.GamingDarkBackground

sealed class Screen {
    object Main : Screen()
    object SpinGame : Screen()
    object LudoGame : Screen()
    object CarromGame : Screen()
    object TeenPattiGame : Screen()
    object DragonTigerGame : Screen()
    object Notifications : Screen()
    object AdminControlRoom : Screen()
}

@Composable
fun NiharikaApp() {
    val authRepo = remember { AuthRepository.getInstance() }
    val walletRepo = remember { WalletRepository.getInstance() }
    val earningRepo = remember { EarningRepository.getInstance() }

    val authState by authRepo.authState.collectAsState()
    val currentUser by authRepo.currentUser.collectAsState()
    val wallet by walletRepo.wallet.collectAsState()
    val notifications by earningRepo.notifications.collectAsState()

    var currentNavDestination by remember { mutableStateOf(NavDestination.HOME) }
    var currentSubScreen by remember { mutableStateOf<Screen>(Screen.Main) }

    // If unauthenticated, show LoginScreen
    if (authState != AuthState.AUTHENTICATED || currentUser == null) {
        LoginScreen(
            onLoginSuccess = { user ->
                walletRepo.syncUserWallet(user.uid)
                currentNavDestination = NavDestination.HOME
                currentSubScreen = Screen.Main
            }
        )
        return
    }

    val user = currentUser!!

    // Sync admin list if user has admin privileges
    remember(user.isAdmin) {
        if (user.isAdmin) {
            walletRepo.syncAllWithdrawalsForAdmin()
        }
    }

    // Sub-screens with BackHandler
    when (val screen = currentSubScreen) {
        Screen.SpinGame -> {
            BackHandler { currentSubScreen = Screen.Main }
            SpinWheelScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.LudoGame -> {
            BackHandler { currentSubScreen = Screen.Main }
            LudoGameScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.CarromGame -> {
            BackHandler { currentSubScreen = Screen.Main }
            CarromGameScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.TeenPattiGame -> {
            BackHandler { currentSubScreen = Screen.Main }
            TeenPattiScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.DragonTigerGame -> {
            BackHandler { currentSubScreen = Screen.Main }
            DragonTigerScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.Notifications -> {
            BackHandler { currentSubScreen = Screen.Main }
            NotificationsScreen(
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.AdminControlRoom -> {
            BackHandler { currentSubScreen = Screen.Main }
            AdminDashboardScreen(
                userProfile = user,
                onBack = { currentSubScreen = Screen.Main },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            )
        }
        Screen.Main -> {
            Scaffold(
                containerColor = GamingDarkBackground,
                topBar = {
                    NiharikaTopBar(
                        walletBalance = wallet.balance,
                        unreadNotifications = notifications.size,
                        onWalletClick = { currentNavDestination = NavDestination.WALLET },
                        onNotificationsClick = { currentSubScreen = Screen.Notifications },
                        modifier = Modifier.statusBarsPadding()
                    )
                },
                bottomBar = {
                    NiharikaBottomNav(
                        currentDestination = currentNavDestination,
                        onDestinationSelected = { currentNavDestination = it },
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(GamingDarkBackground)
                ) {
                    when (currentNavDestination) {
                        NavDestination.HOME -> {
                            HomeScreen(
                                userProfile = user,
                                onNavigateToGame = { gameId ->
                                    currentSubScreen = when (gameId) {
                                        "spin" -> Screen.SpinGame
                                        "ludo" -> Screen.LudoGame
                                        "carrom" -> Screen.CarromGame
                                        "teenpatti" -> Screen.TeenPattiGame
                                        "dragontiger" -> Screen.DragonTigerGame
                                        else -> Screen.SpinGame
                                    }
                                },
                                onNavigateToWallet = { currentNavDestination = NavDestination.WALLET },
                                onNavigateToWithdrawal = { currentNavDestination = NavDestination.WALLET },
                                onNavigateToEarning = { currentNavDestination = NavDestination.EARNING }
                            )
                        }
                        NavDestination.GAMES -> {
                            GamesScreen(
                                onSelectGame = { gameId ->
                                    currentSubScreen = when (gameId) {
                                        "spin" -> Screen.SpinGame
                                        "ludo" -> Screen.LudoGame
                                        "carrom" -> Screen.CarromGame
                                        "teenpatti" -> Screen.TeenPattiGame
                                        "dragontiger" -> Screen.DragonTigerGame
                                        else -> Screen.SpinGame
                                    }
                                }
                            )
                        }
                        NavDestination.EARNING -> {
                            EarningScreen(
                                userProfile = user,
                                onNavigateToSpin = { currentSubScreen = Screen.SpinGame }
                            )
                        }
                        NavDestination.WALLET -> {
                            WalletScreen(
                                userProfile = user
                            )
                        }
                        NavDestination.PROFILE -> {
                            ProfileScreen(
                                userProfile = user,
                                onNavigateToNotifications = { currentSubScreen = Screen.Notifications },
                                onNavigateToAdmin = { currentSubScreen = Screen.AdminControlRoom },
                                onLogout = {
                                    authRepo.logout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
