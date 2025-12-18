package com.spectrum.phoenix.ui.login

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.login.LoginViewModel
import com.spectrum.phoenix.logic.session.SessionManager
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val toast = LocalToastController.current
    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val isFormValid = isEmailValid && password.isNotEmpty()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        loginState?.let { result ->
            if (result.isSuccess) {
                val user = result.getOrNull()
                sessionManager.saveSession(user?.userId ?: "", user?.name ?: "Usuario", user?.email ?: "")
                toast.show("¡Bienvenido, ${user?.name}!", ToastType.SUCCESS)
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            } else {
                toast.show(result.exceptionOrNull()?.message ?: "Error al acceder", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.size(70.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LockPerson, null, tint = FocusBlue, modifier = Modifier.size(32.dp))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Text("Inicia Sesión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("Acceso Phoenix Enterprise System", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = if (isEmailValid || email.isEmpty()) FocusBlue else Color.Red) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = !isEmailValid && email.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = FocusBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(20.dp))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (isFormValid) loginViewModel.onLoginClicked(email, password) },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = { navController.navigate("register") }) {
                    Row {
                        Text("¿Sin cuenta? ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Regístrate aquí", color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
