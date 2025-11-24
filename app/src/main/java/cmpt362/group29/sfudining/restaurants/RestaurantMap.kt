package cmpt362.group29.sfudining.restaurants

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

@Composable
fun RestaurantMap(
    restaurants: List<Restaurant>,
    onMarkerClick: (Restaurant) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(49.2781, -122.9199), 15f)
    }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = MapProperties(
                mapType = MapType.TERRAIN)
        ) {
            for (restaurant in restaurants) {
                Log.d("ViewModel", "Fetched restaurant: ${restaurant.name}")
                Marker(state = MarkerState(
                    position = LatLng(
                        restaurant.location.latitude,
                        restaurant.location.longitude
                    )
                ), title = restaurant.name,
                    onClick = {
                        selectedRestaurant = restaurant
                        true
                    })

            }
        }
        selectedRestaurant?.let {
            restaurant ->
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
                        Button(onClick = { onMarkerClick(restaurant) }) {
                            Text("See Details")
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

