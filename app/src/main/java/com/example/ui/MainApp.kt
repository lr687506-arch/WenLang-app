package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*

enum class AppTab {
    READING,
    PUBLISH,
    QUIZ,
    VOCABULARY,
    PROFILE
}

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    langViewModel: LangViewModel = viewModel()
) {
    val isDarkTheme by langViewModel.isDarkTheme.collectAsState()

    com.example.ui.theme.MyApplicationTheme(darkTheme = isDarkTheme) {
        val profileState by langViewModel.userProfile.collectAsState()
        var currentTab by remember { mutableStateOf(AppTab.READING) }

        val orangePrimary = Color(0xFFFF6734)
        val lightBg = MaterialTheme.colorScheme.background

        val profile = profileState

        if (profile == null || !profile.isOnboarded) {
            // Show step-by-step Onboarding configuration flow first
            OnboardingScreen(
                viewModel = langViewModel,
                modifier = modifier
            )
        } else {
            // Main onboarded app container
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = lightBg,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .height(80.dp)
                            .testTag("bottom_navigation")
                    ) {
                        // Reading tab
                        NavigationBarItem(
                            selected = currentTab == AppTab.READING,
                            onClick = { currentTab = AppTab.READING },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                            label = { Text("Leitura", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = orangePrimary,
                                selectedTextColor = orangePrimary,
                                indicatorColor = orangePrimary.copy(alpha = 0.1f),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("tab_reading")
                        )

                        // Quiz / Test tab
                        NavigationBarItem(
                            selected = currentTab == AppTab.QUIZ,
                            onClick = { currentTab = AppTab.QUIZ },
                            icon = { Icon(Icons.Default.School, contentDescription = null) },
                            label = { Text("Testes", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = orangePrimary,
                                selectedTextColor = orangePrimary,
                                indicatorColor = orangePrimary.copy(alpha = 0.1f),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("tab_quiz")
                        )

                        // Vocabulary tab
                        NavigationBarItem(
                            selected = currentTab == AppTab.VOCABULARY,
                            onClick = { currentTab = AppTab.VOCABULARY },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                            label = { Text("Vocabulário", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = orangePrimary,
                                selectedTextColor = orangePrimary,
                                indicatorColor = orangePrimary.copy(alpha = 0.1f),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("tab_vocabulary")
                        )

                        // Profile tab
                        NavigationBarItem(
                            selected = currentTab == AppTab.PROFILE,
                            onClick = { currentTab = AppTab.PROFILE },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Perfil", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = orangePrimary,
                                selectedTextColor = orangePrimary,
                                indicatorColor = orangePrimary.copy(alpha = 0.1f),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("tab_profile")
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        AppTab.READING -> {
                            ReadingScreen(
                                viewModel = langViewModel,
                                onNavigateToPublish = { currentTab = AppTab.PUBLISH },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.PUBLISH -> {
                            PublishScreen(
                                viewModel = langViewModel,
                                onNavigateToQuiz = { currentTab = AppTab.QUIZ },
                                onBack = { currentTab = AppTab.READING },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.QUIZ -> {
                            QuizScreen(
                                viewModel = langViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.VOCABULARY -> {
                            SavedScreen(
                                viewModel = langViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.PROFILE -> {
                            ProfileScreen(
                                viewModel = langViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
