package com.example.flightapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightapp.viewmodel.BookedFlightsViewModel
import com.example.flightapp.viewmodel.Flight
import kotlin.math.roundToInt

/**
 * FlightDetailsScreen is a composable function that displays a list of booked flights.
 * It includes functionalities like archiving a flight and sharing flight details.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun FlightDetailsScreen(viewModel: BookedFlightsViewModel = viewModel()) {
    // Collect the list of booked flights from the ViewModel
    val flights = viewModel.bookedFlights.collectAsState().value
    // Sort flights by departure date
    val sortedFlights = flights.sortedBy { it.departureDate }
    Scaffold(
        topBar = {
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
                            androidx.compose.material.Text(
                                text = "  My Trips",
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
    ) {
        LazyColumn(modifier = Modifier.padding(bottom = 60.dp)) {
            if (sortedFlights.isEmpty()) {
                item {
                    EmptyFlightsMessage() // Display message if no flights are booked
                }
            } else {
                items(sortedFlights) { flight ->
                    FlightCard(flight, viewModel) // Display each flight in a card
                }
            }
        }
    }
}
/**
 * Composable function to display a message when no flights are booked.
 */
@Composable
fun EmptyFlightsMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Book Your Next Flight With Us!",
            style = MaterialTheme.typography.h6,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover new destinations and adventures.",
            color = Color.Gray
        )
    }
}
/**
 * FlightCard is a composable function that displays flight information in a card format.
 * It includes swipe-to-archive functionality.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlightCard(flight: Flight,viewModel: BookedFlightsViewModel) {
    val context = LocalContext.current
    var showDialog by rememberSaveable { mutableStateOf(false) }  // State to control dialog visibility
    // Swipe gesture state and configuration
    val state = rememberSwipeableState(0)
    val size = 68.dp // Width of the revealed button
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    // Show confirmation dialog for archiving a flight
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Archive Flight") },
            text = { Text("The flight will be archived even if the trip hasn't been completed. Are you sure?") },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(all = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                    ) { Text("Cancel", color = Color.White) }
                    Spacer(modifier = Modifier.width(135.dp))  // Add space between buttons
                    Button(
                        onClick = {
                            viewModel.archiveFlight(flight)
                            showDialog = false
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                    ) { Text("Confirm", color = Color.White) }
                }
            }
        )
    }
    // Box container for swipeable functionality
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    )
    // Row containing the delete icon, revealed on swipe
    {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(size),  // Set width equal to the swipe area size
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .offset { IntOffset(state.offset.value.roundToInt(), 0) }, // Apply swipe offset to the card
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp))
            {
                androidx.compose.material.Text(
                    "Swipe to Archive", // Swipe to delete message
                    color = Color.Gray,
                    style = MaterialTheme.typography.caption
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "From: ${flight.origin} to ${flight.destination}",
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = { shareFlightDetails(context, flight) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
                Text("Departure: ${flight.departureDate}", style = MaterialTheme.typography.body1)
                Text("Return: ${flight.returnDate ?: "N/A"}", style = MaterialTheme.typography.body2)
                Text("Price: $${flight.price}", style = MaterialTheme.typography.body2)
                Text("Passengers: ${flight.passengerCount}", style = MaterialTheme.typography.body2)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Button(
                        onClick = {
                            // Implicit intent to show the airport location in Google Maps
                            val gmmIntentUri =
                                Uri.parse("geo:0,0?q=${Uri.encode(flight.origin + " Airport")}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Place, contentDescription = "Location", tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Show Airport on Map", color = Color.White)
                    }
                }
            }
        }
    }
}
/**
 * Function to share flight details via other apps using a share sheet and implicit intent.
 */
fun shareFlightDetails(context: Context, flight: Flight) {
    val appLink = "https://play.google.com/store/apps/details?id=com.example.flightapp" // Replace with your actual app link
    // Format the flight details into a shareable string
    val shareText = """
        ✈️ Check out this trip from ${flight.origin} to ${flight.destination}!
        📅 Departure: ${flight.departureDate}
        📅 Return: ${flight.returnDate ?: "N/A"}
        💵 Price: $${flight.price}
        👥 Passengers: ${flight.passengerCount}
        
        I booked it with Sky Sailor! Discover amazing deals and book your next adventure with Sky Sailor too! 🚀

        Download the app now: $appLink
    """.trimIndent()
    // Create an implicit intent for sharing the flight details
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    // Create a chooser intent to let the user select an app for sharing
    val chooserIntent = Intent.createChooser(intent, "Share Flight Details")
    context.startActivity(chooserIntent)
}


