package com.example.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.White


@Composable
fun CustomBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val barHeight = 72.dp // Increased height for label visibility
    val dockRadiusDp = 36.dp // Adjust cutout for FAB size
    val dockRadius = with(LocalDensity.current) { dockRadiusDp.toPx() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(BottomNavShape(
                cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                dockRadius = with(LocalDensity.current) { 24.dp.toPx() }
            ))
            .border(
                width = 2.dp,
                color = PrimaryBlue,
                shape = BottomNavShape(
                    cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                    dockRadius = with(LocalDensity.current) { 28.dp.toPx() }
                )
            )

            .background(White)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = true,
                            radius = 48.dp,
                            color = PrimaryBlue
                        )
                    ) { onTabSelected(0) }
            ) {
                Icon(
                    painterResource(R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(32.dp),
                    tint = if (selectedTab == 0) PrimaryBlue else Color.Gray
                )
                Text(
                    "Home",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTab == 0) PrimaryBlue else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Transaction list tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = true,
                            radius = 48.dp,
                            color = PrimaryBlue
                        )
                    ) { onTabSelected(1) }
            ) {
                Icon(
                    painterResource(R.drawable.list),
                    contentDescription = "Transaction list",
                    modifier = Modifier.size(32.dp),
                    tint = if (selectedTab == 1) PrimaryBlue else Color.Gray
                )
                Text(
                    "Transactions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTab == 1) PrimaryBlue else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.weight(1f))
            // Analytics tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = true,
                            radius = 48.dp,
                            color = PrimaryBlue
                        )
                    ) { onTabSelected(2) }
            ) {
                Icon(
                    painterResource(R.drawable.barchart),
                    contentDescription = "Analytics",
                    modifier = Modifier.size(32.dp),
                    tint = if (selectedTab == 2) PrimaryBlue else Color.Gray
                )
                Text(
                    "Analytics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTab == 2) PrimaryBlue else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Profile tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = true,
                            radius = 48.dp,
                            color = PrimaryBlue
                        )
                    ) { onTabSelected(3) }
            ) {
                Icon(
                    painterResource(R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                    tint = if (selectedTab == 3) PrimaryBlue else Color.Gray
                )
                Text(
                    "Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTab == 3) PrimaryBlue else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
