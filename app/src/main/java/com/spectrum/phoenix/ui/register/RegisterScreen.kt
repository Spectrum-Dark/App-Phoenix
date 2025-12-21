package com.spectrum.phoenix.ui.register

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spectrum.phoenix.logic.register.RegisterViewModel
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, registerViewModel: RegisterViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val toast = LocalToastController.current
    
    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val isFormValid = name.isNotEmpty() && isEmailValid && passwordsMatch && password.length >= 6

    val registrationState by registerViewModel.registrationState.collectAsStateWithLifecycle()

    LaunchedEffect(registrationState) {
        registrationState?.let { result ->
            if (result.isSuccess) {
                toast.show("Registro exitoso", ToastType.SUCCESS)
                navController.popBackStack()
            } else {
                toast.show(result.exceptionOrNull()?.message ?: "Error al registrar", ToastType.ERROR)
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
                Box(modifier = Modifier.size(64.dp).background(FocusBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PersonAdd, null, tint = FocusBlue, modifier = Modifier.size(28.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Crear Cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("Completa los datos corporativos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, leadingIcon = { Icon(Icons.Default.Badge, null, tint = FocusBlue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = if(isEmailValid || email.isEmpty()) FocusBlue else Color.Red) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true, isError = !isEmailValid && email.isNotEmpty())
                
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña (mín. 6)") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = FocusBlue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(20.dp)) } })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar Contraseña") }, leadingIcon = { Icon(Icons.Default.VerifiedUser, null, tint = if(passwordsMatch || confirmPassword.isEmpty()) FocusBlue else Color.Red) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), isError = !passwordsMatch && confirmPassword.isNotEmpty(), visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(20.dp)) } })

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { if (isFormValid) registerViewModel.onRegisterClicked(name, email, password) }, enabled = isFormValid, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isFormValid) FocusBlue else MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("CREAR CUENTA", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Row {
                        Text("¿Ya eres parte de Phoenix? ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Inicia sesión", color = FocusBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
