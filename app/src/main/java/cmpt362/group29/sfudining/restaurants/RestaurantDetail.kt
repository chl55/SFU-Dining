package cmpt362.group29.sfudining.restaurants

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import cmpt362.group29.sfudining.R

@Composable
fun RestaurantDetailScreen(restaurant: Restaurant?, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        RestaurantImg()
        Spacer(modifier = Modifier.height(16.dp))
        RestaurantDesc(restaurant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FeaturedItems()
        }
        Spacer(modifier = Modifier.height(15.dp))
        Button(onBack, Modifier.align(Alignment.End)) {
            Text("Back")
        }
    }
}
@Composable
fun RestaurantImg() {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
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
        text = restaurant?.cuisine ?: "Unknown Cuisine",
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
fun FeaturedItems() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.hot_wings),
                contentDescription = "Restaurant Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Mouthwatering Wings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "8 mouthwatering hot wings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "$10.95",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
