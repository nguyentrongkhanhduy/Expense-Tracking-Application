package com.example.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.White

@Composable
fun CustomSegmentedTabRow(
    tabTexts: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFE5E7EB), RoundedCornerShape(20.dp)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabTexts.forEachIndexed { idx, txt ->
            Button(
                onClick = { onTabSelected(idx) },
                shape = when (idx) {
                    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    tabTexts.lastIndex -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    else -> RoundedCornerShape(0.dp)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == idx) PrimaryBlue else Color.Transparent,
                    contentColor = if (selectedTab == idx) White else PrimaryBlue
                ),
                border = if (selectedTab == idx) null else ButtonDefaults.outlinedButtonBorder,
                elevation = null,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = txt,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (selectedTab == idx) White else PrimaryBlue
                )
            }
        }
    }
}