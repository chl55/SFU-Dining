package cmpt362.group29.sfudining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cmpt362.group29.sfudining.ui.theme.SFUDiningTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.nestedScroll

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
    var selectedTab by remember { mutableStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SFUTopAppBar() },
        bottomBar = {
            SFUNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> Greeting("Home", Modifier.padding(innerPadding))
            1 -> Greeting("Browse", Modifier.padding(innerPadding))
            2 -> Greeting("Map", Modifier.padding(innerPadding))
            3 -> Greeting("Profile", Modifier.padding(innerPadding))
            else -> Greeting("Settings", Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFUTopAppBar() {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        title = {
            Text("SFU Dining")
        },
        actions = {
            IconButton(onClick = { /* settings */ }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFUNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
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

        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Browse") },
            label = { Text("Browse") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("Map") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = itemColors
        )
    }
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