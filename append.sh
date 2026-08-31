cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/ProfileScreen.kt
    if (isRecycleBinOpen) {
        com.example.ui.components.RecycleBinDialog(
            recycledTrips = recycledTrips,
            onRestoreTrip = onRestoreTrip,
            onPermanentlyDeleteTrip = onPermanentlyDeleteTrip,
            onDismiss = { isRecycleBinOpen = false }
        )
    }
}
INNER_EOF
