package com.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquearScreen(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    // Estado visual: Bloqueada (true) o Desbloqueada (false)
    var isLocked by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bloqueo de Barrera", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
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
                    .clip(CircleShape)
                    .background(
                        color = if (isLocked) Color(0xFF2A151A) else Color(0xFF152A1A)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isLocked) Color(0xFFFF3B30) else Color(0xFF34C759),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isLocked) "Barrera Bloqueada" else "Barrera Desbloqueada",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLocked)
                    "Barrera cerrada. Pulse para desbloquear y registrar acceso."
                else
                    "Barrera abierta. Pulse para bloquear de nuevo.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val nuevoEstado = !isLocked
                    isLocked = nuevoEstado

                    if (!nuevoEstado) {
                        db.collection("historial_accesos")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { documents ->
                                var nuevoTipo = "ENTRADA"

                                if (!documents.isEmpty) {
                                    val ultimoLog = documents.documents[0].toObject(AccesoFirestore::class.java)
                                    if (ultimoLog?.tipo == "ENTRADA") {
                                        nuevoTipo = "SALIDA"
                                    }
                                }

                                val nuevoLog = AccesoFirestore(
                                    emailUsuario = emailUsuario,
                                    tipo = nuevoTipo,
                                    timestamp = System.currentTimeMillis(),
                                    estadoBarrera = "Abierta"
                                )

                                db.collection("historial_accesos").add(nuevoLog)
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocked) Color(0xFF2563EB) else Color(0xFF2E3245)
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