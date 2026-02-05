package com.quotecards.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quotecards.ui.screens.addquote.AddQuoteScreen
import com.quotecards.ui.screens.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddQuote : Screen("add_quote")
}

@Composable
fun QuoteNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                onAddQuoteClick = {
                    navController.navigate(Screen.AddQuote.route)
                }
            )
        }

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
    }
}
