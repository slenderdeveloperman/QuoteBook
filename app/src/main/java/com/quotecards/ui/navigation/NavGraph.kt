package com.quotecards.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quotecards.data.preferences.AppPreferences
import com.quotecards.ui.screens.addquote.AddQuoteScreen
import com.quotecards.ui.screens.edit.EditQuoteScreen
import com.quotecards.ui.screens.home.HomeScreen
import com.quotecards.ui.screens.intro.IntroScreen
import com.quotecards.ui.screens.search.SearchScreen
import com.quotecards.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Intro : Screen("intro")
    data object Home : Screen("home")
    data object AddQuote : Screen("add_quote")
    data object EditQuote : Screen("edit_quote/{quoteId}") {
        fun createRoute(quoteId: Long) = "edit_quote/$quoteId"
    }
    data object Search : Screen("search")
}

@Composable
fun QuoteNavGraph(
    appPreferences: AppPreferences
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // State for navigating to a specific quote from search
    var navigateToQuoteId by rememberSaveable { mutableStateOf<Long?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            val hasSeenIntro by appPreferences.hasSeenIntro.collectAsState(initial = false)

            SplashScreen(
                onSplashComplete = {
                    if (hasSeenIntro) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Intro.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Intro/Onboarding Screen
        composable(
            route = Screen.Intro.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            IntroScreen(
                onGetStarted = {
                    scope.launch {
                        appPreferences.setHasSeenIntro(true)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(
                onAddQuoteClick = {
                    navController.navigate(Screen.AddQuote.route)
                },
                onEditQuoteClick = { quoteId ->
                    navController.navigate(Screen.EditQuote.createRoute(quoteId))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                navigateToQuoteId = navigateToQuoteId,
                onNavigatedToQuote = { navigateToQuoteId = null }
            )
        }

        // Add Quote Screen
        composable(
            route = Screen.AddQuote.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            AddQuoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Quote Screen
        composable(
            route = Screen.EditQuote.route,
            arguments = listOf(
                navArgument("quoteId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            EditQuoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Search Screen
        composable(
            route = Screen.Search.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQuoteSelected = { quoteId ->
                    navigateToQuoteId = quoteId
                    navController.popBackStack()
                }
            )
        }
    }
}
