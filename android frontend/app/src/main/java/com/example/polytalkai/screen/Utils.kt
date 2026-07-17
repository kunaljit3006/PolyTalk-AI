package com.example.polytalkai.screen

import java.util.Locale

fun getLocaleForLanguage(langName: String): Locale {
    return when (langName) {
        "English" -> Locale.US
        "Hindi" -> Locale("hi", "IN")
        "Tamil" -> Locale("ta", "IN")
        "Telugu" -> Locale("te", "IN")
        "Bengali" -> Locale("bn", "IN")
        "Assamese" -> Locale("as", "IN")
        "Marathi" -> Locale("mr", "IN")
        "Gujarati" -> Locale("gu", "IN")
        "Kannada" -> Locale("kn", "IN")
        "Malayalam" -> Locale("ml", "IN")
        "Punjabi" -> Locale("pa", "IN")
        "Odia" -> Locale("or", "IN")
        "French" -> Locale.FRANCE
        "Spanish" -> Locale("es", "ES")
        "German" -> Locale.GERMANY
        "Italian" -> Locale.ITALY
        "Russian" -> Locale("ru", "RU")
        "Japanese" -> Locale.JAPAN
        else -> Locale.getDefault()
    }
}
