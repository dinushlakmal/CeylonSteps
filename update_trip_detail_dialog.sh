sed -i '/val sheetState =/i\    var showDeleteConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }' app/src/main/java/com/example/ui/components/TripDetailBottomSheet.kt

cat << 'INNER_EOF' > snippet.txt

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { androidx.compose.material3.Text("Delete Location") },
            text = { androidx.compose.material3.Text("Are you sure you want to delete this location? It will be moved to the Recycle Bin and permanently deleted after 7 days.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                    onDismiss()
                }) {
                    androidx.compose.material3.Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }
INNER_EOF

sed -i -e '/ModalBottomSheet(/r snippet.txt' app/src/main/java/com/example/ui/components/TripDetailBottomSheet.kt
