
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flightapp.R
import com.example.flightapp.viewmodel.BookedFlightsViewModel
import com.example.flightapp.viewmodel.Flight
import com.example.flightapp.viewmodel.LoginViewModel
import kotlin.math.roundToInt

/**
 * AccountSettingsScreen is a composable function for the account settings screen.
 * It handles archived flights, notification settings, and logout functionality.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AccountSettingsScreen(viewModel: BookedFlightsViewModel = viewModel(),loginViewModel: LoginViewModel,navController: NavHostController) {
    val flights = viewModel.fetchArchivedFlights.collectAsState().value
    var showArchivedFlights by rememberSaveable { mutableStateOf(false) } // state to toggle flights visibility
    var showAlertDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) } // State for logout confirmation dialog

    // Notification settings
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    var notificationsEnabled by rememberSaveable {
        mutableStateOf(prefs.getBoolean("notificationsEnabled", NotificationManagerCompat.from(context).areNotificationsEnabled()))
    }
    var showDisableNotificationsDialog by rememberSaveable { mutableStateOf(false) }

    // Observe lifecycle changes to update notification status
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentStatus = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (currentStatus != notificationsEnabled) {
                    notificationsEnabled = currentStatus
                    prefs.edit().putBoolean("notificationsEnabled", notificationsEnabled).apply()
                    if (notificationsEnabled) {
                        showAlertDialog = true
                    }
                    viewModel.scheduleNotificationsForUpcomingFlights(context)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // This LaunchedEffect will check the permission and schedule notifications at the start and whenever the notification setting changes.
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.scheduleNotificationsForUpcomingFlights(context)
            }
        }
    }
//    // Function to check if system notifications are enabled
//    fun areNotificationsEnabled(): Boolean {
//        return NotificationManagerCompat.from(context).areNotificationsEnabled()
//    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    // Launcher for requesting notification permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            notificationsEnabled = true
            prefs.edit().putBoolean("notificationsEnabled", true).apply()
            showAlertDialog = true  // Show the alert dialog when notifications are successfully enabled
            if (permissionRequested) {
                Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                showAlertDialog = true  // Show the alert dialog when notifications are successfully enabled
                viewModel.scheduleNotificationsForUpcomingFlights(context)
            }
        } else {
            notificationsEnabled = false
            prefs.edit().putBoolean("notificationsEnabled", false).apply()
            Toast.makeText(context, "Permission denied for notifications.", Toast.LENGTH_LONG).show()
        }
        permissionRequested = false
    }

    // Only request permission if not already granted and notifications are intended to be enabled
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    // Show alert dialog when notifications are scheduled
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Notification Scheduled") },
            text = { Text("Flight notifications will be received 24 hours before the flight.") },
            confirmButton = {
                Button(onClick = { showAlertDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.DarkGray,
                        contentColor = Color.White
                    )) {
                    Text("OK")
                }
            }
        )
    }
    // Show disable notifications dialog
    if (showDisableNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showDisableNotificationsDialog = false },
            title = { Text("Disable Notifications") },
            text = { Text("Current flight notifications will be cancelled. Please go to Settings > FlightApp > Notifications to disable them for future flights.") },
            confirmButton = {
                Button(
                    onClick = { showDisableNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
    /** Main layout with scaffold for displaying the ability to enable and disable notifications,
     * and the archived list of flights
     * */
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
                        Text(
                            text = "Account Settings",
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
    ) { innerPadding ->
        // LazyColumn for account settings content
        LazyColumn(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .padding(bottom = 60.dp)) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preferences", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(fontSize = 18.sp, text = "Enable Notifications")
                    Spacer(modifier = Modifier.width(25.dp))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.POST_NOTIFICATIONS)) {
                                        // Re-prompt for permission if denied once
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        // Direct to settings if permission permanently denied
                                        Toast.makeText(context, "Please enable notifications in settings.", Toast.LENGTH_LONG).show()
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Ensures no history stack is modified
                                        }
                                        context.startActivity(intent)
                                    }
                                } else {
                                    // Permission already granted
                                    notificationsEnabled = true
                                    showAlertDialog = true
                                    prefs.edit().putBoolean("notificationsEnabled", true).apply()
                                    Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                                    viewModel.scheduleNotificationsForUpcomingFlights(context)
                                }
                            } else {
                                notificationsEnabled = false
                                showDisableNotificationsDialog = true
                                prefs.edit().putBoolean("notificationsEnabled", false).apply()
                                viewModel.cancelAllScheduledNotifications(context)
                            }
                        }
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Archived Flights", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp)) // Adds a small space between the text and the icon
                    IconButton(onClick = { showArchivedFlights = !showArchivedFlights }) {
                        Icon(
                            imageVector = if (showArchivedFlights) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (showArchivedFlights) "Hide flights" else "Show flights"
                        )
                    }
                    Text(
                        text = if (showArchivedFlights) "Click to Hide" else "Click to Show",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }


            if (showArchivedFlights) {
                if (flights.isNotEmpty()) {
                    items(flights) { flight ->
                        FlightCard(flight,viewModel)
                    }
                } else {
                    item {
                        Text("No archived flights available.")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        showLogoutDialog= true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontSize = 16.sp)
                    }
                }
            }
        }
    }
    // Permission already granted
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(120.dp))  // This adds space between the buttons
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            loginViewModel.logoutUser() // Call logout function from LoginViewModel
                            navController.popBackStack(route = "login", inclusive = false) // Navigate back to the login screen
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        )
    }

}

/**
 * FlightCard is a composable function that displays flight information in a card format.
 * It includes swipe-to-delete functionality.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlightCard(flight: Flight,viewModel: BookedFlightsViewModel) {
    val state = rememberSwipeableState(0)
    val size = 68.dp // Width of the revealed button
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    // Show delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Flight") },
            text = { Text("Are you sure you want to delete this archived flight? This action cannot be undone.") },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),  // Adjust padding as needed
                    horizontalArrangement = Arrangement.End  // This places the buttons at the end of the dialog
                ) {
                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray, // Or any other color
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(130.dp))  // Adds space between the buttons
                    Button(
                        onClick = {
                            viewModel.softDeleteFlight(flight.id)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray, // Or any other color
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        )
    }
    // Swipe-able card with flight information
    Box(
         modifier = Modifier
             .fillMaxWidth()
             .swipeable(
                 state = state,
                 anchors = anchors,
                 thresholds = { _, _ -> FractionalThreshold(0.3f) },
                 orientation = Orientation.Horizontal
             )
     ){
        // Row containing the delete icon, revealed on swipe
        Row(
             modifier = Modifier
                 .align(Alignment.CenterEnd)
                 .width(size),  // Set width equal to the swipe area size
             horizontalArrangement = Arrangement.End
         ) {
             IconButton(onClick = {
                 showDeleteDialog = true
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
                Text(
                    "Swipe to delete", // Swipe to delete message
                    color = Color.Gray,
                    style = MaterialTheme.typography.caption
                )
                Text(
                    "From: ${flight.origin} to ${flight.destination}",
                    style = MaterialTheme.typography.h6
                )
                androidx.compose.material3.Text(
                    "Departure: ${flight.departureDate}",
                    style = MaterialTheme.typography.body1
                )
                androidx.compose.material3.Text(
                    "Return: ${flight.returnDate ?: "N/A"}",
                    style = MaterialTheme.typography.body2
                )
                androidx.compose.material3.Text(
                    "Price: $${flight.price}",
                    style = MaterialTheme.typography.body2
                )
                androidx.compose.material3.Text(
                    "Passengers: ${flight.passengerCount}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}