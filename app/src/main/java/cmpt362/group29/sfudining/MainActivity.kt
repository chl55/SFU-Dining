package cmpt362.group29.sfudining

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import cmpt362.group29.sfudining.auth.AuthRepository
import cmpt362.group29.sfudining.auth.AuthViewModel
import cmpt362.group29.sfudining.browse.BrowsePage
import cmpt362.group29.sfudining.cart.CartDetailScreen
import cmpt362.group29.sfudining.cart.CartViewModel
import cmpt362.group29.sfudining.profile.Profile
import cmpt362.group29.sfudining.profile.ProfileViewModel
import cmpt362.group29.sfudining.restaurants.RestaurantDetailScreen
import cmpt362.group29.sfudining.restaurants.RestaurantMap
import cmpt362.group29.sfudining.restaurants.RestaurantViewModel
import cmpt362.group29.sfudining.ui.components.HomePage
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
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer

    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            unselectedIconColor = unselectedIconColor,
            selectedTextColor = selectedIconColor,
            unselectedTextColor = unselectedIconColor,
            indicatorColor = indicatorColor
        )

        Destination.entries
            .filter { it != Destination.PROFILE }
            .forEach { destination ->
                NavigationBarItem(
                    selected = selectedDestination == destination.route,
                    onClick = {
                        navController.popBackStack(
                            route = navController.graph.startDestinationRoute!!,
                            inclusive = false
                        )
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            restoreState = true
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
    val repository = remember { VisitRepository(FirebaseFirestore.getInstance()) }
    val visitViewModel: VisitViewModel = viewModel(
        factory = VisitViewModelFactory(repository)
    )
    val restaurantViewModel: RestaurantViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val userId = AuthRepository().getCurrentUser()?.uid
    visitViewModel.loadVisits(userId ?: "")
    restaurantViewModel.getRestaurants()
    val gson = Gson()
    NavHost(navController, startDestination.route) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomePage(
                        restaurantViewModel = restaurantViewModel,
                        visitViewModel = visitViewModel,
                        modifier = modifier,
                        onRestaurantClick = { restaurantId ->
                            navController.navigate("info/$restaurantId")
                        },
                        profileViewModel = profileViewModel
                    )

                    Destination.MAP -> RestaurantMap(
                        restaurantViewModel = restaurantViewModel,
                        onMarkerClick = { restaurant ->
                            navController.navigate("info/${restaurant.id}")
                        },
                        onCheckInClick = { visit: Visit? ->
                            val visitJson = visit?.let { gson.toJson(it) } ?: ""
                            navController.navigate("add_visit/$visitJson")
                        },
                        modifier = modifier
                    )

                    Destination.BROWSE -> BrowsePage(
                        modifier = modifier,
                        onRestaurantClick = { restaurantId ->
                            navController.navigate("info/$restaurantId")
                        }
                    )

                    Destination.CHECKINS -> {
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
                            },
                            profileViewModel = profileViewModel,
                            modifier = modifier
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

            AddVisitPage(
                viewModel = visitViewModel,
                profileViewModel = profileViewModel,
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
            val visits by visitViewModel.visits.collectAsState()

            val visit = visits.find { it.id == visitId }
            if (visit != null) {
                VisitDetailPage(visit, visitViewModel, profileViewModel, navController, modifier)
            }
        }
        composable("insights") {
            InsightsPage(visitViewModel, modifier)
        }
        composable("cart") {
            CartDetailScreen(
                modifier = modifier,
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("map") {
            RestaurantMap(
                restaurantViewModel = restaurantViewModel,
                onMarkerClick = { restaurant ->
                    navController.navigate("info/${restaurant.id}")
                },
                onCheckInClick = { visit: Visit? ->
                    val visitJson = visit?.let { gson.toJson(it) } ?: ""
                    navController.navigate("add_visit/$visitJson")
                },
                modifier = modifier
            )
        }
        composable("browse_list") {
            BrowsePage(
                modifier = modifier,
                onRestaurantClick = { restaurantId ->
                    navController.navigate("info/$restaurantId")
                }
            )
        }
        composable("home_page") {
            HomePage(
                restaurantViewModel = restaurantViewModel,
                visitViewModel = visitViewModel,
                modifier = modifier,
                onRestaurantClick = { restaurantId ->
                    navController.navigate("info/$restaurantId")
                },
                profileViewModel = profileViewModel
            )
        }
        composable("info/{restaurantId}") { backStackEntry ->
            val restId = backStackEntry.arguments?.getString("restaurantId")
            Log.d("NavHost", "Navigating to restaurant ID: $restId")
            LaunchedEffect(restId) {
                if (restId != null) {
                    restaurantViewModel.getRestaurant(restId)
                }
            }
            val restaurant by restaurantViewModel.restaurant.collectAsState()
            restaurant?.let { restaurantData ->
                RestaurantDetailScreen(
                    restaurantData,
                    cartViewModel,
                    navController,
                    onCheckInClick = { visit: Visit? ->
                        val visitJson = visit?.let { gson.toJson(it) } ?: ""
                        navController.navigate("add_visit/$visitJson")
                    }) {
                    navController.popBackStack()
                }
            }
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

