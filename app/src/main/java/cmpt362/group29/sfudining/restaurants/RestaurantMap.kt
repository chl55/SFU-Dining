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
import androidx.compose.runtime.LaunchedEffect
import cmpt362.group29.sfudining.cart.CartRepository
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun RestaurantMap(
    restaurants: List<Restaurant>,
    onMarkerClick: (Restaurant) -> Unit
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

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            for (restaurant in restaurants) {
                Log.d("ViewModel", "Fetched restaurant: ${restaurant.name}")
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            restaurant.location.latitude,
                            restaurant.location.longitude
                        )
                    ),
                    title = restaurant.name,
                    icon = when {
                        RestaurantUtils.isOpenNow(restaurant.schedule) && RestaurantUtils.closesWithinAnHour(restaurant.schedule) ->
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                        RestaurantUtils.isOpenNow(restaurant.schedule) ->
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        else ->
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    },
                    onClick = {
                        selectedRestaurant = restaurant
                        true
                    }
                )

            }
        }

        selectedRestaurant?.let { restaurant ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = restaurant.name)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val items = CartRepository.cartItems
                                if (items.isEmpty()) {
                                    Log.d("CheckInButton", "Cart is empty")
                                } else {
                                    items.forEach { item ->
                                        Log.d("CheckInButton", "Cart item: ${item.title}, ${item.price}, qty: ${item.quantity}")
                                    }
                                }
                            }
                        ) {
                            Text("Check-in")
                        }
                        Button(onClick = { onMarkerClick(restaurant) }) {
                            Text("Restaurant Details")
                        }
                        Button(onClick = { selectedRestaurant = null }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

