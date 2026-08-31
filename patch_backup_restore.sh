#!/bin/bash
sed -i '/                            }                        }                    }                }            }        }    }}/i \
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))\
                        androidx.compose.material3.Button(\
                            onClick = {\
                                val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)\
                                if (account == null) {\
                                    android.widget.Toast.makeText(context, "Please Sign In to Google first.", android.widget.Toast.LENGTH_SHORT).show()\
                                } else {\
                                    isLoading = true\
                                    scope.launch {\
                                        val jsonString = com.example.util.GoogleDriveBackupEngine.restoreFromDrive(\
                                            context, account\
                                        ) { progress, message ->\
                                            \/\/ progress\
                                        }\
                                        isLoading = false\
                                        if (jsonString != null) {\
                                            loadedJsonContent = jsonString\
                                            selectedTab = 1\
                                            android.widget.Toast.makeText(context, "Backup downloaded. Please configure restore options.", android.widget.Toast.LENGTH_LONG).show()\
                                        }\
                                    }\
                                }\
                            },\
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(48.dp),\
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),\
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary)\
                        ) {\
                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.CloudDownload, contentDescription = null)\
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))\
                            androidx.compose.material3.Text("Restore from Google Drive", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)\
                        }' app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt
