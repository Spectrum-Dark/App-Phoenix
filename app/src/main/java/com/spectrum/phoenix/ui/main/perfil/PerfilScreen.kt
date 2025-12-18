package com.spectrum.phoenix.ui.main.perfil

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
import com.spectrum.phoenix.logic.session.PerfilViewModel
import com.spectrum.phoenix.ui.components.LocalToastController
import com.spectrum.phoenix.ui.components.ToastType
import com.spectrum.phoenix.ui.theme.FocusBlue
import com.spectrum.phoenix.ui.theme.PhoenixTheme

@Composable
fun PerfilScreen(perfilViewModel: PerfilViewModel = viewModel()) {
    val toast = LocalToastController.current
    val currentName by perfilViewModel.userName.collectAsStateWithLifecycle()
    val userEmail by perfilViewModel.userEmail.collectAsStateWithLifecycle()
    val result by perfilViewModel.updateResult.collectAsStateWithLifecycle()

    var nameState by remember(currentName) { mutableStateOf(currentName) }
    var passwordState by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        result?.let {
            if (it.isSuccess) {
                toast.show(it.getOrNull() ?: "Perfil actualizado", ToastType.SUCCESS)
                passwordState = ""
                perfilViewModel.clearResult()
            } else {
                toast.show(it.exceptionOrNull()?.message ?: "Error al actualizar", ToastType.ERROR)
            }
        }
    }

    PhoenixTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(FocusBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(FocusBlue), contentAlignment = Alignment.Center) {
                    Text(text = currentName.take(1).uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(currentName, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(userEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)), border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("GESTIÓN DE PERFIL", fontWeight = FontWeight.Black, fontSize = 11.sp, color = FocusBlue, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(value = nameState, onValueChange = { nameState = it }, label = { Text("Nombre Completo") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = FocusBlue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = passwordState, onValueChange = { passwordState = it }, label = { Text("Cambiar Contraseña (opcional)") }, placeholder = { Text("Dejar vacío para no cambiar") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = FocusBlue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } })
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { if (nameState.isNotEmpty()) perfilViewModel.updateProfile(nameState, passwordState.ifEmpty { null }) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = FocusBlue, contentColor = Color.White)) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(10.dp)); Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
