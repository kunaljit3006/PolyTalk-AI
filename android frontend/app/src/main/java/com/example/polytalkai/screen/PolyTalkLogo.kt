package com.example.polytalkai.screen

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun PolyTalkAppIcon(
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/polytalk-app-icon.svg")
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = "PolyTalk App Icon",
        modifier = modifier.aspectRatio(1f)
    )
}

@Composable
fun PolyTalkLogoDark(
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/polytalk-logo-dark.svg")
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = "PolyTalk Logo",
        modifier = modifier.aspectRatio(260f / 60f)
    )
}
