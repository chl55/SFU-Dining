package cmpt362.group29.sfudining.restaurants

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import cmpt362.group29.sfudining.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmpt362.group29.sfudining.cart.CartItem
import cmpt362.group29.sfudining.cart.CartViewModel

object FeaturedImages {
    val imageMap = mapOf(
        "deal_one" to R.drawable.deal_one,
        "hot_wings" to R.drawable.hot_wings,
        "pizza_drink" to R.drawable.pizza_drink
    )
}

@Composable
fun RestaurantDetailScreen(restaurant: Restaurant?, cartViewModel: CartViewModel = viewModel(), navController: NavController, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)

        ) {
            Spacer(modifier = Modifier.height(80.dp))
            RestaurantImg()
            Spacer(modifier = Modifier.height(16.dp))
            RestaurantDesc(restaurant)
            Spacer(modifier = Modifier.height(16.dp))
            restaurant?.featuredItems?.let {
                FeaturedItems(it, cartViewModel)
            }
            Spacer(modifier = Modifier.height(16.dp))
            restaurant?.menu?.let { menuItems ->
                MenuItemList(menuItems, cartViewModel)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onBack, Modifier.align(Alignment.Start)) {
                Text("Back")
            }
        }
        FloatingActionButton(
            onClick = { navController.navigate("cart") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White
            )
        }
    }
}
@Composable
fun RestaurantImg() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.uncle_fatih),
            contentDescription = "Uncle Fatih's Pizza",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center

        )
    }
}

@Composable
fun RestaurantDesc(restaurant: Restaurant?) {
    Text(
        text = restaurant?.name ?: "Unknown Restaurant",
        style = MaterialTheme.typography.headlineMedium
    )
    Text(
        text = restaurant?.category ?: "Unknown Cuisine",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Phone: ${restaurant?.phoneNum}",
                style = MaterialTheme.typography.bodySmall)
            Text("Address: ${restaurant?.address}",
                style = MaterialTheme.typography.bodySmall)

        }
        Spacer(modifier = Modifier.weight(1f))
        Column(modifier = Modifier.wrapContentWidth(),
            horizontalAlignment = Alignment.End) {
            Text("Schedule:",
                style = MaterialTheme.typography.bodySmall)
            restaurant?.schedule?.forEach {
                (day, hour) ->
                Text(
                    "$day - $hour",
                    style = MaterialTheme.typography.bodySmall
                )

            }
        }
    }
    Spacer(modifier = Modifier.height(30.dp))
}

@Composable
fun FeaturedItems(items: List<FeaturedItem>, cartViewModel: CartViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            FeaturedItemCard(item, cartViewModel)
        }
    }
}

@Composable
fun FeaturedItemCard(item: FeaturedItem, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier.widthIn(min = 200.dp, max = 250.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(
                    id = FeaturedImages.imageMap[item.imageName] ?: R.drawable.uncle_fatih
                ),
                contentDescription = "Restaurant Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = { cartViewModel.addItem(
                        CartItem(
                            item.title, item.price, 1
                        )
                    )}
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = Color.DarkGray
                    )
                }
            }

        }
    }
}

@Composable
fun MenuItemList(items: List<MenuItem>, cartViewModel: CartViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            MenuItems(item, cartViewModel)
            if (index < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun MenuItems(item: MenuItem, cartViewModel: CartViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = item.price,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        IconButton(
            onClick = { cartViewModel.addItem(
                CartItem(
                    item.title, item.price, 1
                )
            )}
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add item",
                tint = Color.DarkGray
            )
        }
    }
}