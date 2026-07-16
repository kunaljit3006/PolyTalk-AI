package com.example.polytalkai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.polytalkai.R
import androidx.compose.ui.text.font.Font as LocalFont

// Colors
val BgColor = Color(0xFF030A10)
val SurfaceColor = Color(0xFF0D1B2A)
val SurfaceColor2 = Color(0xFF1B2838)
val TextColor = Color(0xFFF0F5FA)
val TextMutedColor = Color(0xFF8892B0)
val PrimaryColor = Color(0xFF31C5F0)
val AccentColor = Color(0xFF0076FF)
val DangerColor = Color(0xFFFF453A)
val GlassBgColor = Color(0xB20D1B2A) // 70% opacity
val GlassBorderColor = Color(0x1F31C5F0) // 12% opacity
val InputBgColor = Color(0xCC1B2838) // 80% opacity

val Grad1 = Color(0xFF0076FF)
val Grad2 = Color(0xFF31C5F0)

val OutfitFontFamily = FontFamily(
    LocalFont(R.font.outfit_regular, FontWeight.Normal),
    LocalFont(R.font.outfit_bold, FontWeight.Bold),
    LocalFont(R.font.outfit_extrabold, FontWeight.ExtraBold)
)

// Font Setup
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SatoshiFont = GoogleFont("Plus Jakarta Sans")

val SatoshiFontFamily = FontFamily(
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = SatoshiFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val DotGothicFont = GoogleFont("DotGothic16")

val DotGothicFontFamily = FontFamily(
    Font(googleFont = DotGothicFont, fontProvider = provider, weight = FontWeight.Normal)
)

// Typography
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = SatoshiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SatoshiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SatoshiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SatoshiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SatoshiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    secondary = AccentColor,
    background = BgColor,
    surface = SurfaceColor,
    onBackground = TextColor,
    onSurface = TextColor
)

@Composable
fun PolyTalkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
