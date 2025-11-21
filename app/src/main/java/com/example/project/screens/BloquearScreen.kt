package com.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.ParkingDatabase
import kotlinx.coroutines.launch
import com.example.project.AccesoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquearScreen(navController: NavController, emailUsuario: String) {
    var isLocked = BarreraData.getEstado(emailUsuario)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { ParkingDatabase.getDatabase(context) }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bloqueo de Barrera", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F111A))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = if (isLocked) Color(0xFF2A151A) else Color(0xFF152A1A), // rojo oscuro o verde oscuro
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isLocked) Color(0xFFFF3B30) else Color(0xFF34C759), // rojo brillante o verde brillante
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLocked)
                    "Cuando la barrera está bloqueada, no se puede abrir ni cerrar desde la aplicación o de forma remota."
                else
                    "La barrera está operativa. Puedes controlarla manualmente o mediante los horarios automáticos.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val nuevoEstado = !isLocked
                    isLocked = nuevoEstado

                    if (!nuevoEstado) {
                        scope.launch {
                            val ultimoLog = db.dao().getLastAccesolog()

                            val nuevoTipo = if (ultimoLog?.tipo == "ENTRADA") "SALIDA" else "ENTRADA"

                            val nuevoLog = AccesoEntity(
                                emailUsuario = emailUsuario,
                                tipo = nuevoTipo,
                                timestamp = System.currentTimeMillis(),
                                estadoBarrera = "Abierta"
                            )
                            db.dao().insertAccesolog(nuevoLog)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocked) Color(0xFF2563EB) else Color(0xFF1E2230) // azul o gris
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLocked) "Desbloquear Barrera" else "Bloquear Barrera",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}