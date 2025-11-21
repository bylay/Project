package com.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.AccesoEntity
import com.example.project.ParkingDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAccesoScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { ParkingDatabase.getDatabase(context) }

    var listaLogs by remember { mutableStateOf(emptyList<AccesoEntity>()) }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todo") }

    LaunchedEffect(Unit) {
        listaLogs = db.dao().getAllAccesolog()
    }

    val registrosFiltrados = listaLogs.filter { log ->
        val coincideTexto = log.emailUsuario.contains(textoBusqueda, ignoreCase = true)
        val coincideTipo = when (filtroSeleccionado) {
            "Entradas" -> log.tipo == "ENTRADA"
            "Salidas" -> log.tipo == "SALIDA"
            else -> true
        }
        coincideTexto && coincideTipo
    }.groupBy { it.getFechaGrupo() }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial de Entradas y Salidas", color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F111A))
            )
        }
    ){ innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar usuario...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1C29),
                    unfocusedContainerColor = Color(0xFF1A1C29),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF4A80FF),
                    unfocusedIndicatorColor = Color(0xFF2E3245)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val filtros = listOf("Todo", "Entradas", "Salidas")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtros) { filtro ->
                    FilterChipButton(text = filtro, isSelected = filtroSeleccionado == filtro) {
                        filtroSeleccionado = filtro
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                registrosFiltrados.forEach { (fecha, logs) ->
                    item {
                        Text(fecha, color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    items(logs) { log ->
                        HistorialItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialItem(log: AccesoEntity) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        val esEntrada = log.tipo == "ENTRADA"
        val colorIcono = if (esEntrada) Color(0xFF34C759) else Color(0xFFFF9500)

        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colorIcono.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ){
            Icon(
                if (esEntrada) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = colorIcono
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(if (esEntrada) "Entrada Detectada" else "Salida Registrada", color = Color.White, fontSize = 16.sp)
            Text(log.emailUsuario, color = Color.LightGray, fontSize = 12.sp)
            Text("${log.getHoraFormateada()} - ${log.getFechaCompleta()}", color = Color.Gray, fontSize = 12.sp)
        }

        Surface(
            color = Color(0xFF152A1A),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34C759).copy(alpha = 0.3f))
        ) {
            Text("Barrera Abierta", color = Color(0xFF34C759), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun FilterChipButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF1A1C29)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}

