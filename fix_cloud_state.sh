#!/bin/bash
sed -i '/var selectedTab/a \    var isCloudWorking by remember { mutableStateOf(false) }\n    var cloudStatusText by remember { mutableStateOf("") }' app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt
sed -i 's/isLoading = true/isCloudWorking = true/g' app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt
sed -i 's/isLoading = false/isCloudWorking = false/g' app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt
sed -i 's/lastResult = result//g' app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt
