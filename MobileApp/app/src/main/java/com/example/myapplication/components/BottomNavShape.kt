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
    private val dockRadius: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = CornerRadius(0f, 0f),
                    topRightCornerRadius = CornerRadius(0f, 0f),
                    bottomRightCornerRadius = CornerRadius(0f, 0f),
                    bottomLeftCornerRadius = CornerRadius(0f, 0f)
                )
            )
            addOval(
                Rect(
                    Offset(size.width / 2 - dockRadius, -dockRadius),
                    Size(dockRadius * 2, dockRadius * 2)
                )
            )
            fillType = PathFillType.EvenOdd
        }
    )
}
