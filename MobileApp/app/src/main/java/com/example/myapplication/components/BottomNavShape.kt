package com.example.myapplication.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.RoundRect

class BottomNavShape(
    private val cornerRadius: Float,
    private val dockRadius: Float,
    private val dockVerticalOffset: Float = 8f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        Path().apply {
            // Main bar with rounded top corners
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeftCornerRadius = CornerRadius(0f, 0f),
                    bottomRightCornerRadius = CornerRadius(0f, 0f)
                )
            )
            // Cutout: a semicircle, slightly deeper than before
            val fabCenterX = size.width / 2
            val fabCenterY = -dockRadius + dockVerticalOffset // Dips into the bar
            addOval(
                Rect(
                    Offset(fabCenterX - dockRadius, fabCenterY),
                    Size(dockRadius * 2, dockRadius * 2)
                )
            )
            fillType = PathFillType.EvenOdd
        }
    )
}
