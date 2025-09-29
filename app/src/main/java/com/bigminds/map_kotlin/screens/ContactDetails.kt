package com.bigminds.map_kotlin.screens

import android.Manifest
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bigminds.map_kotlin.viewmodel.ContactsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun ContactDetailsScreen(
    contactId: Int,
    navController: NavController,
    viewModel: ContactsViewModel = viewModel()
) {
    val ctx = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            photoUri = tempPhotoUri
            val persistedPath = persistImage(ctx, tempPhotoUri!!) ?: return@rememberLauncherForActivityResult
            viewModel.updateContactPhoto(contactId, persistedPath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            val persistedPath = persistImage(ctx, it) ?: return@let
            viewModel.updateContactPhoto(contactId, persistedPath)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri = createImageUri(ctx)
            tempPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getContactById(contactId)
    }

    val contact by viewModel.contactLiveData.observeAsState()
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(34.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            ) {
                val displayUri = photoUri
                    ?: contact?.imageUrl?.let { path ->
                        if (path.startsWith("content://") || path.startsWith("http")) {
                            Uri.parse(path)
                        } else {
                            getFileUri(ctx, path)
                        }
                    }

                if (displayUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(displayUri),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        tempPhotoUri = createImageUri(ctx)
                                        tempPhotoUri?.let { uri ->
                                            cameraLauncher.launch(uri)
                                        }
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        tempPhotoUri = createImageUri(ctx)
                                        tempPhotoUri?.let { uri ->
                                            cameraLauncher.launch(uri)
                                        }
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                IconButton(
                    onClick = { showDialog.value = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Photo",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Change Profile Picture") },
                    text = { Text("Choose source") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        tempPhotoUri = createImageUri(ctx)
                                        tempPhotoUri?.let { uri ->
                                            cameraLauncher.launch(uri)
                                        }
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                                showDialog.value = false
                            }
                        ) {
                            Text("Camera")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                                showDialog.value = false
                            }
                        ) {
                            Text("Gallery")
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            contact?.let {
                Text(text = it.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(text = " ${it.phoneNumber}")
                if (it.email.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = " ${it.email}")
                }
                if (!it.imageUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = " Photo saved", color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call")
                    Icon(imageVector = Icons.Outlined.Send, contentDescription = "Message")
                }
            }
        }
    }
}

fun createImageUri(context: Context): Uri? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_$timestamp"
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File.createTempFile(fileName, ".jpg", storageDir)

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun persistImage(context: Context, uri: Uri): String? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timestamp.jpg"
        val storageDir = File(context.getExternalFilesDir(null), "profile_pics")

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val destinationFile = File(storageDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        destinationFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFileUri(context: Context, filePath: String): Uri? {
    return try {
        val file = File(filePath)
        if (!file.exists()) return null
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}