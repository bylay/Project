package com.example.project.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object BarreraData {
    var isLocked by mutableStateOf(true)

    private val estadosPorUsuario = mutableStateMapOf<String, Boolean>()

    fun getEstado(email: String): Boolean {
        return estadosPorUsuario[email] ?: true
    }

    fun setEstado(email: String, isLocked: Boolean) {
        estadosPorUsuario[email] = isLocked
    }


}