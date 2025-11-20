package com.example.project.Composable

import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavController

@Composable
fun BottomBar(navController: NavHostController){
    BottomAppBar{
        Text("Home", modifier = Modifier.clickable{
            navController.navigate("principal")
        })
        Text("Buscar", modifier = Modifier.clickable{
            navController.navigate("segunda")
        })
        Text("Sensores")
    }
}