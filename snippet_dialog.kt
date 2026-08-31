    if (isRecycleBinOpen) {
        com.example.ui.components.RecycleBinDialog(
            recycledTrips = recycledTrips,
            onRestoreTrip = onRestoreTrip,
            onPermanentlyDeleteTrip = onPermanentlyDeleteTrip,
            onDismiss = { isRecycleBinOpen = false }
        )
    }
