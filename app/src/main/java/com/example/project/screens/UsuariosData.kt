package com.example.project.screens

data class Usuario(
    val nombre: String,
    val email: String,
    val password: String
)

object UsuariosData {
    val usuarios = mutableListOf<Usuario>()

    fun register(usuario: Usuario): Boolean {
        if (usuarios.any { it.email == usuario.email }) return false
        usuarios.add(usuario)
        return true
    }

    fun login(email: String, password: String): Usuario? {
        return usuarios.find { it.email == email && it.password == password }
    }
}
