package com.example.polytalkai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.ui.theme.*

data class HistoryItem(
    val sourceText: String,
    val translatedText: String,
    val fromLang: String,
    val toLang: String,
    var isSaved: Boolean = false
)

@Composable
fun HistoryScreen(
    historyItems: List<HistoryItem>,
    savedItems: MutableList<HistoryItem>,
    onBack: () -> Unit
) {
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
                text = "History",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        // List View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyItems) { item ->
                var isSaved by remember { mutableStateOf(item.isSaved) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(cornerRadius = 16)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.sourceText,
                            fontFamily = SatoshiFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.translatedText,
                            fontFamily = SatoshiFontFamily,
                            fontSize = 14.sp,
                            color = PrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        // Lang badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceColor2)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${item.fromLang} → ${item.toLang}",
                                fontFamily = SatoshiFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) PrimaryColor else TextMutedColor,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    isSaved = !isSaved
                                    item.isSaved = isSaved
                                    if (isSaved) {
                                        if (!savedItems.contains(item)) savedItems.add(item)
                                    } else {
                                        savedItems.remove(item)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
