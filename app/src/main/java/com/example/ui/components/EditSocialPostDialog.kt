package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.social.SocialPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSocialPostDialog(
    post: SocialPost,
    onDismiss: () -> Unit,
    onSave: (SocialPost) -> Unit
) {
    var title by remember { mutableStateOf(post.title) }
    var story by remember { mutableStateOf(post.story) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Story") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = story,
                    onValueChange = { story = it },
                    label = { Text("Your Story") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(post.copy(title = title, story = story)) 
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
