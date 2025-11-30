package com.example.project.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.Navigator.AppScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    // Firebase
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Estados
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // Para mostrar carga

    Scaffold(containerColor = Color(0xFF0F111A)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium, color = Color(0xFF1E2A45), modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Park, contentDescription = "Logo", tint = Color(0xFF4A80FF), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(if (isRegistering) "Crear Cuenta" else "Bienvenido", fontSize = 24.sp, color = Color.White)

            if (isRegistering) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors() // Tu función de colores
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true

                        if (isRegistering) {
                            // 1. REGISTRO EN FIREBASE
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Guardar el nombre en Firestore (Base de datos)
                                        val userMap = hashMapOf("nombre" to nombre, "email" to email)
                                        db.collection("usuarios").document(email).set(userMap)

                                        Toast.makeText(context, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                                        isRegistering = false // Volver al login
                                    } else {
                                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                        } else {
                            // 2. LOGIN EN FIREBASE
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Ir al Dashboard
                                        navController.navigate(AppScreen.Dashboard.crearRuta(email)) {
                                            popUpTo(AppScreen.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Error de Login", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isRegistering) "Registrarse" else "Acceder", color = Color.White)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isRegistering) "¿Ya tienes cuenta? " else "¿No tienes una cuenta? ",
                    color = Color.Gray, fontSize = 14.sp
                )
                Text(
                    text = if (isRegistering) "Inicia Sesión" else "Regístrate",
                    color = Color(0xFF4A80FF),
                    modifier = Modifier.clickable { isRegistering = !isRegistering }
                )
            }
        }
    }
}

// esto es para no repetir los colores
@Composable
fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1A1C29),
    unfocusedContainerColor = Color(0xFF1A1C29),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedIndicatorColor = Color(0xFF4A80FF),
    unfocusedIndicatorColor = Color.Gray
)

// validacion para el email
fun isEmailValid(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}