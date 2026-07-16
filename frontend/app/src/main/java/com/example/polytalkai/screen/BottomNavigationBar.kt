package com.example.polytalkai.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.ui.theme.*

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .glassmorphic(backgroundColor = BgColor, cornerRadius = 24)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            NavItem("Home", "dashboard", Icons.Default.Home),
            NavItem("History", "history", Icons.Default.History),
            NavItem("Saved", "saved", Icons.Default.Bookmark),
            NavItem("Account", "account", Icons.Default.Person)
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screenId
            val activeColor = PrimaryColor
            val inactiveColor = TextMutedColor

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTabSelected(item.screenId) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = if (isSelected) activeColor else inactiveColor,
                    modifier = Modifier.size(20.dp)
                )
                AnimatedVisibility(visible = isSelected) {
                    Row {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.name,
                            fontFamily = SatoshiFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = activeColor
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(
    val name: String,
    val screenId: String,
    val icon: ImageVector
)
