package com.example.polytalkai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polytalkai.screen.*
import com.example.polytalkai.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyTalkTheme {
                MainAppContainer()
            }
        }
    }
}

// Utility Glassmorphic Modifier
fun Modifier.glassmorphic(
    backgroundColor: Color = GlassBgColor,
    borderColor: Color = GlassBorderColor,
    cornerRadius: Int = 16
) = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(backgroundColor)
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius.dp))

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppContainer() {
    var currentScreen by remember { mutableStateOf("splash") }
    var userEmail by remember { mutableStateOf("email@example.com") }
    var userName by remember { mutableStateOf("User") }
    var isLoggedIn by remember { mutableStateOf(false) }

    // History and Saved states
    val historyItems = remember {
        mutableStateListOf(
            HistoryItem("Where is the station?", "স্টেশন কোথায়?", "EN", "BN"),
            HistoryItem("Hello, how are you?", "Hola, ¿cómo estás?", "EN", "ES"),
            HistoryItem("Good morning", "शुभ प्रभात", "EN", "HI")
        )
    }
    val savedItems = remember {
        mutableStateListOf<HistoryItem>()
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == "splash") {
            delay(3000)
            currentScreen = if (isLoggedIn) "dashboard" else "auth"
        }
    }

    // Intercept hardware back button to prevent accidental app closure
    BackHandler(enabled = currentScreen != "dashboard" && currentScreen != "splash" && currentScreen != "auth") {
        if (currentScreen == "forgot") {
            currentScreen = "auth"
        } else {
            currentScreen = "dashboard"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(500, easing = EaseOutCubic)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(500, easing = EaseOutCubic)) +
                        slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(500, easing = EaseOutCubic)) togetherWith
                        fadeOut(animationSpec = tween(300, easing = EaseOutCubic)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(300, easing = EaseOutCubic))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                "splash" -> SplashScreen()
                "auth" -> AuthScreen(
                    onLoginSuccess = { email, name ->
                        userEmail = email
                        userName = name
                        isLoggedIn = true
                        currentScreen = "dashboard"
                    },
                    onForgotPasswordNavigate = { currentScreen = "forgot" }
                )
                "forgot" -> ForgotPasswordScreen(
                    onBack = { currentScreen = "auth" }
                )
                "dashboard" -> DashboardScreen(
                    userName = userName,
                    onNavigate = { currentScreen = it }
                )
                "text-screen" -> TextTranslationScreen(
                    onBack = { currentScreen = "dashboard" },
                    onTranslate = { src, trans, from, to ->
                        historyItems.add(0, HistoryItem(src, trans, from, to))
                    }
                )
                "voice-screen" -> VoiceTranslationScreen(
                    onBack = { currentScreen = "dashboard" }
                )
                "conv-screen" -> ConversationScreen(
                    onBack = { currentScreen = "dashboard" }
                )
                "cam-screen" -> CameraOcrScreen(
                    onBack = { currentScreen = "dashboard" }
                )
                "history" -> HistoryScreen(
                    historyItems = historyItems,
                    savedItems = savedItems,
                    onBack = { currentScreen = "dashboard" }
                )
                "saved" -> SavedScreen(
                    savedItems = savedItems,
                    onBack = { currentScreen = "dashboard" }
                )
                "account" -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AccountScreen(
                        userName = userName,
                        userEmail = userEmail,
                        onLogout = {
                            isLoggedIn = false
                            currentScreen = "auth"
                        },
                        onDeleteAccount = {
                            isLoggedIn = false
                            currentScreen = "auth"
                            android.widget.Toast.makeText(context, "Account deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onBack = { currentScreen = "dashboard" }
                    )
                }
            }
        }

        // Show Bottom Navigation only for core dashboard and secondary tabs
        val showBottomNav = currentScreen in listOf("dashboard", "history", "saved", "account")
        if (showBottomNav) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onTabSelected = { currentScreen = it }
                )
            }
        }
    }
}