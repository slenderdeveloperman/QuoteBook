package com.quotecards.utils

/**
 * Application-wide constants for timing, thresholds, and UI values.
 * Centralizing these makes them easier to maintain and test.
 */
object AppConstants {
    // Timing constants (milliseconds)
    const val SPLASH_DELAY_MS = 1500L
    const val SEARCH_DEBOUNCE_MS = 300L
    const val FLOW_SUBSCRIBE_TIMEOUT_MS = 5000L

    // UI constants
    const val SWIPE_THRESHOLD = 200f
    const val SWIPE_EXIT_OFFSET = 1500f
    const val ANIMATION_DIVISOR = 500f
    const val MAX_SEARCH_LENGTH = 100

    // Card stack constants
    const val MAX_VISIBLE_CARDS = 3
    const val CARD_SCALE_FACTOR = 0.04f
    const val CARD_ALPHA_FACTOR = 0.15f
    const val CARD_TRANSLATION_Y_DP = 20
    const val MAX_ROTATION_DEGREES = 15f
    const val CARD_STACK_BOTTOM_PADDING_DP = 140

    // Animation constants
    const val CARD_TO_BOTTOM_DURATION_MS = 400
}
