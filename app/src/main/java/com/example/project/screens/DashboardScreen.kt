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

    LaunchedEffect(emailUsuario) {
        // obtener nombre desde la colección "usuarios"
        db.collection("usuarios").document(emailUsuario).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nombreUsuario = document.getString("nombre") ?: "Usuario"
                }
            }

        // obtener horarios desde la colección "horarios"
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
                } catch (e: Exception) {
                    // SI FALLA, NO CRASHEA, SOLO AVISA
                    textoTituloRecordatorio = "Error de datos"
                    textoDetalleRecordatorio = "Revisa tu conexión o base de datos."
                    e.printStackTrace()
                }
            }
            .addOnFailureListener {
                textoTituloRecordatorio = "Error de red"
                textoDetalleRecordatorio = "No se pudieron cargar los datos."
            }
    }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("P", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Smart Parking", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                // Menú Cerrar Sesión
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
                            text = { Text("Cerrar Sesión", color = Color.White) },
                            onClick = {
                                mostrarMenu = false
                                FirebaseAuth.getInstance().signOut() // Cerrar en Firebase
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

            // Tarjeta Recordatorio
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

            // Botones de navegación
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Bloqueo de Barrera",
                    subtitle = "Control manual",
                    icon = Icons.Rounded.DirectionsCar,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(AppScreen.BloquearBarrera.crearRuta(emailUsuario))
                    }
                )
                DashboardCard(
                    title = "Configurar Horarios",
                    subtitle = "Define los horarios",
                    icon = Icons.Rounded.Today,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(AppScreen.Horarios.crearRuta(emailUsuario))
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                DashboardCard(
                    title = "Historial de Accesos",
                    subtitle = "Revisa entradas/salidas",
                    icon = Icons.Rounded.History,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(AppScreen.HistorialAcceso.route)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Surface(
        color = Color(0xFF1A1C29),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clickable { onClick() }
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF24293D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF4A80FF)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            content()
        }
    }
}
