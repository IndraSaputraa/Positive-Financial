package com.positivefinancial.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.positivefinancial.app.ui.accounts.AccountDetailScreen
import com.positivefinancial.app.ui.accounts.AccountsScreen
import com.positivefinancial.app.ui.accounts.AddEditAccountScreen
import com.positivefinancial.app.ui.creditcards.AddEditCreditCardScreen
import com.positivefinancial.app.ui.creditcards.CreditCardDetailScreen
import com.positivefinancial.app.ui.creditcards.CreditCardsScreen
import com.positivefinancial.app.ui.dashboard.DashboardScreen
import com.positivefinancial.app.ui.settings.SettingsScreen
import com.positivefinancial.app.ui.transactions.AddEditTransactionScreen
import com.positivefinancial.app.ui.transactions.TransactionListScreen
import com.positivefinancial.app.ui.transactions.TransferScreen

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Screen.Dashboard, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Screen.Transactions, "Activity", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomTab(Screen.Accounts, "Accounts", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    BottomTab(Screen.Cards, "Cards", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    BottomTab(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun PositiveFinancialApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            val isTopLevel = bottomTabs.any { it.screen.route == currentRoute?.route }
            if (isTopLevel) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddTransaction = { type -> navController.navigate(Screen.AddEditTransaction.create(type = type)) },
                    onSeeAllTransactions = { navController.navigate(Screen.Transactions.route) },
                    onTransfer = { navController.navigate(Screen.Transfer.route) },
                    onOpenAccount = { id -> navController.navigate(Screen.AccountDetail.create(id)) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    onTransactionClick = { id -> navController.navigate(Screen.AddEditTransaction.create(transactionId = id)) },
                    onAddClick = { navController.navigate(Screen.AddEditTransaction.create()) }
                )
            }

            composable(Screen.Accounts.route) {
                AccountsScreen(
                    onAccountClick = { id -> navController.navigate(Screen.AccountDetail.create(id)) },
                    onAddAccount = { navController.navigate(Screen.AddEditAccount.create()) },
                    onTransfer = { navController.navigate(Screen.Transfer.route) }
                )
            }

            composable(Screen.Cards.route) {
                CreditCardsScreen(
                    onCardClick = { id -> navController.navigate(Screen.CardDetail.create(id)) },
                    onAddCard = { navController.navigate(Screen.AddEditCard.create()) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("type") { type = NavType.StringType; defaultValue = "EXPENSE" }
                )
            ) {
                AddEditTransactionScreen(onDone = { navController.popBackStack() })
            }

            composable(Screen.Transfer.route) {
                TransferScreen(onDone = { navController.popBackStack() })
            }

            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                AccountDetailScreen(
                    onEdit = { id -> navController.navigate(Screen.AddEditAccount.create(id)) },
                    onBack = { navController.popBackStack() },
                    onTransactionClick = { id -> navController.navigate(Screen.AddEditTransaction.create(transactionId = id)) }
                )
            }

            composable(
                route = Screen.AddEditAccount.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                AddEditAccountScreen(onDone = { navController.popBackStack() })
            }

            composable(
                route = Screen.CardDetail.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                CreditCardDetailScreen(
                    onEdit = { id -> navController.navigate(Screen.AddEditCard.create(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AddEditCard.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                AddEditCreditCardScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
