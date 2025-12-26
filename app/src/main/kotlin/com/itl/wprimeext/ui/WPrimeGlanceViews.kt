package com.itl.wprimeext.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.absolutePadding
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.itl.wprimeext.R
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.roundToInt
import androidx.glance.unit.ColorProvider as UnitColorProvider

/**
 * Glance composable for W' display - follows CustomDoubleTypeView pattern
 */
@SuppressLint("RestrictedApi")
@Composable
fun WPrimeGlanceView(
    value: String,
    fieldLabel: String,
    backgroundColor: UnitColorProvider,
    textColor: UnitColorProvider = UnitColorProvider(Color.White),
    currentPower: Int,
    criticalPower: Int,
    wPrimeJoules: Double,
    anaerobicCapacity: Double,
    textSize: Int = 56,
    alignment: ViewConfig.Alignment = ViewConfig.Alignment.RIGHT,
    maxPowerDeltaForFullRotation: Int = 150,
    numberVerticalOffset: Int = 0,
    targetHeightFraction: Float = 0.5f,
    valueBottomExtraPadding: Int = 0,
    fixedCharCount: Int? = null,
    sizeScale: Float = 1f,
    showArrow: Boolean = true,
) {
    val (textAlign, horizontalAlignment) = when (alignment) {
        ViewConfig.Alignment.LEFT -> TextAlign.Start to Alignment.Start
        ViewConfig.Alignment.CENTER -> TextAlign.Center to Alignment.CenterHorizontally
        ViewConfig.Alignment.RIGHT -> TextAlign.End to Alignment.End
    }

    val safeCapacity = anaerobicCapacity.takeIf { it > 0 } ?: 1.0
    val wPrimeFraction = (wPrimeJoules / safeCapacity).toFloat().coerceIn(0f, 1f)

    val powerDelta = currentPower - criticalPower
    val wPrimeIsFull = wPrimeFraction >= 0.995f
    val isAtMaxWithLowPower = currentPower < criticalPower && wPrimeIsFull

    val rotationDegrees = if (isAtMaxWithLowPower) {
        0f
    } else {
        val rotationRatio = (powerDelta.toFloat() / maxPowerDeltaForFullRotation).coerceIn(-1f, 1f)
        ((if (powerDelta == 0) 0f else rotationRatio * 90f) / 15f).roundToInt() * 15f
    }

    // Dynamic text size calculation
    val currentWidgetSize = LocalSize.current
    val iconSizeDp = 28.dp
    val iconSidePaddingDp = 2.dp
    val arrowColWidthDp = iconSizeDp + iconSidePaddingDp * 2

    val sizingReservedHorizontal = if (showArrow) arrowColWidthDp else 0.dp

    val baseAutoSp = pickTextSizeSp(
        value = value,
        widgetWidth = currentWidgetSize.width,
        widgetHeight = currentWidgetSize.height,
        reservedHorizontal = sizingReservedHorizontal,
        maxSp = textSize,
        minSp = 24,
        targetHeightFraction = targetHeightFraction,
        fixedCharCount = fixedCharCount,
    )
    val autoTextSp = (baseAutoSp * sizeScale).toInt().coerceAtLeast(8)

    // Arrow placement logic
    val arrowOnLeft = (alignment == ViewConfig.Alignment.RIGHT)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(12.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Top,
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            TitleRow(fieldLabel, textAlign, horizontalAlignment, textColor)

            // Value area - Row with arrow column(s) and text column
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LEFT SPACER for CENTER alignment (symmetric with right arrow)
                if (alignment == ViewConfig.Alignment.CENTER && showArrow) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .padding(horizontal = iconSidePaddingDp)
                            .wrapContentSize()
                    ) {
                        // Empty spacer with icon size
                        Spacer(modifier = GlanceModifier.size(iconSizeDp))
                    }
                }

                // LEFT ARROW COLUMN (for RIGHT alignment)
                if (arrowOnLeft && showArrow) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .padding(horizontal = iconSidePaddingDp)
                            .wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val arrowDrawableRes = when (rotationDegrees.roundToInt()) {
                            -90 -> R.drawable.ic_direction_arrow_n90
                            -75 -> R.drawable.ic_direction_arrow_n75
                            -60 -> R.drawable.ic_direction_arrow_n60
                            -45 -> R.drawable.ic_direction_arrow_n45
                            -30 -> R.drawable.ic_direction_arrow_n30
                            -15 -> R.drawable.ic_direction_arrow_n15
                            0 -> R.drawable.ic_direction_arrow
                            15 -> R.drawable.ic_direction_arrow_p15
                            30 -> R.drawable.ic_direction_arrow_p30
                            45 -> R.drawable.ic_direction_arrow_p45
                            60 -> R.drawable.ic_direction_arrow_p60
                            75 -> R.drawable.ic_direction_arrow_p75
                            90 -> R.drawable.ic_direction_arrow_p90
                            else -> R.drawable.ic_direction_arrow
                        }

                        Image(
                            provider = ImageProvider(arrowDrawableRes),
                            contentDescription = "W' Trend",
                            modifier = GlanceModifier.size(iconSizeDp),
                            colorFilter = ColorFilter.tint(textColor),
                        )
                    }
                }

                // TEXT COLUMN (bottom-aligned)
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    contentAlignment = when(alignment) {
                        ViewConfig.Alignment.LEFT -> Alignment.BottomStart
                        ViewConfig.Alignment.RIGHT -> Alignment.BottomEnd
                        else -> Alignment.BottomCenter
                    }
                ) {
                    Text(
                        text = value,
                        style = TextStyle(
                            color = textColor,
                            fontSize = autoTextSp.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            textAlign = textAlign,
                        ),
                        maxLines = 1,
                    )
                }

                // RIGHT ARROW COLUMN (for LEFT/CENTER alignment)
                if (!arrowOnLeft && showArrow) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .padding(horizontal = iconSidePaddingDp)
                            .wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val arrowDrawableRes = when (rotationDegrees.roundToInt()) {
                            -90 -> R.drawable.ic_direction_arrow_n90
                            -75 -> R.drawable.ic_direction_arrow_n75
                            -60 -> R.drawable.ic_direction_arrow_n60
                            -45 -> R.drawable.ic_direction_arrow_n45
                            -30 -> R.drawable.ic_direction_arrow_n30
                            -15 -> R.drawable.ic_direction_arrow_n15
                            0 -> R.drawable.ic_direction_arrow
                            15 -> R.drawable.ic_direction_arrow_p15
                            30 -> R.drawable.ic_direction_arrow_p30
                            45 -> R.drawable.ic_direction_arrow_p45
                            60 -> R.drawable.ic_direction_arrow_p60
                            75 -> R.drawable.ic_direction_arrow_p75
                            90 -> R.drawable.ic_direction_arrow_p90
                            else -> R.drawable.ic_direction_arrow
                        }

                        Image(
                            provider = ImageProvider(arrowDrawableRes),
                            contentDescription = "W' Trend",
                            modifier = GlanceModifier.size(iconSizeDp),
                            colorFilter = ColorFilter.tint(textColor),
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun TitleRow(
    text: String,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal,
    textColor: UnitColorProvider,
) {
    Row(
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
            .padding(0.dp)
            .height(22.dp),
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_wprime_battery),
            contentDescription = "W' Icon",
            modifier = GlanceModifier.size(22.dp).absolutePadding(top = 4.dp),
        )
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign,
            ),
            modifier = GlanceModifier.padding(top = 6.dp),
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun WPrimeNotAvailableGlanceView(
    message: String = "N/A",
    isKaroo3: Boolean = true,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .let { if (isKaroo3) it.cornerRadius(12.dp) else it.cornerRadius(0.dp) }
            .padding(1.dp),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Gray)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Text(
                text = message,
                style = TextStyle(
                    color = UnitColorProvider(Color.White),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

// Helper functions for dynamic text sizing
private const val CHAR_WIDTH_FACTOR = 0.62f
private const val LINE_HEIGHT_FACTOR = 1.2f

private fun pickTextSizeSp(
    value: String,
    widgetWidth: Dp,
    widgetHeight: Dp,
    reservedHorizontal: Dp,
    maxSp: Int,
    minSp: Int = 24,
    targetHeightFraction: Float = 0.5f,
    fixedCharCount: Int? = null,
): Int {
    val safeMax = if (maxSp < minSp) minSp else maxSp
    if (widgetWidth == Dp.Unspecified ||
        widgetHeight == Dp.Unspecified ||
        widgetWidth.value <= 0f ||
        widgetHeight.value <= 0f
    ) {
        return safeMax
    }

    // Subtract reserved space (arrow) only once
    val availW = (widgetWidth - reservedHorizontal - 4.dp).coerceAtLeast(0.dp).value

    val titleRowHeight = 22.dp
    val verticalMargins = 8.dp
    val availH = (widgetHeight - titleRowHeight - verticalMargins).coerceAtLeast(0.dp).value
    if (availW <= 0f || availH <= 0f) {
        return safeMax
    }

    val targetChars = fixedCharCount ?: value.length
    val avgUnitPerChar = 1.0f
    val units = targetChars * avgUnitPerChar

    val fromWidth = if (units * CHAR_WIDTH_FACTOR > 0) (availW / (units * CHAR_WIDTH_FACTOR)) else safeMax.toFloat()
    val adjustedFraction = targetHeightFraction.coerceIn(0.3f, 0.85f)
    val fromHeight = (availH * adjustedFraction) / LINE_HEIGHT_FACTOR
    val raw = fromWidth.coerceAtMost(fromHeight)
    val clamped = raw.coerceIn(minSp.toFloat(), safeMax.toFloat())

    if (fixedCharCount != null) return clamped.toInt()

    val steps = listOf(64, 56, 50, 46, 42, 38, 34, 32, 30, 28, 26, 24)
    val stepped = steps.firstOrNull { clamped >= it && it <= safeMax } ?: steps.last { it <= safeMax }
    return stepped
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 420, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview() {
    WPrimeGlanceView(
        value = "12.3",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.DarkGray),
        currentPower = 250,
        criticalPower = 200,
        wPrimeJoules = 8000.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
    )
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview_Recovering() {
    WPrimeGlanceView(
        value = "18.5",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.hsl(100f, 0.3f, 0.4f)),
        currentPower = 150,
        criticalPower = 200,
        wPrimeJoules = 10800.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
    )
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview_FullNoArrow() {
    WPrimeGlanceView(
        value = "11438",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.hsl(120f, 0.5f, 0.5f)),
        currentPower = 100,
        criticalPower = 200,
        wPrimeJoules = 12000.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
    )
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview_MaxEffort() {
    WPrimeGlanceView(
        value = "3789",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.Red),
        currentPower = 380,
        criticalPower = 200,
        wPrimeJoules = 2000.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
    )
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview_Neutral() {
    WPrimeGlanceView(
        value = "1580",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.Gray),
        currentPower = 200,
        criticalPower = 200,
        wPrimeJoules = 9000.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
    )
}

@Suppress("unused")
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun WPrimeGlanceViewPreview_NoArrow_NoColors() {
    WPrimeGlanceView(
        value = "1580",
        fieldLabel = "W' (kJ)",
        backgroundColor = UnitColorProvider(Color.White),
        textColor = UnitColorProvider(Color.Black),
        currentPower = 200,
        criticalPower = 200,
        wPrimeJoules = 9000.0,
        anaerobicCapacity = 12000.0,
        textSize = 50,
        alignment = ViewConfig.Alignment.CENTER,
        maxPowerDeltaForFullRotation = 150,
        showArrow = false,
    )
}

