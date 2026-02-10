package com.quotecards.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.quotecards.R

@OptIn(ExperimentalTextApi::class)
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
private val ebGaramond = GoogleFont("EB Garamond")

@OptIn(ExperimentalTextApi::class)
val HomeTitleFontFamily = FontFamily(
    Font(R.font.garamond_regular, FontWeight.Normal),
    GoogleFontEntry(
        googleFont = ebGaramond,
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    )
)
