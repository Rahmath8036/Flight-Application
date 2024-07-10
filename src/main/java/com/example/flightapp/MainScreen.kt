package com.example.flightapp

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightapp.viewmodel.BookedFlightsViewModel
import com.example.flightapp.viewmodel.Flight
import com.example.flightapp.viewmodel.Offer
import com.example.flightapp.viewmodel.OffersViewModel
import com.example.flightapp.viewmodel.OffersViewModelFactory


/**
 * MainScreen is the primary UI component of the app, displaying upcoming flights and special offers.
 * It also includes navigation options via the AppBar.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(bookedFlightsViewModel: BookedFlightsViewModel = viewModel()) {
    val context = LocalContext.current
    val flights = bookedFlightsViewModel.bookedFlights.collectAsState().value
    val offersViewModel: OffersViewModel = viewModel(factory = OffersViewModelFactory(context))
    Scaffold(
        topBar = { AppBar() },
        content = { padding ->
            BodyContent(modifier = Modifier.padding(padding), flights = flights, offersViewModel = offersViewModel)
        }
    )
}

/**
 * AppBar is a composable function that displays the top bar with the app's logo and title.
 */
@Composable
fun AppBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(50.dp)
                        .aspectRatio(1f)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "SkySailor",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        backgroundColor = Color(0xFF1565C0),
        contentColor = Color.White,
        elevation = 10.dp
    )
}
/**
 * BodyContent is a composable function that structures the main content of the screen,
 * including upcoming flights, special offers, and flight cabins.
 *
 * @param modifier Modifier for custom styling.
 * @param flights List of booked flights.
 * @param offersViewModel ViewModel for managing offers.
 */
@Composable
fun BodyContent(
    modifier: Modifier = Modifier,
    flights: List<Flight>,
    offersViewModel: OffersViewModel
) {
    LazyColumn(modifier = modifier.padding(16.dp).padding(bottom = 60.dp)) {
        item {
            GreetingSection()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Upcoming Flights", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (flights.isEmpty()) {
            item {
                EmptyFlightsMessageOnMain()  // Displays a message when there are no upcoming flights.
            }
        } else {
            items(flights) { flight ->
                SimpleFlightItem(flight)
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OffersSection(offersViewModel)
            Spacer(modifier = Modifier.height(16.dp))
            FlightCabins()
        }
    }
}
/**
 * SimpleFlightItem displays individual flight details within a Card.
 *
 * @param flight Flight details to display.
 */
@Composable
fun SimpleFlightItem(flight: Flight) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(flight.destination, style = MaterialTheme.typography.body1)
            Text(flight.departureDate, style = MaterialTheme.typography.body1)
        }
    }
}
/**
 * GreetingSection is a composable function that displays a welcome message and a brief introduction.
 */
@Composable
fun GreetingSection() {
    Text(text = "Welcome to SkySailor!", fontWeight = FontWeight.Bold, fontSize = 25.sp)
    Text(text = "Explore features to enhance your flying experience with Emirates.")
    Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
}

/**
 * EmptyFlightsMessageOnMain displays a friendly message when there are no booked flights.
 */
@Composable
fun EmptyFlightsMessageOnMain() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ready to explore?",
            style = MaterialTheme.typography.subtitle1,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Book your next flight with us and discover amazing destinations.",
            style = MaterialTheme.typography.body1,
            color = Color.Gray
        )
    }
}

/**
 * OffersSection displays special offers from the ViewModel.
 *
 * @param offersViewModel ViewModel for managing offers.
 */
@Composable
fun OffersSection(offersViewModel: OffersViewModel) {
    val offers by offersViewModel.offers.collectAsState()
    var isLoading by remember { mutableStateOf(true) }  // Initialize loading state

    // Update loading state when offers change
    LaunchedEffect(offers) {
        isLoading = false
        Log.d("OffersSection", "Offers updated: ${offers.size}")
    }

    Column {
        Text("Special Offers", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (offers.isNotEmpty()) {
            LazyRow {
                items(offers) { offer ->
                    OfferItem(offer, offersViewModel)
                }
            }
        } else {
            Text("No current offers available", style = MaterialTheme.typography.body1)
        }
    }
}
/**
 * FlightCabins displays a list of different flight cabins in a horizontal scrollable row.
 */
@Composable
fun FlightCabins() {
    Text("Explore Our Cabins", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    LazyRow (
        modifier = Modifier.padding(vertical = 8.dp) // Add padding around the LazyRow
    ){
        items(cabins) { cabin ->
            CabinItem(cabin)
        }
    }
}

/**
 * OfferItem displays individual offer details within a Card.
 *
 * @param offer Offer details to display.
 * @param offersViewModel ViewModel for managing offer favorites.
 */
@Composable
fun OfferItem(offer: Offer, offersViewModel: OffersViewModel) {
    val isFavorite = offersViewModel.isFavorite(offer.id).collectAsState(initial = false)
    val showTooltip = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(250.dp)
            .height(150.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = offer.description, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Discount: ${offer.discount}", fontSize = 16.sp, color = MaterialTheme.colors.secondary)
                Text(text = "Expires on: ${offer.expiryDate}", fontSize = 14.sp, color = MaterialTheme.colors.onSurface)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = {
                    offersViewModel.saveFavoriteStatus(offer.id, !isFavorite.value)
                    showTooltip.value = true
                }) {
                    Icon(
                        imageVector = if (isFavorite.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite.value) Color.Red else Color.Gray
                    )
                }
            }
            if (showTooltip.value) {
                Text(
                    "Click to toggle favorite",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )

                // Use LaunchedEffect to handle timeout
                LaunchedEffect(showTooltip.value) {
                    kotlinx.coroutines.delay(10000)
                    showTooltip.value = false
                }
            }
        }
    }
}

/**
 * CabinItem displays details of a specific flight cabin within a Card.
 *
 * @param cabin Cabin details to display.
 */
@Composable
fun CabinItem(cabin: Cabin) {
    Card(
        modifier = Modifier.padding(8.dp).width(200.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = cabin.type, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
            Text(text = cabin.features, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp)) // Adds space between text and image
            Image(
                painter = painterResource(id = cabin.imageResId),
                contentDescription = "${cabin.type} Cabin",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
        }
    }
}
// Cabin Type data class
data class Cabin(val type: String, val features: String,val imageResId: Int)

val cabins = listOf(
    Cabin("Economy", "Comfort and value",R.drawable.economy_class),
    Cabin("Business", "Work and relax",R.drawable.business_class),
    Cabin("First Class", "Luxury and privacy",R.drawable.first_class)
)