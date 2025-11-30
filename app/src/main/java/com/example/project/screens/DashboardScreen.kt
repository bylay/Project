package com.example.project.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.Navigator.AppScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DashboardScreen(navController: NavController, emailUsuario: String) {
    val db = FirebaseFirestore.getInstance()

    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    var textoTituloRecordatorio by remember { mutableStateOf("Buscando horarios...") }
    var textoDetalleRecordatorio by remember { mutableStateOf("...") }

    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDialogoDistancia by remember { mutableStateOf(false) }
    var distanciaSlider by remember { mutableFloatStateOf(10f) }

    LaunchedEffect(emailUsuario) {
        db.collection("usuarios").document(emailUsuario).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nombreUsuario = document.getString("nombre") ?: "Usuario"
                }
            }

        db.collection("horarios")
            .whereEqualTo("userEmail", emailUsuario)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    val horarios = documents.toObjects(HorarioFirestore::class.java)
                    val horarioActivo = horarios.find { it.activo }

                    if (horarioActivo != null) {
                        textoTituloRecordatorio = "Horario Activo: ${horarioActivo.nombre}"
                        textoDetalleRecordatorio = "Apertura: ${horarioActivo.apertura} - Cierre: ${horarioActivo.cierre}"
                    } else if (horarios.isNotEmpty()) {
                        textoTituloRecordatorio = "Sin horarios activos"
                        textoDetalleRecordatorio = "Activa tus horarios en la configuración."
                    } else {
                        textoTituloRecordatorio = "Configura tu Parking"
                        textoDetalleRecordatorio = "Aún no tienes horarios definidos."
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

        db.collection("configuracion").document(emailUsuario).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val config = document.toObject(ConfiguracionFirestore::class.java)
                    if (config != null) {
                        distanciaSlider = config.distanciaActivacion.toFloat()
                    }
                }
            }
    }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("P", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Smart Parking", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Box {
                    IconButton(onClick = { mostrarMenu = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Config", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = mostrarMenu,
                        onDismissRequest = { mostrarMenu = false },
                        modifier = Modifier.background(Color(0xFF1A1C29))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Distancia Activación", color = Color.White) },
                            onClick = {
                                mostrarMenu = false
                                mostrarDialogoDistancia = true
                            },
                            leadingIcon = { Icon(Icons.Default.SettingsInputAntenna, null, tint = Color(0xFF4A80FF)) }
                        )

                        HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)

                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = Color.White) },
                            onClick = {
                                mostrarMenu = false
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(AppScreen.Login.route) { popUpTo(0) { inclusive = true } }
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFEF4444)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 24.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bienvenido, $nombreUsuario", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Gestiona tu parking fácilmente.", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Surface(color = Color(0xFF152238), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFF1E3A8A)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null, tint = Color(0xFF4A80FF), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(textoTituloRecordatorio, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(textoDetalleRecordatorio, color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard("Bloqueo de Barrera", "Control manual", Icons.Rounded.DirectionsCar, Modifier.weight(1f)) {
                    navController.navigate(AppScreen.BloquearBarrera.crearRuta(emailUsuario))
                }
                DashboardCard("Configurar Horarios", "Define los horarios", Icons.Rounded.Today, Modifier.weight(1f)) {
                    navController.navigate(AppScreen.Horarios.crearRuta(emailUsuario))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                DashboardCard("Historial de Accesos", "Revisa entradas/salidas", Icons.Rounded.History, Modifier.weight(1f)) {
                    navController.navigate(AppScreen.HistorialAcceso.route)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (mostrarDialogoDistancia) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoDistancia = false },
                containerColor = Color(0xFF1A1C29),
                title = { Text("Distancia de Activación", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Define a qué distancia se activa la barrera automática.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${distanciaSlider.toInt()} metros",
                            color = Color(0xFF4A80FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = distanciaSlider,
                            onValueChange = { distanciaSlider = it },
                            valueRange = 0f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4A80FF),
                                activeTrackColor = Color(0xFF4A80FF),
                                inactiveTrackColor = Color.Gray
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nuevaConfig = ConfiguracionFirestore(distanciaActivacion = distanciaSlider.toInt())
                            db.collection("configuracion").document(emailUsuario).set(nuevaConfig)
                            mostrarDialogoDistancia = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoDistancia = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(color = Color(0xFF1A1C29), shape = RoundedCornerShape(16.dp), modifier = modifier.clickable { onClick() }.height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.padding(16.dp)) {
            ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF24293D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFF4A80FF))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}