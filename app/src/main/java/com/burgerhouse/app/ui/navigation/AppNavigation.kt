package com.burgerhouse.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.burgerhouse.app.ui.screens.cart.CartScreen
import com.burgerhouse.app.ui.screens.checkout.CheckoutScreen
import com.burgerhouse.app.ui.screens.history.OrderHistoryScreen
import com.burgerhouse.app.ui.screens.home.HomeScreen
import com.burgerhouse.app.ui.screens.login.LoginScreen
import com.burgerhouse.app.ui.screens.loyalty.LoyaltyScreen
import com.burgerhouse.app.ui.screens.onboarding.OnboardingScreen
import com.burgerhouse.app.ui.screens.productdetail.ProductDetailScreen
import com.burgerhouse.app.ui.screens.profile.ProfileScreen
import com.burgerhouse.app.ui.screens.splash.SplashScreen
import com.burgerhouse.app.ui.screens.tracking.OrderTrackingScreen
import com.burgerhouse.app.viewmodel.AuthViewModel

/**
 * Grafo de navegação do app. Controla o fluxo:
 * Splash → Onboarding → Login → Home → (Produto/Carrinho/Checkout/Tracking)
 * e as abas principais (Histórico, Fidelidade, Perfil).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val onboardingDone by authViewModel.onboardingDone.collectAsState()
    val sessionActive by authViewModel.sessionActive.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomItem.routes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(bottom = if (showBottomBar) 88.dp else 0.dp)
        ) {
            // ---------- Splash ----------
            composable(Routes.SPLASH) {
                SplashScreen(
                    onTimeout = {
                        val next = when {
                            !onboardingDone -> Routes.ONBOARDING
                            !sessionActive -> Routes.LOGIN
                            else -> Routes.HOME
                        }
                        navController.navigate(next) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- Onboarding ----------
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        authViewModel.finishOnboarding()
                        val next = if (sessionActive) Routes.HOME else Routes.LOGIN
                        navController.navigate(next) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- Login / Cadastro ----------
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- Home / Cardápio ----------
            composable(Routes.HOME) {
                HomeScreen(
                    onProductClick = { productId ->
                        navController.navigate(Routes.productDetail(productId))
                    },
                    onCartClick = {
                        navController.navigate(Routes.CART)
                    }
                )
            }

            // ---------- Detalhe do Produto ----------
            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                val productId = entry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Routes.CART) }
                )
            }

            // ---------- Carrinho ----------
            composable(Routes.CART) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Routes.CHECKOUT) }
                )
            }

            // ---------- Checkout ----------
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Routes.tracking(orderId)) {
                            // Remove checkout e carrinho da pilha após finalizar
                            popUpTo(Routes.CART) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- Acompanhamento ----------
            composable(
                route = Routes.TRACKING,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { entry ->
                val orderId = entry.arguments?.getLong("orderId") ?: 0L
                OrderTrackingScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() }
                )
            }

            // ---------- Histórico ----------
            composable(Routes.HISTORY) {
                OrderHistoryScreen(
                    onTracking = { orderId ->
                        navController.navigate(Routes.tracking(orderId))
                    },
                    onBack = { navController.navigate(Routes.HOME) }
                )
            }

            // ---------- Fidelidade ----------
            composable(Routes.LOYALTY) {
                LoyaltyScreen(
                    onBack = { navController.navigate(Routes.HOME) }
                )
            }

            // ---------- Perfil ----------
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.navigate(Routes.HOME) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ---------- Bottom bar (apenas telas principais) ----------
        if (showBottomBar) {
            BottomBar(
                currentRoute = currentRoute,
                onItemSelected = { item ->
                    if (item.route != currentRoute) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { inclusive = item.route == Routes.HOME }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
