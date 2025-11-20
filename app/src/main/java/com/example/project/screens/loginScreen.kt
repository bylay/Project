package com.example.project.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.project.Navigator.AppScreen

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    // Estados del formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") } // Nuevo campo para registro

    // Estado para controlar si estamos en Login o Registro
    var isRegistering by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F111A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- LOGO ---
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF1E2A45),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Park, contentDescription = "Logo", tint = Color(0xFF4A80FF), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TÍTULOS DINÁMICOS ---
            Text(
                text = if (isRegistering) "Crear Cuenta" else "Bienvenido de Nuevo",
                fontSize = 24.sp, color = Color.White, style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRegistering) "Regístrate para empezar." else "Inicia sesión para encontrar tu espacio.",
                fontSize = 14.sp, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- CAMPO NOMBRE (Solo visible en Registro) ---
            if (isRegistering) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- CAMPO EMAIL ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMPO PASSWORD ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            // Olvidaste contraseña (Solo en Login)
            if (!isRegistering) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { }) {
                        Text("¿Olvidaste tu contraseña?", color = Color(0xFF4A80FF))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp)) // Espacio extra en registro
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÓN DE ACCIÓN (Login o Registro) ---
            Button(
                onClick = {
                    if (isRegistering) {
                        // LÓGICA DE REGISTRO
                        if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            val success = UsuariosData.register(Usuario(nombre, email, password))
                            if (success) {
                                Toast.makeText(context, "Registro exitoso. ¡Inicia sesión!", Toast.LENGTH_SHORT).show()
                                isRegistering = false // Volver a pantalla de login
                            } else {
                                Toast.makeText(context, "El correo ya está registrado.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // LÓGICA DE LOGIN
                        val user = UsuariosData.login(email, password)
                        if (user != null) {
                            // Navegar al Dashboard pasando el nombre real
                            navController.navigate(AppScreen.Dashboard.crearRuta(user.nombre)) {
                                popUpTo(AppScreen.Login.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (isRegistering) "Registrarse" else "Acceder", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMBIAR MODO (Texto inferior) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isRegistering) "¿Ya tienes cuenta? " else "¿No tienes una cuenta? ",
                    color = Color.Gray, fontSize = 14.sp
                )
                Text(
                    text = if (isRegistering) "Inicia Sesión" else "Regístrate",
                    color = Color(0xFF4A80FF),
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.clickable {
                        // Limpiar campos y cambiar modo
                        isRegistering = !isRegistering
                        email = ""
                        password = ""
                        nombre = ""
                    }
                )
            }
        }
    }
}

// Función auxiliar para no repetir colores
@Composable
fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1A1C29),
    unfocusedContainerColor = Color(0xFF1A1C29),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedIndicatorColor = Color(0xFF4A80FF),
    unfocusedIndicatorColor = Color.Gray
)