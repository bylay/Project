package com.example.project.screens

// Clase para Horarios
data class HorarioFirestore(
    var id: String = "",
    val userEmail: String = "",
    val nombre: String = "",
    val apertura: String = "",
    val cierre: String = "",
    val dias: String = "",
    val activo: Boolean = true
)

// Clase para Historial
data class AccesoFirestore(
    val emailUsuario: String = "",
    val tipo: String = "", // "ENTRADA" o "SALIDA"
    val timestamp: Long = 0,
    val estadoBarrera: String = ""
) {
    // Funciones auxiliares para fecha (igual que antes)
    fun getFechaGrupo(): String {
        val sdf = java.text.SimpleDateFormat("dd 'de' MMMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp)).uppercase()
    }
    fun getHoraFormateada(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    fun getFechaCompleta(): String {
        val sdf = java.text.SimpleDateFormat("dd 'de' MMMM, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

data class ConfiguracionFirestore(
    val distanciaActivacion: Int = 10
)

data class EstadoBarreraFirestore(
    val estado: String = "CERRADA", // "ABIERTA" o "CERRADA"
    val ultimoCambio: Long = 0,     // Timestamp del último movimiento
    val tiempoCierreAutomatico: Int = 10, // Segundos para cerrar (0 = desactivado)
    val error: String = ""          // Si hay algún error (ej: "Obstrucción")
)