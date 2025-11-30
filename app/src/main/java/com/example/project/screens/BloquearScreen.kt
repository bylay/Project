package com.example.project.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.project.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquearScreen(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var estadoBarrera by remember { mutableStateOf(EstadoBarreraFirestore()) }
    var temporizadorRestante by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val docRef = db.collection("estado_barrera").document(emailUsuario)

    // esto "escucha" en tienpo real SNAPSHOT LISTENER
    DisposableEffect(emailUsuario) {
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val nuevoEstado = snapshot.toObject(EstadoBarreraFirestore::class.java)
                if (nuevoEstado != null) {
                    // Detectar cambio para notificación
                    if (estadoBarrera.estado == "CERRADA" && nuevoEstado.estado == "ABIERTA") {
                        NotificationHelper.enviarNotificacion(context, "Alerta de Seguridad", "La barrera se ha ABIERTO.")
                    }
                    if (nuevoEstado.error.isNotEmpty()) {
                        NotificationHelper.enviarNotificacion(context, "Error en Barrera", nuevoEstado.error)
                    }

                    estadoBarrera = nuevoEstado
                    isLoading = false
                }
            } else {
                // si no existe, se crea uno por defecto
                docRef.set(EstadoBarreraFirestore())
            }
        }
        onDispose { listener.remove() }
    }

    // temporizador
    LaunchedEffect(estadoBarrera.estado) {
        if (estadoBarrera.estado == "ABIERTA" && estadoBarrera.tiempoCierreAutomatico > 0) {
            temporizadorRestante = estadoBarrera.tiempoCierreAutomatico
            while (temporizadorRestante > 0) {
                delay(1000L) // Esperar 1 segundo
                temporizadorRestante--
            }
            // Si llega a 0, cerramos automáticamente
            if (estadoBarrera.estado == "ABIERTA") {
                docRef.update("estado", "CERRADA")
                NotificationHelper.enviarNotificacion(context, "Cierre Automático", "La barrera se cerró por temporizador.")
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Control en Tiempo Real", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F111A))
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val isAbierta = estadoBarrera.estado == "ABIERTA"
                val colorEstado = if (isAbierta) Color(0xFF34C759) else Color(0xFFFF3B30) // Verde o Rojo

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(colorEstado.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(colorEstado),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAbierta) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (isAbierta) "BARRERA ABIERTA" else "BARRERA CERRADA",
                    color = colorEstado,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Mostrar Temporizador si está abierta
                if (isAbierta && temporizadorRestante > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cierre automático en: ${temporizadorRestante}s",
                        color = Color(0xFFFFA500), // Naranja
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (estadoBarrera.error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = Color(0xFF3B1E1E), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Error: ${estadoBarrera.error}", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text("Tiempo de Cierre Automático:", color = Color.Gray, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = estadoBarrera.tiempoCierreAutomatico.toFloat(),
                        onValueChange = {
                            // Actualizar tiempo en Firebase (Deslizar)
                            docRef.update("tiempoCierreAutomatico", it.toInt())
                        },
                        valueRange = 0f..60f, // 0 a 60 segundos
                        steps = 59,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF4A80FF), activeTrackColor = Color(0xFF4A80FF))
                    )
                    Text(
                        text = "${estadoBarrera.tiempoCierreAutomatico}s",
                        color = Color.White,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val nuevoEstadoStr = if (isAbierta) "CERRADA" else "ABIERTA"

                        // 1. Actualizar Estado en Firebase
                        docRef.update("estado", nuevoEstadoStr)

                        // 2. Registrar Historial (Tu lógica de Entrada/Salida)
                        if (nuevoEstadoStr == "ABIERTA") {
                            db.collection("historial_accesos")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { documents ->
                                    var nuevoTipo = "ENTRADA"
                                    if (!documents.isEmpty) {
                                        val ultimoLog = documents.documents[0].toObject(AccesoFirestore::class.java)
                                        if (ultimoLog?.tipo == "ENTRADA") nuevoTipo = "SALIDA"
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
                        containerColor = if (isAbierta) Color(0xFF2E3245) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = if (isAbierta) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAbierta) "Cerrar Barrera" else "Abrir Barrera",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}