sed -i '/val sheetState =/i\    var showDeleteConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }' app/src/main/java/com/example/ui/components/TripDetailBottomSheet.kt

sed -i 's/onDelete()/showDeleteConfirm = true/g' app/src/main/java/com/example/ui/components/TripDetailBottomSheet.kt
sed -i 's/onDismiss()//g' app/src/main/java/com/example/ui/components/TripDetailBottomSheet.kt

