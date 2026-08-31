sed -i '/onAppThemeTypeChange:/a\    recycledTrips: List<TripLocation> = emptyList(),\n    onRestoreTrip: (TripLocation) -> Unit = {},\n    onPermanentlyDeleteTrip: (TripLocation) -> Unit = {},' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

sed -i '/val context = LocalContext.current/a\    var isRecycleBinOpen by remember { mutableStateOf(false) }' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

cat << 'INNER_EOF' > snippet_button.kt
                            OutlinedButton(
                                onClick = { isRecycleBinOpen = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_recycle_bin"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Recycle Bin", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            }
INNER_EOF

sed -i '/\/\/ Google Sign-In Status Row/e cat snippet_button.kt' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

cat << 'INNER_EOF2' > snippet_dialog.kt
    if (isRecycleBinOpen) {
        com.example.ui.components.RecycleBinDialog(
            recycledTrips = recycledTrips,
            onRestoreTrip = onRestoreTrip,
            onPermanentlyDeleteTrip = onPermanentlyDeleteTrip,
            onDismiss = { isRecycleBinOpen = false }
        )
    }
INNER_EOF2

sed -i '/\/\/ ================= 9 PROVINCES EXPLORATION PROGRESS =================/e cat snippet_dialog.kt' app/src/main/java/com/example/ui/screens/ProfileScreen.kt

