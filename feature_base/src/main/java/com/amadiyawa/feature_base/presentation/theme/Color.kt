package com.amadiyawa.feature_base.presentation.theme

import androidx.compose.ui.graphics.Color
import com.amadiyawa.feature_base.common.util.toColor

/**
 * Interface representing custom colors used in the application.
 *
 * @property success The color used to indicate success.
 * @property warning The color used to indicate a warning.
 * @property info The color used to indicate informational messages.
 * @property danger The color used to indicate danger or errors.
 * @property onSuccess The color used for text or icons on success backgrounds.
 * @property onWarning The color used for text or icons on warning backgrounds.
 * @property onInfo The color used for text or icons on informational backgrounds.
 * @property onDanger The color used for text or icons on danger backgrounds.
 * @author Amadou Iyawa
 */
interface CustomColor {
    val success: Color
    val warning: Color
    val info: Color
    val danger: Color
    val onSuccess: Color
    val onWarning: Color
    val onInfo: Color
    val onDanger: Color
}

internal const val WHITE = "#FFFFFF"
internal const val BLACK = "#000000"

// Material color
// Light Theme Colors
val LightPrimary = Color(0xFFE65100)         // Deep orange - more saturation for primary actions
val LightPrimaryVariant = Color(0xFFFF8A65)  // Lighter orange for hover/pressed states
val LightPrimaryContainer = Color(0xFFFFD0B5) // Very light orange container
val LightSecondary = Color(0xFF0277BD)       // Deep blue - complementary to orange for electricity theme
val LightSecondaryVariant = Color(0xFF58A5F0) // Lighter blue
val LightSecondaryContainer = Color(0xFFD0E8FF) // Light blue container
val LightTertiary = Color(0xFFC77800)        // Amber - energy accent
val LightTertiaryContainer = Color(0xFFFFECB3) // Light amber container
val LightSurface = Color(0xFFFFFFFF)         // Pure white
val LightSurfaceVariant = Color(0xFFF5F5F5)  // Very light gray
val LightSurfaceContainer = Color(0xFFEEEEEE) // Light gray container
val LightSurfaceContainerHigh = Color(0xFFE1E1E1) // Medium light gray
val LightSurfaceContainerLow = Color(0xFFF8F8F8)  // Off-white
val LightBackground = Color(0xFFFAFAFA)      // Near white
val LightError = Color(0xFFB71C1C)           // Dark red for better visibility
val LightOnPrimary = Color(0xFFFFFFFF)       // White text on primary
val LightOnPrimaryContainer = Color(0xFF7A2900) // Dark orange on light orange container
val LightOnSecondary = Color(0xFFFFFFFF)     // White text on secondary
val LightOnSecondaryContainer = Color(0xFF01579B) // Dark blue on light blue container
val LightOnTertiary = Color(0xFFFFFFFF)      // White on tertiary
val LightOnTertiaryContainer = Color(0xFF704400) // Dark amber on light amber container
val LightOnSurface = Color(0xFF212121)       // Near black on surface
val LightOnSurfaceVariant = Color(0xFF757575) // Medium gray on surface variant
val LightOnBackground = Color(0xFF212121)    // Near black on background
val LightOnError = Color(0xFFFFFFFF)         // White on error
val LightOutline = Color(0xFFBDBDBD)         // Medium light gray outline

// Dark Theme Colors
val DarkPrimary = Color(0xFFFF8A65)          // Vibrant orange for dark theme
val DarkPrimaryVariant = Color(0xFFFF5722)   // Deeper orange
val DarkPrimaryContainer = Color(0xFFBF360C) // Dark burnt orange container
val DarkSecondary = Color(0xFF64B5F6)        // Bright blue for electric theme
val DarkSecondaryVariant = Color(0xFF2196F3) // Medium blue
val DarkSecondaryContainer = Color(0xFF0D47A1) // Deep blue container
val DarkTertiary = Color(0xFFFFB74D)         // Bright amber
val DarkTertiaryContainer = Color(0xFF945900) // Dark amber container
val DarkSurface = Color(0xFF121212)          // Standard Material dark surface
val DarkSurfaceVariant = Color(0xFF2D2D2D)    // Slightly lighter surface
val DarkSurfaceContainer = Color(0xFF1E1E1E)  // Slightly lighter than background
val DarkSurfaceContainerHigh = Color(0xFF333333) // More elevated surface
val DarkSurfaceContainerLow = Color(0xFF1A1A1A) // Slight contrast from background
val DarkBackground = Color(0xFF121212)       // Standard Material dark background
val DarkError = Color(0xFFEF5350)            // Bright red for dark theme
val DarkOnPrimary = Color(0xFF000000)        // Black text on bright orange
val DarkOnPrimaryContainer = Color(0xFFFFDBC8) // Light orange on dark orange container
val DarkOnSecondary = Color(0xFF000000)      // Black text on bright blue
val DarkOnSecondaryContainer = Color(0xFFD6EAFF) // Light blue text on dark blue container
val DarkOnTertiary = Color(0xFF000000)       // Black text on bright amber
val DarkOnTertiaryContainer = Color(0xFFFFECB3) // Light amber on dark amber container
val DarkOnSurface = Color(0xFFEEEEEE)        // Off-white text on dark surfaces
val DarkOnSurfaceVariant = Color(0xFFBDBDBD) // Light gray on surface variant
val DarkOnBackground = Color(0xFFEEEEEE)     // Off-white text on background
val DarkOnError = Color(0xFF000000)          // Black text on error
val DarkOutline = Color(0xFF757575)          // Medium gray outline

/**
 * Specifies the custom light's colours that should be used through the application in a non-graphic
 * library specific amount.
 *
 * @author Amadou Iyawa
 */
data object CustomLightColorDefaults {
    internal const val SUCCESS = "#4CAF50"
    internal const val WARNING = "#FFC107"
    internal const val INFO = "#2196F3"
    internal const val DANGER = "#F44336"
    internal const val ONSUCCESS = WHITE
    internal const val ONWARNING = "#000000"
    internal const val ONINFO = WHITE
    internal const val ONDANGER = WHITE
}

/**
 * Specifies the custom dark's colours that should be used through the application in a non-graphic
 * library specific amount.
 *
 * @author Amadou Iyawa
 */
data object CustomDarkColorDefaults {
    internal const val SUCCESS = "#81C784"
    internal const val WARNING = "#FFD54F"
    internal const val INFO = "#64B5F6"
    internal const val DANGER = "#E57373"
    internal const val ONSUCCESS = BLACK
    internal const val ONWARNING = BLACK
    internal const val ONINFO = BLACK
    internal const val ONDANGER = BLACK
}

/**
 * Specifies the accent's colours that should be used through the application in a non-graphic
 * library specific amount.
 *
 * @author Amadou Iyawa
 */
data object AccentColorDefaults {
    internal const val PRIMARY = "#2196F3"
    internal const val WARNING = "#FFC107"
    internal const val NEUTRAL = "#808080"
    internal const val BACKGROUND = "#2F3135"
    internal const val SUCCESS = "#98FB98"
    internal const val ERROR = "#FF6961"
    internal const val LIGHT = "#87CEFA"
    internal const val BRIGHT = "#00BFFF"
}

/**
 * Data class representing custom light colors used in the application.
 *
 * @property success The color used to indicate success.
 * @property warning The color used to indicate a warning.
 * @property info The color used to indicate informational messages.
 * @property danger The color used to indicate danger or errors.
 * @property onSuccess The color used for text or icons on success backgrounds.
 * @property onWarning The color used for text or icons on warning backgrounds.
 * @property onInfo The color used for text or icons on informational backgrounds.
 * @property onDanger The color used for text or icons on danger backgrounds.
 * @author Amadou Iyawa
 */
data class CustomLightColor(
    override val success: Color = CustomLightColorDefaults.SUCCESS.toColor(),
    override val warning: Color = CustomLightColorDefaults.WARNING.toColor(),
    override val info: Color = CustomLightColorDefaults.INFO.toColor(),
    override val danger: Color = CustomLightColorDefaults.DANGER.toColor(),
    override val onSuccess: Color = CustomLightColorDefaults.ONSUCCESS.toColor(),
    override val onWarning: Color = CustomLightColorDefaults.ONWARNING.toColor(),
    override val onInfo: Color = CustomLightColorDefaults.ONINFO.toColor(),
    override val onDanger: Color = CustomLightColorDefaults.ONDANGER.toColor()
) : CustomColor

/**
 * Data class representing custom dark colors used in the application.
 *
 * @property success The color used to indicate success.
 * @property warning The color used to indicate a warning.
 * @property info The color used to indicate informational messages.
 * @property danger The color used to indicate danger or errors.
 * @property onSuccess The color used for text or icons on success backgrounds.
 * @property onWarning The color used for text or icons on warning backgrounds.
 * @property onInfo The color used for text or icons on informational backgrounds.
 * @property onDanger The color used for text or icons on danger backgrounds.
 * @author Amadou Iyawa
 */
data class CustomDarkColor(
    override val success: Color = CustomDarkColorDefaults.SUCCESS.toColor(),
    override val warning: Color = CustomDarkColorDefaults.WARNING.toColor(),
    override val info: Color = CustomDarkColorDefaults.INFO.toColor(),
    override val danger: Color = CustomDarkColorDefaults.DANGER.toColor(),
    override val onSuccess: Color = CustomDarkColorDefaults.ONSUCCESS.toColor(),
    override val onWarning: Color = CustomDarkColorDefaults.ONWARNING.toColor(),
    override val onInfo: Color = CustomDarkColorDefaults.ONINFO.toColor(),
    override val onDanger: Color = CustomDarkColorDefaults.ONDANGER.toColor()
) : CustomColor

/**
 * Data class representing accent colors used in the application.
 *
 * @property primary The primary accent color.
 * @property warning The color used to indicate warnings.
 * @property neutral The neutral color.
 * @property background The background color.
 * @property success The color used to indicate success.
 * @property error The color used to indicate errors.
 * @property light The light accent color.
 * @property bright The bright accent color.
 * @author Amadou Iyawa
 */
data class AccentColor(
    val primary: Color = AccentColorDefaults.PRIMARY.toColor(),
    val warning: Color = AccentColorDefaults.WARNING.toColor(),
    val neutral: Color = AccentColorDefaults.NEUTRAL.toColor(),
    val background: Color = AccentColorDefaults.BACKGROUND.toColor(),
    val success: Color = AccentColorDefaults.SUCCESS.toColor(),
    val error: Color = AccentColorDefaults.ERROR.toColor(),
    val light: Color = AccentColorDefaults.LIGHT.toColor(),
    val bright: Color = AccentColorDefaults.BRIGHT.toColor()
)