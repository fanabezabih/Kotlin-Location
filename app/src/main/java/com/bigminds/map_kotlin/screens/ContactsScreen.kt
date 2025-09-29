package com.bigminds.map_kotlin.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.core.content.FileProvider
import com.bigminds.map_kotlin.viewmodel.ContactsViewModel


import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onClickAdd: () -> Unit,
    onClickContact: (Int) -> Unit,
    viewModel: ContactsViewModel = viewModel()
) {
    viewModel.getContacts()
    val contacts by viewModel.contacts.observeAsState(emptyList())
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "My contacts") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onClickAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No contacts yet. Tap + to add one.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(contacts) { contact ->
                    Card(
                        onClick = { onClickContact(contact.contactId) },
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUri: Uri? = contact.imageUrl?.let { path ->
                                when {
                                    path.startsWith("content://") || path.startsWith("http") ->
                                        Uri.parse(path)
                                    else -> {
                                        val file = File(path)
                                        if (file.exists()) FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        ) else null
                                    }
                                }
                            }

                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = "Contact Image",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { onClickContact(contact.contactId) },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = "Default Contact Icon",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { onClickContact(contact.contactId) }
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Name: ${contact.name}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Phone: ${contact.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (contact.email.isNotBlank()) {
                                    Text(
                                        text = "Email: ${contact.email}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (!contact.imageUrl.isNullOrBlank()) {
                                    Text(
                                        text = "Has photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
