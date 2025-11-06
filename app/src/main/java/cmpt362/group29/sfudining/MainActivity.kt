package cmpt362.group29.sfudining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cmpt362.group29.sfudining.browse.BrowsePage
import cmpt362.group29.sfudining.restaurants.RestaurantNavHost
import cmpt362.group29.sfudining.ui.theme.SFUDiningTheme
import cmpt362.group29.sfudining.ui.components.HomePage

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
    // notes on how to do viewmodel: https://composables.com/blog/viewmodels-in-jetpack-compose

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SFUTopAppBar(scrollBehavior) },
        bottomBar = {
            SFUNavigationBar(navController)
        }
    ) { contentPadding ->
        AppNavHost(navController, startDestination, modifier = Modifier.padding((contentPadding)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SFUNavigationBar(
    navController: NavHostController
) {
    // References:
    // https://developer.android.com/develop/ui/compose/components/navigation-bar
    // https://developer.android.com/guide/navigation/use-graph/navigate
    // https://developer.android.com/develop/ui/compose/navigation
    // https://developer.android.com/guide/navigation/backstack#savestate saving states

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route

    val selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
    val selectedTextColor = selectedIconColor
    val unselectedTextColor = unselectedIconColor
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            unselectedIconColor = unselectedIconColor,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            indicatorColor = indicatorColor
        )

        Destination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
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
    PROFILE("Profile", Icons.Default.AccountCircle, "profile")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
) {
    // Referenced example from https://developer.android.com/develop/ui/compose/components/navigation-bar
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomePage(modifier)
                    Destination.BROWSE -> BrowsePage(modifier)
                    Destination.MAP -> RestaurantNavHost()
                    Destination.PROFILE -> Greeting("Profile", modifier)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFUTopAppBar(scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()) {
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
            IconButton(onClick = { /* settings */ }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainPage()
}