

package com.mygallery.app

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalleryScreen()
        }
    }
}

@Composable
fun GalleryScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var photos by remember { mutableStateOf(listOf<Uri>()) }
    var permission by remember {
        mutableStateOf(
            context.checkSelfPermission(
                if (Build.VERSION.SDK_INT >= 33)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun loadPhotos() {
        val list = mutableListOf<Uri>()

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                list.add(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            }
        }

        photos = list
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permission =
            if (Build.VERSION.SDK_INT >= 33) {
                it[Manifest.permission.READ_MEDIA_IMAGES] == true
            } else {
                it[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }

        if (permission) loadPhotos()
    }

    LaunchedEffect(Unit) {
        if (permission) loadPhotos()
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7FA))
                .padding(16.dp)
        ) {
            Text(
                "My Gallery",
                fontSize = 30.sp,
                color = Color(0xFF20202A)
            )

            Text(
                "Your beautiful memories",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!permission) {
                Button(onClick = {
                    val permissions =
                        if (Build.VERSION.SDK_INT >= 33)
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        else
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )

                    launcher.launch(permissions)
                }) {
                    Text("Allow Photos Access")
                }
            } else {
                Text(
                    "${photos.size} Photos",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    items(photos) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Gallery Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}
