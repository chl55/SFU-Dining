package cmpt362.group29.sfudining.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cmpt362.group29.sfudining.restaurants.Restaurant
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.horizontalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowsePage(
    modifier: Modifier = Modifier,
    onRestaurantClick: (String) -> Unit
) {
    val viewModel = viewModel<BrowseViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val cuisines by viewModel.cuisines.collectAsState(initial = emptyMap())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    ) {
        SearchBar(
            query = searchText,
            onQueryChange = viewModel::onSearchTextChange
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisines.forEach { (cuisine, isSelected) ->
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onCuisineSelected(cuisine, !isSelected) },
                    label = { Text(cuisine) }
                )
            }
        }
        restaurants.forEach { restaurant ->
            RestaurantRow(
                name = restaurant.name,
                description = restaurant.description,
                restaurantImageURL = restaurant.restaurantImageURL,
                onClick = { onRestaurantClick(restaurant.id) }
            )
        }
    }
}

// https://developer.android.com/develop/ui/compose/text/user-input?textfield=value-based#textfield-nav
@Composable
fun SearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(2f),
            placeholder = { Text("Search restaurants...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            colors = TextFieldDefaults.colors()
        )
    }
}

@Composable
private fun RestaurantRow(
    name: String = "Restaurant Name",
    description: String = "Restaurant Details",
    restaurantImageURL: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(start = 12.dp)
            .padding(end = 4.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.5f))
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = restaurantImageURL),
                contentDescription = "Restaurant Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    BrowsePage(
        modifier = Modifier,
        onRestaurantClick = {}
    )
}