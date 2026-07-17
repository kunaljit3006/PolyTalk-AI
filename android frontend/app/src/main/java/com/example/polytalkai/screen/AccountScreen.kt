package com.example.polytalkai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.ui.theme.*

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun AccountScreen(
    userName: String,
    userEmail: String,
    userAvatarUrl: String?,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceColor)
                    .border(1.dp, GlassBorderColor, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Account settings",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceColor)
                    .border(1.5.dp, GlassBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TextColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextColor
            )
            Text(
                text = userEmail,
                fontFamily = SatoshiFontFamily,
                fontSize = 14.sp,
                color = TextMutedColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Settings list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 18)
                    .padding(8.dp)
            ) {
                // Clickable Row for Log Out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign Out Account",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextColor
                    )
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = TextMutedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(color = GlassBorderColor, thickness = 1.dp)

                // Clickable Row for Delete Account
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delete Account",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DangerColor
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Account",
                        tint = DangerColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out Account",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your account?",
                    fontFamily = SatoshiFontFamily,
                    color = TextMutedColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DangerColor
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = SatoshiFontFamily,
                        color = TextColor
                    )
                }
            },
            containerColor = SurfaceColor,
            titleContentColor = TextColor,
            textContentColor = TextColor
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account?",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DangerColor
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete your account? This action is irreversible and all your translation history will be lost.",
                    fontFamily = SatoshiFontFamily,
                    color = TextMutedColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(
                        text = "Delete Permanent",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DangerColor
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = SatoshiFontFamily,
                        color = TextColor
                    )
                }
            },
            containerColor = SurfaceColor,
            titleContentColor = TextColor,
            textContentColor = TextColor
        )
    }
}
