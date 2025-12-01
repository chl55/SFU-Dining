package cmpt362.group29.sfudining

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import cmpt362.group29.sfudining.auth.AuthActivity
import cmpt362.group29.sfudining.auth.AuthViewModel
import cmpt362.group29.sfudining.cart.CartDetailScreen
import cmpt362.group29.sfudining.profile.Profile
import cmpt362.group29.sfudining.restaurants.RestaurantNavHost
import cmpt362.group29.sfudining.ui.theme.SFUDiningTheme
import cmpt362.group29.sfudining.visits.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SFUDiningTheme {
                MainPage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage() {
    val startDestination = Destination.HOME
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SFUTopAppBar(scrollBehavior, navController) },
        bottomBar = { SFUNavigationBar(navController) }
    ) { contentPadding ->
        AppNavHost(navController, startDestination, Modifier.padding(contentPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SFUNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route
    val selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedIconColor = selectedIconColor.copy(alpha = 0.5f)
    val selectedTextColor = selectedIconColor
    val unselectedTextColor = unselectedIconColor
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer

    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            unselectedIconColor = unselectedIconColor,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            indicatorColor = indicatorColor
        )

        Destination.entries
            .filter { it != Destination.PROFILE }
            .forEach { destination ->
                NavigationBarItem(
                    selected = selectedDestination == destination.route,
                    onClick = {
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                                saveState = true
                            }
                        }
                    },
                    icon = { Icon(destination.icon, contentDescription = destination.title) },
                    label = { Text(destination.title) },
                    colors = itemColors
                )
            }
    }
}

enum class Destination(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "home"),
    BROWSE("Browse", Icons.Default.Search, "browse"),
    MAP("Map", Icons.Default.Place, "map"),
    CHECKINS("Check-Ins", Icons.Default.DateRange, "checkins"),
    PROFILE("Profile", Icons.Default.AccountCircle, "profile")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    NavHost(navController, startDestination.route) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> RestaurantNavHost(
                        modifier = modifier,
                        startDestination = "home_page",
                        onNavigateParent = { route ->
                            navController.navigate(route)
                        }
                    )
                    Destination.MAP -> RestaurantNavHost(
                        modifier = modifier,
                        startDestination = "map",
                        onNavigateParent = { route ->
                            navController.navigate(route)
                        }
                    )
                    Destination.BROWSE -> RestaurantNavHost(
                        modifier = modifier,
                        startDestination = "browse_list",
                        onNavigateParent = { route ->
                            navController.navigate(route)
                        }
                    )
                    Destination.CHECKINS -> {
                        val repository = VisitRepository(FirebaseFirestore.getInstance())
                        val visitViewModel: VisitViewModel = viewModel(
                            factory = VisitViewModelFactory(repository)
                        )
                        VisitPage(modifier, navController, visitViewModel)
                    }

                    Destination.PROFILE -> {
                        val userEmail = remember { authViewModel.getUserEmail() }
                        Profile(
                            email = userEmail ?: "Not logged in",
                            onSignOutClick = {
                                val context = LocalContext.current
                                authViewModel.signOut()
                                val intent = Intent(context, AuthActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
        composable(
            route = "add_visit/{visitJson}",
            arguments = listOf(
                navArgument("visitJson") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->

            val visitJson = backStackEntry.arguments?.getString("visitJson")
            val visit: Visit? =
                if (visitJson == null || visitJson == "new") null
                else Gson().fromJson(visitJson, Visit::class.java)

            val repository = VisitRepository(FirebaseFirestore.getInstance())
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destination.CHECKINS.route)
            }
            val visitViewModel: VisitViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = VisitViewModelFactory(repository)
            )

            AddVisitPage(
                viewModel = visitViewModel,
                navController = navController,
                modifier = modifier,
                initialVisit = visit
            )
        }
        composable(
            "visit_detail/{visitId}",
            arguments = listOf(navArgument("visitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getString("visitId")
            val repository = VisitRepository(FirebaseFirestore.getInstance())
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destination.CHECKINS.route)
            }
            val visitViewModel: VisitViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = VisitViewModelFactory(repository)
            )
            val visit = visitViewModel.visits.find { it.id == visitId }
            if (visit != null) {
                VisitDetailPage(visit, visitViewModel, navController, modifier)
            }
        }
        composable("insights") { backStackEntry ->
            val repository = VisitRepository(FirebaseFirestore.getInstance())
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destination.CHECKINS.route)
            }
            val visitViewModel: VisitViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = VisitViewModelFactory(repository)
            )
            InsightsPage(visitViewModel, modifier)
        }
        composable("cart") {
            CartDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFUTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    navController: NavHostController
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        title = {
            Text("SFU Dining")
        },
        actions = {
            IconButton(onClick = {
                navController.navigate(Destination.PROFILE.route)
            }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = {
                navController.navigate("cart")
            }) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Cart",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

