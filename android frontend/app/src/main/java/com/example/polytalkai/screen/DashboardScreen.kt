package com.example.polytalkai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.ui.theme.*

@Composable
fun DashboardScreen(
    userName: String,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    val images = listOf(
        "https://images.unsplash.com/photo-1543269865-cbf427effbad?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1531538606174-0f90ff5dce83?auto=format&fit=crop&w=600&q=80"
    )
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Cinematic Background Image (Coil AsyncImage with Crossfade & Blur)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(images[currentImageIndex])
                    .crossfade(true)
                    .crossfade(1000)
                    .build(),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize(),
                alpha = 0.8f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.2f to Color.Transparent,
                            0.7f to BgColor.copy(alpha = 0.8f),
                            1f to BgColor
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Static Header
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Dash Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    PolyTalkLogoDark(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Good Evening, $userName",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Translate Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(backgroundColor = Color(0x66030A10), cornerRadius = 20)
                        .clickable { onNavigate("text-screen") }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryColor.copy(alpha = 0.2f))
                            .border(1.dp, PrimaryColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Quick",
                            tint = PrimaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "QUICK TRANSLATE",
                            fontFamily = SatoshiFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to start translating instantly",
                            fontFamily = SatoshiFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFA0C4E6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // Translation Modes Section
                Text(
                    text = "TRANSLATION MODES",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMutedColor,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // 2x2 Feature Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Text Translation Tile
                    FeatureTile(
                        title = "TEXT",
                        description = "Type or paste text to translate across 18 languages",
                        icon = Icons.Default.TextFields,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("text-screen") }
                    )
                    // Voice Translation Tile
                    FeatureTile(
                        title = "VOICE",
                        description = "Speak naturally and get instant translation",
                        icon = Icons.Default.Mic,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("voice-screen") }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Conversation Translation Tile
                    FeatureTile(
                        title = "CONVERSATION",
                        description = "Real-time dual-mic translation mode",
                        icon = Icons.Default.Hearing,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("conv-screen") }
                    )
                    // Camera Translation Tile
                    FeatureTile(
                        title = "CAMERA",
                        description = "Scan signs, menus, and documents",
                        icon = Icons.Default.CameraAlt,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("cam-screen") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Section
            Text(
                text = "RECENT",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextMutedColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // History items
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RecentHistoryItem(
                    sourceText = "Hello, how are you?",
                    targetText = "हैलो, आप कैसे हैं?",
                    langBadge = "EN → HI"
                )
                RecentHistoryItem(
                    sourceText = "I need a taxi",
                    targetText = "Necesito un taxi",
                    langBadge = "EN → ES"
                )
                RecentHistoryItem(
                    sourceText = "Where is the station?",
                    targetText = "স্টেশন কোথায়?",
                    langBadge = "EN → BN"
                )
            }

            Spacer(modifier = Modifier.height(150.dp)) // Spacer for bottom navigation
        }
    }
    }
}

@Composable
fun FeatureTile(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphic(backgroundColor = SurfaceColor, cornerRadius = 20)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1F0076FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontFamily = SatoshiFontFamily,
                fontSize = 11.sp,
                color = TextMutedColor,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun RecentHistoryItem(
    sourceText: String,
    targetText: String,
    langBadge: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(backgroundColor = SurfaceColor, cornerRadius = 16)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sourceText,
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = targetText,
                fontFamily = SatoshiFontFamily,
                fontSize = 13.sp,
                color = PrimaryColor
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x1F31C5F0))
                .border(1.dp, GlassBorderColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = langBadge,
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = PrimaryColor
            )
        }
    }
}
