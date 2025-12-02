package cmpt362.group29.sfudining.restaurants

import android.content.Intent
import android.net.Uri
import coil.compose.rememberAsyncImagePainter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmpt362.group29.sfudining.cart.CartItem
import cmpt362.group29.sfudining.cart.CartRepository
import cmpt362.group29.sfudining.cart.CartViewModel
import cmpt362.group29.sfudining.restaurants.RestaurantUtils.OpeningStatusBadge
import cmpt362.group29.sfudining.visits.Visit
import java.util.Date


@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant?,
    cartViewModel: CartViewModel = viewModel(),
    navController: NavController,
    onCheckInClick: (Visit) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)

        ) {
            Spacer(modifier = Modifier.height(80.dp))
            RestaurantImg(restaurant)
            Spacer(modifier = Modifier.height(16.dp))
            RestaurantDesc(restaurant, onCheckInClick)
            Spacer(modifier = Modifier.height(16.dp))
            restaurant?.featuredItems?.let {
                FeaturedItems(it, cartViewModel, restaurant.name)
            }
            Spacer(modifier = Modifier.height(16.dp))
            restaurant?.menu?.let { menuItems ->
                MenuItemList(menuItems, cartViewModel, restaurant.name)
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
fun RestaurantImg(restaurant: Restaurant?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(restaurant?.restaurantImageURL),
            contentDescription = "Uncle Fatih's Pizza",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center

        )
    }
}

@Composable
fun RestaurantDesc(
    restaurant: Restaurant?,
    onCheckInClick: (Visit) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = restaurant?.name ?: "Unknown Restaurant",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            restaurant?.schedule?.let {
                OpeningStatusBadge(schedule = it)
            }
        }

        Text(
            text = restaurant?.cuisine ?: "Unknown Cuisine",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Phone: ${restaurant?.phone ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Address: ${restaurant?.address ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
                restaurant?.website?.let { website ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = website,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val visitItems = cartItemsToVisitItems(
                            CartRepository.cartItems.filter {
                                it.restaurantName == restaurant?.name
                            }
                        )

                        val visit = Visit(
                            restaurantId = restaurant?.id ?: "",
                            restaurantName = restaurant?.name ?: "",
                            items = visitItems,
                            totalCost = visitItems.sumOf { it.cost ?: 0.0 },
                            totalCal = visitItems.sumOf { it.calories ?: 0 },
                            datetime = Date()
                        )
                        onCheckInClick(visit)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check-in")
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "Schedule:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                restaurant?.schedule?.forEach { (day, hour) ->
                    Text(
                        "$day - $hour",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FeaturedItems(items: List<FeaturedItem>, cartViewModel: CartViewModel, restaurantName: String) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            FeaturedItemCard(item, cartViewModel, restaurantName)
        }
    }
}

@Composable
fun FeaturedItemCard(item: FeaturedItem, cartViewModel: CartViewModel, restaurantName: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.widthIn(min = 200.dp, max = 250.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(item.imageName),
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
                        text = "${item.price} • ${item.kcal} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = {
                        cartViewModel.addItem(
                            CartItem(
                                restaurantName, item.title, item.price, item.kcal.toInt(), 1
                            )
                        )
                        Toast.makeText(
                            context,
                            "${item.title} (${item.price}, ${item.kcal} kcal) added to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
fun MenuItemList(items: List<MenuItem>, cartViewModel: CartViewModel, restaurantName: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            MenuItems(item, cartViewModel, restaurantName)
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
fun MenuItems(item: MenuItem, cartViewModel: CartViewModel, restaurantName: String) {
    val context = LocalContext.current
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
                text = "${item.price} • ${item.kcal} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        IconButton(
            onClick = {
                cartViewModel.addItem(
                    CartItem(
                        restaurantName, item.title, item.price, item.kcal.toInt(), 1
                    )
                )
                Toast.makeText(
                    context,
                    "${item.title} (${item.price}, ${item.kcal} kcal) added to cart",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add item",
                tint = Color.DarkGray
            )
        }
    }
}