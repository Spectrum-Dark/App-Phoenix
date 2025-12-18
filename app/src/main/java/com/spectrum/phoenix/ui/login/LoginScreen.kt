package com.spectrum.phoenix.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.login.LoginViewModel
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.ui.theme.DarkGray
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(loginState) {
        loginState?.let { result ->
            if (result.isSuccess) {
                val user = result.getOrNull()
                val name = user?.name ?: "Usuario"
                
                // GUARDAR SESION
                sessionManager.saveSession(name)
                
                Toast.makeText(context, "Bienvenido $name", Toast.LENGTH_SHORT).show()
                navController.navigate("main/$name") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    PhoenixTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Phoenix",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusBlue
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBlue,
                        cursorColor = FocusBlue,
                        focusedLeadingIconColor = FocusBlue,
                        focusedLabelColor = FocusBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Mostrar/Ocultar")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBlue,
                        cursorColor = FocusBlue,
                        focusedLeadingIconColor = FocusBlue,
                        focusedLabelColor = FocusBlue
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            loginViewModel.onLoginClicked(email, password)
                        } else {
                            Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("LOGIN", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { navController.navigate("register") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¿No tienes una cuenta? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Regístrate", color = FocusBlue, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = { /* TODO: Password recovery */ }) {
                    Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
