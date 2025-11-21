package com.example.project.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.Navigator.AppScreen
import com.example.project.ParkingDatabase

@Composable
fun DashboardScreen(navController: NavController, emailUsuario: String) {

    val context = LocalContext.current
    val db = remember { ParkingDatabase.getDatabase(context) }

    // estado para que se guarde el nombre del usuario que viene de la bd
    var nombreUsuario by remember { mutableStateOf("Cargando...") }

    // recordatorio
    var textoTituloRecordatorio by remember { mutableStateOf("Buscando horarios...") }
    var textoDetalleRecordatorio by remember { mutableStateOf("...") }

    LaunchedEffect(emailUsuario) {
        val nombre = db.dao().getUsuario(emailUsuario)
        if (nombre != null) {
            nombreUsuario = nombre
        }

        val horarios = db.dao().getUserHorarios(emailUsuario)

        val horarioActivo = horarios.find { it.activo }

        // si hay
        if (horarioActivo != null) {
            textoTituloRecordatorio = "Horario Activo: ${horarioActivo.nombre}"
            textoDetalleRecordatorio = "Apertura: ${horarioActivo.apertura} - Cierre: ${horarioActivo.cierre}"
        }
        // hay pero apagados
        else if (horarios.isNotEmpty()) {
            textoTituloRecordatorio = "Sin horarios activos"
            textoDetalleRecordatorio = "Activa tus horarios en la configuración."
        }
        // no hay ninguno
        else {
            textoTituloRecordatorio = "Configura tu Parking"
            textoDetalleRecordatorio = "Aún no tienes horarios definidos."
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "P",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Smart Parking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Configuración",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenido, $nombreUsuario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Gestiona tu parking fácilmente.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color(0xFF152238),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E3A8A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF4A80FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = textoTituloRecordatorio,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = textoDetalleRecordatorio,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Bloqueo de Barrera",
                    subtitle = "Control manual",
                    icon = Icons.Rounded.DirectionsCar,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppScreen.BloquearBarrera.crearRuta(emailUsuario)) }
                )


                DashboardCard(
                    title = "Configurar Horarios",
                    subtitle = "Define los horarios de acceso",
                    icon = Icons.Rounded.Today,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppScreen.Horarios.crearRuta(emailUsuario)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Historial de Accesos",
                    subtitle = "Revisa las entradas y salidas",
                    icon = Icons.Rounded.History,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppScreen.HistorialAcceso.route) }
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
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
