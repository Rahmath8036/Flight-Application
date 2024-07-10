package com.example.flightapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flightapp.viewmodel.BookedFlightsViewModel
import com.example.flightapp.viewmodel.Flight
import com.example.flightapp.viewmodel.SearchViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Composable function representing the search screen.
 * It allows users to search for flights and displays the search results.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel(),
                 bookedFlightsViewModel: BookedFlightsViewModel = viewModel(),
                 navController: NavController) {
    // Collect the state of flights and error from the view model
    val flights by viewModel.flights.collectAsState()
    val error by viewModel.error.collectAsState()

    // Mutable states for the search criteria and maintaining state when rotated
    var tripType by rememberSaveable { mutableStateOf("Return") }
    var fromDestination by rememberSaveable { mutableStateOf("") }
    var toDestination by rememberSaveable { mutableStateOf("") }
    var departureDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var returnDate by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(7)) }
    var passengers by rememberSaveable { mutableIntStateOf(1) }

    // Scaffold layout with a top app bar and a content section
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
                            text = "  Search Flights",
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
    )
    {
        // LazyColumn for displaying the search form and results
        LazyColumn(modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 60.dp)
        ) {
            // Toggle button group for selecting trip type
            item{
                ToggleButtonGroup(tripType = tripType, onToggleChanged = { tripType = it })
            }
            // Input fields for origin and destination
            item{Spacer(Modifier.height(16.dp))}
            item{
                DestinationInput("From", fromDestination) { fromDestination = it }
            }
            item{
                Spacer(Modifier.height(8.dp))
            }
            item{
                DestinationInput("To", toDestination) { toDestination = it }
            }
            item{Spacer(Modifier.height(16.dp))}
            // Date pickers for departure and return dates (if the trip type is return)
            item{
                DatePicker("Departure Date", departureDate) { departureDate = it }
            }
            if (tripType == "Return") {
                item{
                    Spacer(Modifier.height(8.dp))
                }
                item{
                    DatePicker("Return Date", returnDate!!) { returnDate = it }
                }
            }
            item{Spacer(Modifier.height(16.dp))}
            // Input field for the number of passengers
            item{
                NumberOfPassengers(passengers) { passengers = it }
            }
            item{Spacer(Modifier.height(16.dp))}
            // Button to trigger the flight search
            item {
                Button(
                    onClick = {
                        viewModel.searchFlights(
                            fromDestination,
                            toDestination,
                            departureDate,
                            if (tripType == "Return") returnDate else null,
                            passengers,
                            tripType
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors=ButtonDefaults.buttonColors(
                        backgroundColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Search Flights", color = Color.White)
                }
            }
            // Display the search results
            if (flights.isNotEmpty()) {
                items(flights) { flight ->
                    FlightCard(flight, viewModel, bookedFlightsViewModel, navController, passengers)
                }
            }
            // Display error message if any
            if (error != null) {
                item {
                    Text(error!!, color = Color.Red)
                }
            }
        }
    }
}

/**
 * Composable function for displaying a flight card with booking options.
 */
@Composable
fun FlightCard(
    flight: Flight,
    searchViewModel: SearchViewModel,
    bookedFlightsViewModel: BookedFlightsViewModel,
    navController: NavController,
    numberOfPassengers: Int
) {
    // Mutable states for showing dialog and booking progress
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var bookingInProgress by remember { mutableStateOf(false) }
    val bookingError by searchViewModel.error.collectAsState()
    val context = LocalContext.current

    // Show booking confirmation dialog if needed
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = { Text("Confirm Booking") },
            text = { Text("Do you want to book this flight? Please note once confirmed, it cannot be reversed.") },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(all = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                    ) { Text("Cancel", color = Color.White) }
                    Spacer(Modifier.width(110.dp))
                    Button(
                        onClick = {
                            println("Booking flight with $numberOfPassengers passengers") // Log the passenger count
                            bookingInProgress = true
                            searchViewModel.bookFlight(
                                flightId = flight.id,
                                numberOfPassengers = numberOfPassengers,
                                context = context,  // Pass the context here
                                onSuccess = {
                                    bookedFlightsViewModel.scheduleNotificationsForFlight(context, flight)
                                    navController.navigate("myTrips") // Navigate to the My Trips screen
                                    bookingInProgress = false
                                    showDialog = false
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                        enabled = !bookingInProgress
                    ) {
                        if (bookingInProgress) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Confirm", color = Color.White)
                        }
                    }
                }
            }
        )
    }

    // Display error message if booking failed
    if (bookingError != null) {
        Text(bookingError!!, color = Color.Red, modifier = Modifier.padding(8.dp))
    }
    // Card layout for displaying flight information
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("From: ${flight.origin} to ${flight.destination}", style = MaterialTheme.typography.h6)
            Text("Departure: ${flight.departureDate}", style = MaterialTheme.typography.body1)
            Text("Price: $${flight.price}", style = MaterialTheme.typography.body2)
            if (flight.tripType == "Return" && flight.returnDate != null) {
                Text("Return: ${flight.returnDate}", style = MaterialTheme.typography.body2)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1565C0),
                    contentColor = Color.White
                )
            ) {
                Text("Book This Flight")
            }
        }
    }
}

/**
 * Composable function for displaying a toggle button group.
 * It allows users to select the trip type (Return or One Way).
 */
@Composable
fun ToggleButtonGroup(tripType: String, onToggleChanged: (String) -> Unit) {
    Row {
        ToggleButton("Return", tripType == "Return", onToggleChanged)
        Spacer(Modifier.width(8.dp))
        ToggleButton("One Way", tripType == "One Way", onToggleChanged)
    }
}
/**
 * Composable function for a single toggle button.
 */
@Composable
fun ToggleButton(text: String, checked: Boolean, onToggleChanged: (String) -> Unit) {
    Button(
        onClick = { onToggleChanged(text) },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (checked) Color.DarkGray else Color.LightGray,
            contentColor = if (checked) Color.White else Color.Black
        )
    ) {
        Text(text)
    }
}
/**
 * Composable function for inputting a destination (origin or destination).
 */
@Composable
fun DestinationInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {input -> onValueChange(input.trim()) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF1565C0),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        )
    )
}
/**
 * Composable function for a date picker.
 * It displays a text field that, when clicked, shows a date picker dialog.
 */
@Composable
fun DatePicker(label: String, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val context = LocalContext.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        DatePickerDialog(
            context,
            R.style.DatePickerDialogTheme,
            { _, year, monthOfYear, dayOfMonth ->
                onDateSelected(LocalDate.of(year, monthOfYear + 1, dayOfMonth))
            },
            date.year, date.monthValue - 1, date.dayOfMonth
        ).apply {
            setOnDismissListener { showDialog = false }
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
        showDialog = false
    }

    OutlinedTextField(
        value = date.format(formatter),
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF1565C0),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        )
    )
}
/**
 * Composable function for inputting the number of passengers.
 * It displays buttons to increase or decrease the number of passengers.
 */
@Composable
fun NumberOfPassengers(passengers: Int, onPassengersChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Passengers", fontSize = 18.sp)
        Spacer(Modifier.width(16.dp))
        Button(onClick = { if (passengers > 1) onPassengersChange(passengers - 1) },
            colors=ButtonDefaults.buttonColors(
                backgroundColor = Color.DarkGray,
                contentColor = Color.White
            )
        ) {
            Text("-")
        }
        Text("$passengers", modifier = Modifier.padding(horizontal = 8.dp))
        Button(onClick = { onPassengersChange(passengers + 1) },
            colors=ButtonDefaults.buttonColors(
                backgroundColor = Color.DarkGray,
                contentColor = Color.White
            )
        ) {
            Text("+")
        }
    }
}