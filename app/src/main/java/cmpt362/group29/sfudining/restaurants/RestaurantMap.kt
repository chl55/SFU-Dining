package cmpt362.group29.sfudining.restaurants

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import cmpt362.group29.sfudining.cart.CartItem
import cmpt362.group29.sfudining.cart.CartRepository
import cmpt362.group29.sfudining.restaurants.RestaurantUtils.OpeningStatusBadge
import cmpt362.group29.sfudining.restaurants.RestaurantUtils.distanceToRestaurant
import cmpt362.group29.sfudining.visits.Visit
import cmpt362.group29.sfudining.visits.VisitItem
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Date

fun cartItemsToVisitItems(cartItems: List<CartItem>): List<VisitItem> {
    return cartItems.map { cartItem ->
        VisitItem(
            itemName = cartItem.title,
            cost = cartItem.price.replace("$", "").toDoubleOrNull(),
            calories = cartItem.calories,
            quantity = cartItem.quantity
        )
    }
}

@Composable
fun RestaurantMap(
    restaurants: List<Restaurant>,
    onMarkerClick: (Restaurant) -> Unit,
    onCheckInClick: (Visit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(49.2781, -122.9199), 15f)
    }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            restaurants.forEach { restaurant ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            restaurant.location.latitude,
                            restaurant.location.longitude
                        )
                    ),
                    title = restaurant.name,
                    icon =
                        if (selectedRestaurant?.id == restaurant.id)
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        else if (RestaurantUtils.isOpenNow(restaurant.schedule) &&
                            RestaurantUtils.closesWithinAnHour(restaurant.schedule))
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                        else if (RestaurantUtils.isOpenNow(restaurant.schedule))
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        else
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onClick = {
                        selectedRestaurant = restaurant
                        true
                    }
                )
            }
        }

        selectedRestaurant?.let { restaurant ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    AsyncImage(
                        model = restaurant.restaurantImageURL,
                        contentDescription = restaurant.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        OpeningStatusBadge(restaurant.schedule)
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))

                    val ctx = LocalContext.current

                    val distance by produceState<String?>(initialValue = null, restaurant.location) {
                        value = distanceToRestaurant(ctx, restaurant.location)
                    }
                    distance?.let {
                        Text(it)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val visitItems = cartItemsToVisitItems(
                                    CartRepository.cartItems.filter {
                                        it.restaurantName == restaurant.name
                                    }
                                )

                                val visit = Visit(
                                    restaurantId = restaurant.id,
                                    restaurantName = restaurant.name,
                                    items = visitItems,
                                    totalCost = visitItems.sumOf { it.cost ?: 0.0 },
                                    totalCal = visitItems.sumOf { it.calories ?: 0 },
                                    datetime = Date()
                                )
                                onCheckInClick(visit)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check-in")
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { onMarkerClick(restaurant) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Details")
                        }

                        Spacer(Modifier.width(8.dp))

                        TextButton(onClick = { selectedRestaurant = null }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

