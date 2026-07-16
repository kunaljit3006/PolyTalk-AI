package com.example.polytalkai.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: (String, String) -> Unit,
    onForgotPasswordNavigate: () -> Unit
) {
    var activeTab by remember { mutableStateOf("signin") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand Header (Custom Logo + Text Logo)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            PolyTalkLogoDark(
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Sign in to sync your translations",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextMutedColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch Tabs Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x331B2838))
                .border(1.dp, GlassBorderColor, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            val isSignIn = activeTab == "signin"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSignIn) PrimaryColor else Color.Transparent)
                    .clickable { activeTab = "signin" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSignIn) BgColor else TextMutedColor
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isSignIn) PrimaryColor else Color.Transparent)
                    .clickable { activeTab = "signup" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (!isSignIn) BgColor else TextMutedColor
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields Column
        Column(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (activeTab == "signup") {
                Text(
                    text = "Full Name",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TextMutedColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                CustomInputField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "Kunal",
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = "Email Address",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            CustomInputField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Password",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            CustomInputField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            if (activeTab == "signin") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryColor,
                        modifier = Modifier.clickable { onForgotPasswordNavigate() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Grad1, Grad2)))
                    .clickable {
                        val email = if (emailInput.isEmpty()) "user@example.com" else emailInput
                        val name = if (nameInput.isEmpty()) "User" else nameInput
                        onLoginSuccess(email, name)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (activeTab == "signin") "Sign In" else "Create Account",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = BgColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(TextMutedColor.copy(alpha = 0.3f))
            )
            Text(
                text = "or continue with",
                fontFamily = SatoshiFontFamily,
                fontSize = 11.sp,
                color = TextMutedColor,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(TextMutedColor.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Buttons Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Google Login Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .glassmorphic(cornerRadius = 16)
                    .clickable { onLoginSuccess("google.user@example.com", "Google User") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GoogleIcon(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextColor
                )
            }

            // GitHub Login Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .glassmorphic(cornerRadius = 16)
                    .clickable { onLoginSuccess("github.user@example.com", "GitHub User") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GitHubIcon(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GitHub",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextColor
                )
            }
        }
    }
}

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        cursorBrush = SolidColor(PrimaryColor),
        textStyle = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextColor
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x331B2838))
                    .border(1.dp, GlassBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextMutedColor.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val scale = w / 48f
        
        val redPath = Path().apply {
            moveTo(24f * scale, 9.5f * scale)
            cubicTo(27.54f * scale, 9.5f * scale, 30.71f * scale, 10.72f * scale, 33.21f * scale, 13.1f * scale)
            lineTo(40.06f * scale, 6.25f * scale)
            cubicTo(35.9f * scale, 2.38f * scale, 30.47f * scale, 0f, 24f * scale, 0f)
            cubicTo(14.62f * scale, 0f, 6.51f * scale, 5.38f * scale, 2.56f * scale, 13.22f * scale)
            lineTo(10.54f * scale, 19.41f * scale)
            cubicTo(12.43f * scale, 13.72f * scale, 17.74f * scale, 9.5f * scale, 24f * scale, 9.5f * scale)
        }
        drawPath(redPath, Color(0xFFEA4335))

        val bluePath = Path().apply {
            moveTo(46.98f * scale, 24.55f * scale)
            cubicTo(46.98f * scale, 22.98f * scale, 46.83f * scale, 21.46f * scale, 46.6f * scale, 20f * scale)
            lineTo(24f * scale, 20f * scale)
            lineTo(24f * scale, 29.02f * scale)
            lineTo(36.94f * scale, 29.02f * scale)
            cubicTo(36.36f * scale, 31.98f * scale, 34.68f * scale, 34.5f * scale, 32.16f * scale, 36.2f * scale)
            lineTo(39.89f * scale, 42.2f * scale)
            cubicTo(44.4f * scale, 38.02f * scale, 46.98f * scale, 31.84f * scale, 46.98f * scale, 24.55f * scale)
        }
        drawPath(bluePath, Color(0xFF4285F4))

        val yellowPath = Path().apply {
            moveTo(10.53f * scale, 28.59f * scale)
            cubicTo(10.05f * scale, 27.14f * scale, 9.77f * scale, 25.6f * scale, 9.77f * scale, 24f * scale)
            cubicTo(9.77f * scale, 22.4f * scale, 10.05f * scale, 20.86f * scale, 10.53f * scale, 19.41f * scale)
            lineTo(2.55f * scale, 13.22f * scale)
            cubicTo(0.92f * scale, 16.46f * scale, 0f, 20.12f * scale, 0f, 24f * scale)
            cubicTo(0f, 27.88f * scale, 0.92f * scale, 31.54f * scale, 2.56f * scale, 34.78f * scale)
            lineTo(10.53f * scale, 28.59f * scale)
        }
        drawPath(yellowPath, Color(0xFFFBBC05))

        val greenPath = Path().apply {
            moveTo(24f * scale, 48f * scale)
            cubicTo(30.48f * scale, 48f * scale, 35.93f * scale, 45.87f * scale, 39.89f * scale, 42.19f * scale)
            lineTo(32.16f * scale, 36.19f * scale)
            cubicTo(30.01f * scale, 37.64f * scale, 27.24f * scale, 38.5f * scale, 24f * scale, 38.5f * scale)
            cubicTo(17.74f * scale, 38.5f * scale, 12.43f * scale, 34.28f * scale, 10.53f * scale, 28.59f * scale)
            lineTo(2.56f * scale, 34.78f * scale)
            cubicTo(6.51f * scale, 42.62f * scale, 14.62f * scale, 48f * scale, 24f * scale, 48f * scale)
        }
        drawPath(greenPath, Color(0xFF34A853))
    }
}

@Composable
fun GitHubIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val scale = w / 16f
        
        val gitPath = Path().apply {
            moveTo(8f * scale, 0f)
            cubicTo(3.58f * scale, 0f, 0f, 3.58f * scale, 0f, 8f * scale)
            cubicTo(0f, 11.54f * scale, 2.29f * scale, 14.53f * scale, 5.47f * scale, 15.59f * scale)
            cubicTo(5.87f * scale, 15.66f * scale, 6.02f * scale, 15.42f * scale, 6.02f * scale, 15.21f * scale)
            cubicTo(6.02f * scale, 15.02f * scale, 6.01f * scale, 14.39f * scale, 6.01f * scale, 13.72f * scale)
            cubicTo(4f * scale, 14.09f * scale, 3.48f * scale, 13.23f * scale, 3.32f * scale, 12.78f * scale)
            cubicTo(3.23f * scale, 12.55f * scale, 2.84f * scale, 11.84f * scale, 2.5f * scale, 11.65f * scale)
            cubicTo(2.22f * scale, 11.5f * scale, 1.82f * scale, 11.13f * scale, 2.49f * scale, 11.12f * scale)
            cubicTo(3.12f * scale, 11.11f * scale, 3.57f * scale, 11.7f * scale, 3.72f * scale, 11.94f * scale)
            cubicTo(4.44f * scale, 13.15f * scale, 5.59f * scale, 12.81f * scale, 6.05f * scale, 12.6f * scale)
            cubicTo(6.12f * scale, 12.08f * scale, 6.33f * scale, 11.73f * scale, 6.56f * scale, 11.53f * scale)
            cubicTo(4.78f * scale, 11.33f * scale, 2.92f * scale, 10.64f * scale, 2.92f * scale, 7.58f * scale)
            cubicTo(2.92f * scale, 6.71f * scale, 3.23f * scale, 5.99f * scale, 3.74f * scale, 5.43f * scale)
            cubicTo(3.66f * scale, 5.23f * scale, 3.38f * scale, 4.41f * scale, 3.82f * scale, 3.31f * scale)
            cubicTo(3.82f * scale, 3.31f * scale, 4.49f * scale, 3.1f * scale, 6.02f * scale, 4.13f * scale)
            cubicTo(6.66f * scale, 3.95f * scale, 7.34f * scale, 3.86f * scale, 8.02f * scale, 3.86f * scale)
            cubicTo(8.7f * scale, 3.86f * scale, 9.38f * scale, 3.95f * scale, 10.02f * scale, 4.13f * scale)
            cubicTo(11.55f * scale, 3.09f * scale, 12.22f * scale, 3.31f * scale, 12.22f * scale, 3.31f * scale)
            cubicTo(12.66f * scale, 4.41f * scale, 12.38f * scale, 5.23f * scale, 12.3f * scale, 5.43f * scale)
            cubicTo(12.81f * scale, 5.99f * scale, 13.12f * scale, 6.71f * scale, 13.12f * scale, 7.58f * scale)
            cubicTo(13.12f * scale, 10.65f * scale, 11.25f * scale, 11.33f * scale, 9.47f * scale, 11.53f * scale)
            cubicTo(9.76f * scale, 11.78f * scale, 10.01f * scale, 12.26f * scale, 10.01f * scale, 13.01f * scale)
            cubicTo(10.01f * scale, 14.08f * scale, 10f * scale, 14.94f * scale, 10f * scale, 15.21f * scale)
            cubicTo(10f * scale, 15.42f * scale, 10.15f * scale, 15.66f * scale, 10.55f * scale, 15.59f * scale)
            cubicTo(13.73f * scale, 14.53f * scale, 16f * scale, 11.54f * scale, 16f * scale, 8f * scale)
            cubicTo(16f * scale, 3.58f * scale, 12.42f * scale, 0f, 8f * scale, 0f)
        }
        drawPath(gitPath, Color(0xFFF0F5FA))
    }
}

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var emailInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
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
                text = "Reset Password",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            fontFamily = SatoshiFontFamily,
            fontSize = 14.sp,
            color = TextMutedColor,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        Text(
            text = "Email Address",
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = TextMutedColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        CustomInputField(
            value = emailInput,
            onValueChange = { emailInput = it },
            placeholder = "name@example.com",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Grad1, Grad2)))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Send Reset Link",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = BgColor
            )
        }
    }
}
