package com.example.messenger.presentation.screens.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class MessengerTokens(
    val isDark: Boolean,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val fieldFill: Color,
    val cardFill: Color,
    val pillFill: Color,
    val pillFillSelected: Color,
    val trackFill: Color,
    val neutralButtonFill: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
    val callAccept: Color,
    val textPrimary: Color,
    val textOnField: Color,
    val textMuted: Color,
    val divider: Color,
)

val DarkTokens = MessengerTokens(
    isDark = true,
    backgroundTop = BgDarkTop,
    backgroundBottom = BgDarkBottom,
    fieldFill = FieldFillDark,
    cardFill = CardFillDark,
    pillFill = PillTranslucentDark,
    pillFillSelected = PillWhite,
    trackFill = TabTrackTranslucent,
    neutralButtonFill = NeutralButtonDark,
    accent = AccentTeal,
    onAccent = PillWhite,
    danger = DeclineRed,
    callAccept = CallAcceptGreen,
    textPrimary = TextDarkPrimary,
    textOnField = TextLightPrimary,
    textMuted = LabelMutedDark,
    divider = DividerOnDark,
)

val LightTokens = MessengerTokens(
    isDark = false,
    backgroundTop = BgLightTop,
    backgroundBottom = BgLightBottom,
    fieldFill = FieldFillLight,
    cardFill = CardFillLight,
    pillFill = PillTranslucentLight,
    pillFillSelected = PillWhite,
    trackFill = TabTrackTranslucent,
    neutralButtonFill = NeutralButtonLight,
    accent = AccentTeal,
    onAccent = PillWhite,
    danger = DeclineRed,
    callAccept = CallAcceptGreen,
    textPrimary = TextLightPrimary,
    textOnField = TextLightPrimary,
    textMuted = TextLightMuted,
    divider = DividerOnLight,
)

val LocalMessengerTokens = staticCompositionLocalOf { DarkTokens }

val messengerTokens: MessengerTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalMessengerTokens.current
