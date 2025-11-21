package com.example.project.screens

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.HorariosEntity
import com.example.project.ParkingDatabase
import kotlinx.coroutines.launch
import java.util.Calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(navController: NavController, userEmail: String) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { ParkingDatabase.getDatabase(context) }

    val listaHorarios = remember { mutableStateListOf<HorariosEntity>() }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaApertura by remember { mutableStateOf("09:00 AM") }
    var nuevaCierre by remember { mutableStateOf("05:00 PM") }
    val nuevosDias = remember { mutableStateListOf<String>() }

    LaunchedEffect(userEmail) {
        val horariosGuardados = db.dao().getUserHorarios(userEmail)
        listaHorarios.clear()
        listaHorarios.addAll(horariosGuardados)
    }

    Scaffold(
        containerColor = Color(0xFF0F111A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Horarios de Barrera", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Ayuda",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(
                        0xFF0F111A))
            )
        },

        floatingActionButton = {
            if (!mostrarFormulario) {
                FloatingActionButton(
                    onClick = {mostrarFormulario = true}, // click y se abre la ventana
                    containerColor = Color(0xFF4A80FF),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Horario")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = if (mostrarFormulario) 400.dp else 80.dp)
            ) {
                listaHorarios.forEach { horario ->
                    HorarioItem(
                        horario = horario,
                        onToggle = {nuevoEstado ->
                            scope.launch {
                                val itemActualizado = horario.copy(activo = nuevoEstado)
                                db.dao().updateHorario(itemActualizado)

                                val actualizados = db.dao().getUserHorarios(userEmail)
                                listaHorarios.clear()
                                listaHorarios.addAll(actualizados)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (mostrarFormulario) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { mostrarFormulario = false } // se cierra si se toca afuera
                )

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color(0xFF1A1C29),
                    shadowElevation = 16.dp
                ) {
                    // se usa el clickable para evitar que los clicks pasen hacia el fondo
                    Column(modifier = Modifier.padding(24.dp).clickable(enabled = false) {}) {

                        Box(
                            modifier = Modifier.width(40.dp).height(4.dp)
                                .clip(RoundedCornerShape(2.dp)).background(Color.DarkGray)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nuevo Horario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        // input nombre
                        Text("Nombre (Opcional)", color = Color.Gray, fontSize = 12.sp)
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            placeholder = { Text("Ej: Turno Noche", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF24293D),
                                unfocusedContainerColor = Color(0xFF24293D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color(0xFF4A80FF),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // inputs de horas
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TimePickerInput(
                                label = "Hora de Apertura",
                                timeValue = nuevaApertura,
                                onTimeChange = { nuevaApertura = it },
                                modifier = Modifier.weight(1f)
                            )
                            TimePickerInput(
                                label = "Hora de Cierre",
                                timeValue = nuevaCierre,
                                onTimeChange = { nuevaCierre = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // selector de dias
                        Text("Repetir en días", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        WeekDaySelector(selectedDays = nuevosDias)

                        Spacer(modifier = Modifier.height(24.dp))

                        // botones
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    // cancelar y cerrar ventana
                                    mostrarFormulario = false
                                    nuevoNombre = ""
                                    nuevosDias.clear()
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    // guardar
                                    val nombreFinal = nuevoNombre.ifEmpty { "Personalizado" }
                                    val nuevoHorario = HorariosEntity(
                                        userEmail = userEmail,
                                        nombre = nombreFinal,
                                        apertura = nuevaApertura,
                                        cierre = nuevaCierre,
                                        dias = nuevosDias.joinToString(","),
                                        activo = true
                                    )
                                    scope.launch {
                                        db.dao().insertHorario(nuevoHorario)
                                        val actualizados = db.dao().getUserHorarios(userEmail)
                                        listaHorarios.clear()
                                        listaHorarios.addAll(actualizados)
                                    }

                                    // se limpia el campo y se cierra la ventana
                                    nuevoNombre = ""
                                    nuevosDias.clear()
                                    mostrarFormulario = false
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Guardar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorarioItem(
    horario: HorariosEntity,
    onDelete: () -> Unit = {},
    onToggle: (Boolean) -> Unit
) {
    var isActive by remember { mutableStateOf(horario.activo) }

    Surface(
        color = Color(0xFF151722),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E2230), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF4A80FF))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(horario.nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Abre: ${horario.apertura} - Cierra: ${horario.cierre} | ${horario.dias}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = horario.activo,
                onCheckedChange = { isChecked ->
                    onToggle(isChecked)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4A80FF),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun TimePickerInput(
    label: String,
    timeValue: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, hour: Int, minute: Int ->
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            onTimeChange(String.format("%02d:%02d %s", displayHour, minute, amPm))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        OutlinedTextField(
            value = timeValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() },
            enabled = false,
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color(0xFF24293D),
                disabledTextColor = Color.White,
                disabledIndicatorColor = Color.Gray,
                disabledTrailingIconColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun WeekDaySelector(selectedDays: SnapshotStateList<String>) {
    val days = listOf("D", "L", "M", "X", "J", "V", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            val backgroundColor = if (isSelected) Color(0xFF4A80FF) else Color(0xFF24293D)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable {
                        if (isSelected) selectedDays.remove(day)
                        else selectedDays.add(day)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(day, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
