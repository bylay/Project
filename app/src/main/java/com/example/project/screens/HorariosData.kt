package com.example.project.screens

import androidx.compose.runtime.mutableStateListOf
data class HorarioData(
    val nombre: String,
    val apertura: String,
    val cierre: String,
    val dias: List<String>,
    var activo: Boolean = true
)

object HorariosManager {
    val listaHorarios = mutableStateListOf(
        HorarioData("Horario Laboral", "08:00 AM", "06:00 PM", listOf("L", "M", "X", "J", "V")),
        HorarioData("Fin de Semana", "10:00 AM", "08:00 PM", listOf("S", "D"), false)
    )
}