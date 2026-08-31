sed -i 's/androidx.compose.material.icons.filled.Delete/Icons.Default.Delete/g' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

# Remove the recycle bin block
sed -i '605,612d' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

cat << 'INNER_EOF' > snippet.kt

    if (isRecycleBinOpen) {
        com.example.ui.components.RecycleBinDialog(
            recycledTrips = recycledTrips,
            onRestoreTrip = onRestoreTrip,
            onPermanentlyDeleteTrip = onPermanentlyDeleteTrip,
            onDismiss = { isRecycleBinOpen = false }
        )
    }
INNER_EOF

# Append it before the final brace of ProfileScreen
sed -i '$ e cat snippet.kt' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

