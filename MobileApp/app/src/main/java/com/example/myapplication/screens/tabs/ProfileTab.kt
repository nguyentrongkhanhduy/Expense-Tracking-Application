package com.example.myapplication.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileTab(
    navController: NavController,
    onChangeCurrency: () -> Unit = {},
    onSyncData: () -> Unit = {},
    onBackupData: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: "Username"
    val email = user?.email ?: "Username@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A2540),
            modifier = Modifier.padding(bottom = 18.dp)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(6.dp, Color(0xFF222B45), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Image",
                tint = Color(0xFF222B45),
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            if (user != null) displayName else "Guest",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (user != null) {
            Text(
                text = email,
                fontSize = 16.sp,
                color = Color(0xFF222B45)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Currency preference: CAD",
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onChangeCurrency,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Change", fontSize = 14.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        MyButton(onClick = { navController.navigate("categories?fromTab=3") }) {
            Text(
                "Manage categories",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (user != null) {
            MyButton(onClick = onSyncData) {
                Text("Sync data", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            MyButton(onClick = onBackupData) {
                Text(
                    "Back up data",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            MyButton(onClick = onLogout, backgroundColor = PrimaryRed) {
                Text("Log out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            MyButton(onClick = { navController.navigate("login") }) {
                Text("Log in", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
